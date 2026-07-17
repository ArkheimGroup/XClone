package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.PostPort;
import arkheim.client.domain.ports.dtos.PostDto;
import javafx.beans.property.*;
import javafx.collections.*;
import java.util.function.UnaryOperator;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer state and actions for viewing, creating, and
 * interacting with posts. Wraps {@link PostPort} and exposes
 * JavaFX-bindable properties so the view never talks to the port directly.
 */
public class PostViewModel {

    private final PostPort postPort;

    // --- loaded collections ---
    private final ObservableList<PostDto> timeline = FXCollections.observableArrayList();
    private final ObservableList<PostDto> replies = FXCollections.observableArrayList();

    // --- single loaded post (e.g. a detail/thread view) ---
    private final ObjectProperty<PostDto> currentPost = new SimpleObjectProperty<>();

    // --- posts matching the current search query ---
    private final ObservableList<PostDto> searchResults = FXCollections.observableArrayList();

    // --- create-post form fields ---
    private final StringProperty newPostContent = new SimpleStringProperty("");
    private final StringProperty newPostMediaUrl = new SimpleStringProperty("");
    private final ObjectProperty<UUID> newPostParentId = new SimpleObjectProperty<>(); // null => top-level post

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public PostViewModel(PostPort postPort) {
        this.postPort = postPort;
    }

    /**
     * Creates a post using the current {@link #newPostContentProperty()},
     * {@link #newPostMediaUrlProperty()}, and {@link #newPostParentIdProperty()}
     * values via {@link PostPort#createPost}, then clears the form fields
     */
    public void createPost(UUID authorId){
        errorMessage.set("");
        try {
            PostDto created = postPort.createPost(
                    authorId,
                    newPostContent.get(),
                    newPostMediaUrl.get(),
                    newPostParentId.get()
            );
            clearNewPostForm();
            currentPost.set(created);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
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
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads a user's timeline via {@link PostPort#getUserTimeline} and
     * replaces the contents of {@link #timelineProperty()}.
     */
//    public void loadUserTimeline(String username, UUID requesterId){
//        errorMessage.set("");
//        try {
//            List<PostDto> posts = postPort.getUserTimeline(username, requesterId);
//            timeline.setAll(posts);
//        } catch (Exception e) {
//            errorMessage.set(e.getMessage());
//        }
//    }

    /**
     * Loads a single post's details via {@link PostPort#getPostDetails}
     * and stores it in {@link #currentPostProperty()}.
     */
    public void loadPostDetails(UUID postId, UUID requesterId) {
        errorMessage.set("");
        try {
            currentPost.set(postPort.getPostDetails(postId, requesterId));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads replies to a post via {@link PostPort#getPostReplies} and
     * replaces the contents of {@link #repliesProperty()}.
     */
    public void loadPostReplies(UUID postId, UUID requesterId) {
        errorMessage.set("");
        try {
            List<PostDto> loaded = postPort.getPostReplies(postId, requesterId);
            replies.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
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
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Returns a copy of the given post with {@code likedByMe} flipped and
     * {@code likeCount} adjusted accordingly, since {@link PostDto} is an
     * immutable record and cannot be mutated in place.
     */
    private PostDto withToggledLike(PostDto p) {
        boolean nowLiked = !p.likedByMe();
        int newLikeCount = nowLiked ? p.likeCount() + 1 : p.likeCount() - 1;
        return new PostDto(
                p.id(), p.authorId(), p.authorUsername(), p.authorName(), p.authorPfpUrl(),
                p.content(), p.mediaUrls(), p.createdAt(), newLikeCount, p.repostCount(),
                p.replyCount(), p.parentPostId(), nowLiked, p.repostedByMe()
        );
    }

    /**
     * Applies the given transform to the post with the given id, wherever
     * it currently appears among {@link #currentPostProperty()},
     * {@link #timelineProperty()}, and {@link #repliesProperty()}.
     */
    private void replaceWherePresent(UUID postId, UnaryOperator<PostDto> transform) {
        PostDto current = currentPost.get();
        if (current != null && current.id().equals(postId)) {
            currentPost.set(transform.apply(current));
        }
        replaceInList(timeline, postId, transform);
        replaceInList(replies, postId, transform);
    }
    private void replaceInList(ObservableList<PostDto> list, UUID postId, UnaryOperator<PostDto> transform) {
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
    public void findPostsByWord(String word) {
        errorMessage.set("");
        try {
            List<PostDto> results = postPort.findPostsByWord(word);
            searchResults.setAll(results);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- collection getters ---
    public ObservableList<PostDto> timelineProperty() { return timeline; }
    public ObservableList<PostDto> repliesProperty() { return replies; }

    // --- single post getter ---
    public ReadOnlyObjectProperty<PostDto> currentPostProperty() { return currentPost; }

    // --- create-post form property getters ---
    public StringProperty newPostContentProperty() { return newPostContent; }
    public StringProperty newPostMediaUrlProperty() { return newPostMediaUrl; }
    public ObjectProperty<UUID> newPostParentIdProperty() { return newPostParentId; }

    // --- search results ---
    public ObservableList<PostDto> searchResultsProperty() { return searchResults; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
