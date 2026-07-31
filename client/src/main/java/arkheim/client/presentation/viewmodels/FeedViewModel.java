package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.ports.FeedPort;
import arkheim.client.domain.ports.HashtagPort;
import arkheim.client.domain.ports.PostPort;
import arkheim.client.infrastructure.adapter.HttpHashtagAdapter;
import arkheim.client.presentation.state.FeedUiEvent;
import arkheim.client.presentation.state.FeedUiState;
import arkheim.client.presentation.utils.ExceptionMessageRetriever;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Presentation-layer ViewModel for the main feed, designed with Unidirectional Data Flow (UDF).
 * It consumes UI Events (Intents), processes them by invoking domain Ports, and emits
 * new immutable FeedUiState snapshots.
 */
public class FeedViewModel {

    private final FeedPort feedPort;
    private final PostPort postPort;
    private final HashtagPort hashtagPort;

    private final ScheduledExecutorService autoRefreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> autoRefreshTask;
    private UUID currentUserId;

    // The single source of truth for the Feed UI
    private final ObjectProperty<FeedUiState> uiState = new SimpleObjectProperty<>(FeedUiState.initial());

    public FeedViewModel(FeedPort feedPort, PostPort postPort, HashtagPort hashtagPort) {
        this.feedPort = feedPort;
        this.postPort = postPort;
        this.hashtagPort = hashtagPort;
    }

    public FeedViewModel(FeedPort feedPort, PostPort postPort) {
        this(feedPort, postPort, new HttpHashtagAdapter());
    }

    private synchronized void startAutoRefresh(UUID userId) {
        this.currentUserId = userId;
        if (autoRefreshTask != null && !autoRefreshTask.isCancelled()) {
            return;
        }
        autoRefreshTask = autoRefreshExecutor.scheduleAtFixedRate(() -> {
            try {
                if (currentUserId == null || uiState.get().isSearching() || uiState.get().isPosting()) {
                    return;
                }
                List<PostDetailDto> latest;
                if (uiState.get().activeTab() == FeedUiState.TabType.FOLLOWING) {
                    latest = feedPort.getHomeFeed(currentUserId);
                } else {
                    latest = postPort.getUserTimeline(currentUserId);
                }
                if (latest != null && !latest.equals(uiState.get().posts())) {
                    try {
                        Platform.runLater(() -> {
                            if (!uiState.get().isSearching()) {
                                uiState.set(uiState.get().withPosts(latest));
                            }
                        });
                    } catch (IllegalStateException e) {
                        uiState.set(uiState.get().withPosts(latest));
                    }
                }
            } catch (Exception ignored) {}
        }, 3, 3, TimeUnit.SECONDS);
    }

    /**
     * Exposes the read-only UI State property for the view to observe and bind to.
     */
    public ReadOnlyObjectProperty<FeedUiState> uiStateProperty() {
        return uiState;
    }

    /**
     * Returns the current state snapshot.
     */
    public FeedUiState getState() {
        return uiState.get();
    }

    /**
     * Main entry point for the View to send events.
     * Decouples the UI elements from the backend logic.
     * Uses Java 25 pattern matching for a clean dispatcher.
     */
    public void processEvent(FeedUiEvent event) {
        switch (event) {
            case FeedUiEvent.LoadFeed e -> handleLoadFeed(e.userId());
            case FeedUiEvent.SwitchTab e -> handleSwitchTab(e.tabType(), e.userId());
            case FeedUiEvent.UpdateComposerText e -> handleUpdateComposer(e.text());
            case FeedUiEvent.SubmitPost e -> handleSubmitPost(e.authorId(), e.mediaUrl());
            case FeedUiEvent.ToggleLike e -> handleToggleLike(e.postId(), e.userId());
            case FeedUiEvent.ToggleRepost e -> handleToggleRepost(e.postId(), e.userId());
            case FeedUiEvent.DeletePost e -> handleDeletePost(e.postId(), e.userId());
            case FeedUiEvent.PerformSearch e -> handleSearch(e.query(), e.userId());
            case FeedUiEvent.ClearError ignored -> handleClearError();
        }
    }

