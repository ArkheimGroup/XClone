package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.ports.PostPort;
import arkheim.client.presentation.utils.ExceptionMessageRetriever;
import javafx.beans.property.*;
import javafx.collections.*;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Presentation-layer state and actions for viewing, creating, and
 * interacting with posts. Wraps {@link PostPort} and exposes
 * JavaFX-bindable properties so the view never talks to the port directly.
 */
public class PostViewModel {

    private final PostPort postPort;
    private final arkheim.client.domain.ports.HashtagPort hashtagPort;

    // --- loaded collections ---
    private final ObservableList<PostDetailDto> timeline = FXCollections.observableArrayList();
    private final ObservableList<PostDetailDto> replies = FXCollections.observableArrayList();

    // --- single loaded post (e.g. a detail/thread view) ---
    private final ObjectProperty<PostDetailDto> currentPost = new SimpleObjectProperty<>();

    // --- posts matching the current search query ---
    private final ObservableList<PostDetailDto> searchResults = FXCollections.observableArrayList();

    // --- posts created by a specific user ---
    private final ObservableList<PostDetailDto> userPosts = FXCollections.observableArrayList();

    // --- create-post form fields ---
    private final StringProperty newPostContent = new SimpleStringProperty("");
    private final StringProperty newPostMediaUrl = new SimpleStringProperty("");
    private final ObjectProperty<UUID> newPostParentId = new SimpleObjectProperty<>(); // null => top-level post

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public PostViewModel(PostPort postPort, arkheim.client.domain.ports.HashtagPort hashtagPort) {
        this.postPort = postPort;
        this.hashtagPort = hashtagPort;
    }

    public PostViewModel(PostPort postPort) {
        this(postPort, new arkheim.client.infrastructure.adapter.HttpHashtagAdapter());
    }