    private void handleLoadFeed(UUID userId) {
        startAutoRefresh(userId);
        uiState.set(uiState.get().withLoading(true).withError(null));
        try {
            List<PostDetailDto> loaded;
            if (uiState.get().activeTab() == FeedUiState.TabType.FOLLOWING) {
                // Following tab: load posts from followed users
                loaded = feedPort.getHomeFeed(userId);
            } else {
                // For You tab: load all timeline posts as recommendation
                loaded = postPort.getUserTimeline(userId);
            }
            uiState.set(uiState.get().withPosts(loaded).withSearch("", List.of()).withLoading(false));
        } catch (Exception e) {
            String message = ExceptionMessageRetriever.getMessage(e);
            uiState.set(uiState.get().withError("Failed to load timeline: " + message).withLoading(false));
        }
    }

    private void handleSwitchTab(FeedUiState.TabType tabType, UUID userId) {
        if (uiState.get().activeTab() == tabType && !uiState.get().isSearching()) {
            return;
        }
        uiState.set(uiState.get().withActiveTab(tabType).withSearch("", List.of()));
        handleLoadFeed(userId);
    }

    private void handleUpdateComposer(String text) {
        uiState.set(uiState.get().withComposerText(text));
    }

    private void handleSubmitPost(UUID authorId, String mediaUrl) {
        String content = uiState.get().composerText();
        boolean hasContent = content != null && !content.isBlank();
        boolean hasMedia = mediaUrl != null && !mediaUrl.isBlank();
        if (!hasContent && !hasMedia) {
            return;
        }

        uiState.set(uiState.get().withPosting(true).withError(null));
        try {
            // Call postPort to create a top-level post with optional mediaUrl
            PostDetailDto created = postPort.createPost(authorId, hasContent ? content : "", mediaUrl, null);

            // Prepend the new post directly to timeline for instant feedback
            List<PostDetailDto> updatedPosts = new ArrayList<>();
            updatedPosts.add(created);
            updatedPosts.addAll(uiState.get().posts());

            uiState.set(uiState.get()
                    .withPosts(updatedPosts)
                    .withComposerText("")
                    .withPosting(false));
        } catch (Exception e) {
            String message = ExceptionMessageRetriever.getMessage(e);
            uiState.set(uiState.get().withError("Could not share post: " + message).withPosting(false));
        }
    }

    private void handleToggleLike(UUID postId, UUID userId) {
        try {
            postPort.toggleLike(postId, userId);

            // Reactively map current posts list to update the liked state locally
            List<PostDetailDto> updatedPosts = uiState.get().posts().stream()
                    .map(p -> {
                        if (p.id().equals(postId)) {
                            boolean nowLiked = !p.isLikedByMe();
                            int newLikeCount = nowLiked ? p.likeCount() + 1 : p.likeCount() - 1;
                            return new PostDetailDto(
                                    p.id(),
                                    p.authorId(),
                                    p.authorUsername(),
                                    p.authorName(),
                                    p.authorPfpUrl(),
                                    p.content(),
                                    p.mediaUrls(),
                                    p.createdAt(),
                                    newLikeCount,
                                    p.repostCount(),
                                    p.replyCount(),
                                    p.parentPostId(),
                                    p.repliedUsername(),
                                    p.isRepost(),
                                    p.repostedFromUsername(),
                                    nowLiked,
                                    p.isRepostedByMe()
                            );
                        }
                        return p;
                    }).toList();

            uiState.set(uiState.get().withPosts(updatedPosts));
        } catch (Exception e) {
            String message = ExceptionMessageRetriever.getMessage(e);
            uiState.set(uiState.get().withError("Could not update like state: " + message));
        }
    }