    /**
     * Creates a post using the current {@link #newPostContentProperty()},
     * {@link #newPostMediaUrlProperty()}, and {@link #newPostParentIdProperty()}
     * values via {@link PostPort#createPost}, then clears the form fields
     */
    public void createPost(UUID authorId){
        errorMessage.set("");
        if (newPostContent.get() != null && newPostContent.get().length() > 280) {
            errorMessage.set("Post content exceeds 280 character limit.");
            return;
        }
        try {
            PostDetailDto created = postPort.createPost(
                    authorId,
                    newPostContent.get(),
                    newPostMediaUrl.get(),
                    newPostParentId.get()
            );
            clearNewPostForm();
            currentPost.set(created);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Resets the create-post form fields to their default (empty/null)
     * values. Called automatically after a successful {@link #createPost}.
     */
    public void clearNewPostForm() {
        newPostContent.set("");
        newPostMediaUrl.set("");
        newPostParentId.set(null);
    }

    /**
     * Deletes the given post via {@link PostPort#deletePost}.
     * removes the post locally from both {@link #timelineProperty()} and
     * {@link #repliesProperty()} rather than re-fetching either list
     */
    public void deletePost(UUID postId, UUID requesterId) {
        errorMessage.set("");
        try {
            postPort.deletePost(postId, requesterId);
            timeline.removeIf(p -> p.id().equals(postId));
            replies.removeIf(p -> p.id().equals(postId));
            userPosts.removeIf(p -> p.id().equals(postId));
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Loads a user's timeline (all the created posts) via {@link PostPort#getUserTimeline} and
     * replaces the contents of {@link #timelineProperty()}.
     */
    public void loadUserTimeline(UUID requesterId){
        errorMessage.set("");
        try {
            List<PostDetailDto> posts = postPort.getUserTimeline(requesterId);
            timeline.setAll(posts);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Loads a single post's details via {@link PostPort#getPostDetails}
     * and stores it in {@link #currentPostProperty()}.
     */
    public void loadPostDetails(UUID postId, UUID requesterId) {
        errorMessage.set("");
        try {
            currentPost.set(postPort.getPostDetails(postId, requesterId));
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Synchronously/directly fetches details of a post by ID via {@link PostPort#getPostDetails}.
     */
    public PostDetailDto fetchPostDetails(UUID postId, UUID requesterId) {
        if (postId == null) return null;
        try {
            return postPort.getPostDetails(postId, requesterId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Synchronously/directly fetches the parent chain up to maxDepth levels.
     * Returns a list ordered from oldest parent (top) to immediate parent (bottom).
     */
    public List<PostDetailDto> fetchParentChain(UUID immediateParentId, UUID requesterId) {
        List<PostDetailDto> chain = new ArrayList<>();
        if (immediateParentId == null) return chain;

        UUID currentId = immediateParentId;
        int depth = 0;
        while (currentId != null && depth < 10) {
            PostDetailDto parent = fetchPostDetails(currentId, requesterId);
            if (parent == null) break;
            chain.add(0, parent); // Prepend to order from oldest to newest
            currentId = parent.parentPostId();
            depth++;
        }
        return chain;
    }

    /**
     * Loads replies to a post via {@link PostPort#getPostReplies} and
     * replaces the contents of {@link #repliesProperty()}.
     */
    public void loadPostReplies(UUID postId, UUID requesterId) {
        errorMessage.set("");
        try {
            List<PostDetailDto> loaded = postPort.getPostReplies(postId, requesterId);
            replies.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Toggles like state on the given post via {@link PostPort#toggleLike}.
     * flips {@code likedByMe} and adjusts
     * {@code likeCount} locally — in {@link #currentPostProperty()} if it
     * matches, and in {@link #timelineProperty()}/{@link #repliesProperty()}
     * wherever the post appears — rather than re-fetching.
     */
    public void toggleLike(UUID postId, UUID userId) {
        errorMessage.set("");
        try {
            postPort.toggleLike(postId, userId);
            replaceWherePresent(postId, this::withToggledLike);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Returns a copy of the given post with {@code likedByMe} flipped and
     * {@code likeCount} adjusted accordingly, since {@link PostDetailDto} is an
     * immutable record and cannot be mutated in place.
     */
    private PostDetailDto withToggledLike(PostDetailDto p) {
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

    /**
     * Reposts the given post on behalf of {@code authorId} via {@link PostPort#createPost}.
     * Adjusts {@code repostCount} and flips {@code repostedByMe} locally.
     */
    public void repost(UUID postId, UUID authorId) {
        errorMessage.set("");
        try {
            PostDetailDto current = findPostById(postId);
            boolean alreadyReposted = current != null && current.isRepostedByMe();

            if (alreadyReposted) {
                List<PostDetailDto> userTimeline = postPort.getUserTimeline(authorId);
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
                    postPort.deletePost(repostToDelete.id(), authorId);
                }
            } else {
                postPort.createPost(authorId, "", null, postId);
            }

            replaceWherePresent(postId, this::withToggledRepost);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    private PostDetailDto findPostById(UUID postId) {
        if (currentPost.get() != null && currentPost.get().id().equals(postId)) {
            return currentPost.get();
        }
        for (PostDetailDto p : timeline) {
            if (p.id().equals(postId)) return p;
        }
        for (PostDetailDto p : userPosts) {
            if (p.id().equals(postId)) return p;
        }
        for (PostDetailDto p : replies) {
            if (p.id().equals(postId)) return p;
        }
        for (PostDetailDto p : searchResults) {
            if (p.id().equals(postId)) return p;
        }
        return null;
    }

    private PostDetailDto withToggledRepost(PostDetailDto p) {
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

    /**
     * Applies the given transform to the post with the given id, wherever
     * it currently appears among {@link #currentPostProperty()},
     * {@link #timelineProperty()}, and {@link #repliesProperty()}.
     */
    private void replaceWherePresent(UUID postId, UnaryOperator<PostDetailDto> transform) {
        PostDetailDto current = currentPost.get();
        if (current != null && current.id().equals(postId)) {
            currentPost.set(transform.apply(current));
        }
        replaceInList(timeline, postId, transform);
        replaceInList(replies, postId, transform);
        replaceInList(userPosts, postId, transform);
    }
    private void replaceInList(ObservableList<PostDetailDto> list, UUID postId, UnaryOperator<PostDetailDto> transform) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(postId)) {
                list.set(i, transform.apply(list.get(i)));
                break;
            }
        }
    }

    /**
     * Searches for posts matching the given word via
     * {@link PostPort#findPostsByWord} and stores the results in
     * {@link #searchResultsProperty()}.
     */
    public void findPostsByWord(String word, UUID requesterId) {
        errorMessage.set("");
        try {
            String cleanQuery = word != null ? word.trim() : "";
            if (cleanQuery.isEmpty()) {
                searchResults.clear();
                return;
            }

            List<PostDetailDto> results;
            if (cleanQuery.startsWith("#")) {
                String hashtagTag = cleanQuery.substring(1).trim();
                if (hashtagTag.isBlank()) {
                    results = List.of();
                } else if (hashtagPort != null) {
                    results = hashtagPort.getPostsByHashtag(hashtagTag, requesterId);
                } else {
                    results = postPort.findPostsByWord(cleanQuery, requesterId);
                }
            } else {
                results = new ArrayList<>(postPort.findPostsByWord(cleanQuery, requesterId));
                if (hashtagPort != null && !cleanQuery.isBlank()) {
                    try {
                        List<PostDetailDto> hashtagPosts = hashtagPort.getPostsByHashtag(cleanQuery, requesterId);
                        if (hashtagPosts != null && !hashtagPosts.isEmpty()) {
                            Set<UUID> existingIds = results.stream().map(PostDetailDto::id).collect(Collectors.toSet());
                            for (PostDetailDto hp : hashtagPosts) {
                                if (!existingIds.contains(hp.id())) {
                                    results.add(hp);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            searchResults.setAll(results);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    /**
     * Loads posts created by a specific user via {@link PostPort#getUserPosts} and
     * replaces the contents of {@link #userPostsProperty()}.
     */
    public void loadUserPosts(String username) {
        errorMessage.set("");
        try {
            List<PostDetailDto> posts = postPort.getUserPosts(username);
            userPosts.setAll(posts);
        } catch (Exception e) {
            errorMessage.set(ExceptionMessageRetriever.getMessage(e));
        }
    }

    // Getters

    // --- collection getters ---
    public ObservableList<PostDetailDto> timelineProperty() { return timeline; }
    public ObservableList<PostDetailDto> repliesProperty() { return replies; }

    // --- single post getter ---
    public ReadOnlyObjectProperty<PostDetailDto> currentPostProperty() { return currentPost; }

    // --- create-post form property getters ---
    public StringProperty newPostContentProperty() { return newPostContent; }
    public StringProperty newPostMediaUrlProperty() { return newPostMediaUrl; }
    public ObjectProperty<UUID> newPostParentIdProperty() { return newPostParentId; }

    // --- search results ---
    public ObservableList<PostDetailDto> searchResultsProperty() { return searchResults; }

    // --- user posts ---
    public ObservableList<PostDetailDto> userPostsProperty() { return userPosts; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