    private void handleToggleRepost(UUID postId, UUID userId) {
        try {
            PostDetailDto target = uiState.get().posts().stream()
                    .filter(p -> p.id().equals(postId))
                    .findFirst()
                    .orElse(null);

            boolean isAlreadyReposted = target != null && target.isRepostedByMe();

            if (isAlreadyReposted) {
                List<PostDetailDto> userTimeline = postPort.getUserTimeline(userId);
                PostDetailDto repostToDelete = null;
                if (userTimeline != null) {
                    for (PostDetailDto p : userTimeline) {
                        if (p.isRepost() && postId.equals(p.parentPostId())) {
                            repostToDelete = p;
                            break;
                        }
                    }
                }
                if (repostToDelete != null) {
                    postPort.deletePost(repostToDelete.id(), userId);
                }
            } else {
                postPort.createPost(userId, "", null, postId);
            }

            List<PostDetailDto> updatedPosts = uiState.get().posts().stream()
                    .map(p -> {
                        if (p.id().equals(postId)) {
                            boolean nowReposted = !p.isRepostedByMe();
                            int newRepostCount = nowReposted ? p.repostCount() + 1 : Math.max(0, p.repostCount() - 1);
                            return new PostDetailDto(
                                    p.id(),
                                    p.authorId(),
                                    p.authorUsername(),
                                    p.authorName(),
                                    p.authorPfpUrl(),
                                    p.content(),
                                    p.mediaUrls(),
                                    p.createdAt(),
                                    p.likeCount(),
                                    newRepostCount,
                                    p.replyCount(),
                                    p.parentPostId(),
                                    p.repliedUsername(),
                                    p.isRepost(),
                                    p.repostedFromUsername(),
                                    p.isLikedByMe(),
                                    nowReposted
                            );
                        }
                        return p;
                    }).toList();

            uiState.set(uiState.get().withPosts(updatedPosts));
        } catch (Exception e) {
            uiState.set(uiState.get().withError("Could not toggle repost state: " + e.getMessage()));
        }
    }

    private void handleDeletePost(UUID postId, UUID userId) {
        try {
            postPort.deletePost(postId, userId);

            // Reactively remove deleted post from list
            List<PostDetailDto> updatedPosts = uiState.get().posts().stream()
                    .filter(p -> !p.id().equals(postId))
                    .toList();

            uiState.set(uiState.get().withPosts(updatedPosts));
        } catch (Exception e) {
            String message = ExceptionMessageRetriever.getMessage(e);
            uiState.set(uiState.get().withError("Could not delete post: " + message));
        }
    }

    private void handleSearch(String query, UUID userId) {
        if (query == null || query.isBlank()) {
            uiState.set(uiState.get().withSearch("", List.of()));
            return;
        }

        uiState.set(uiState.get().withLoading(true).withError(null));
        try {
            String cleanQuery = query.trim();
            List<PostDetailDto> results;
            if (cleanQuery.startsWith("#")) {
                String hashtagTag = cleanQuery.substring(1).trim();
                if (hashtagTag.isBlank()) {
                    results = List.of();
                } else if (hashtagPort != null) {
                    results = hashtagPort.getPostsByHashtag(hashtagTag, userId);
                } else {
                    results = postPort.findPostsByWord(cleanQuery, userId);
                }
            } else {
                results = new java.util.ArrayList<>(postPort.findPostsByWord(cleanQuery, userId));
                if (hashtagPort != null && !cleanQuery.isBlank()) {
                    try {
                        List<PostDetailDto> hashtagResults = hashtagPort.getPostsByHashtag(cleanQuery, userId);
                        if (hashtagResults != null && !hashtagResults.isEmpty()) {
                            java.util.Set<UUID> existingIds = results.stream().map(PostDetailDto::id).collect(java.util.stream.Collectors.toSet());
                            for (PostDetailDto hp : hashtagResults) {
                                if (!existingIds.contains(hp.id())) {
                                    results.add(hp);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            uiState.set(uiState.get().withSearch(query, results).withLoading(false));
        } catch (Exception e) {
            String message = ExceptionMessageRetriever.getMessage(e);
            uiState.set(uiState.get().withError("Search failed: " + message).withLoading(false));
        }
    }

    private void handleClearError() {
        uiState.set(uiState.get().withError(null));
    }
}
