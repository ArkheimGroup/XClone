package arkheim.client.presentation.state;

import java.util.UUID;

/**
 * Sealed interface defining all possible UI Events (Intents) that can be triggered
 * on the main Feed Timeline. These events flow into the state machine / ViewModel
 * to compute the next state.
 */
public sealed interface FeedUiEvent {

    /**
     * Triggers fetching the latest feed posts for the timeline.
     */
    record LoadFeed(UUID userId) implements FeedUiEvent {}

    /**
     * Triggers switching between "For You" (global/timeline) and "Following" tabs.
     */
    record SwitchTab(FeedUiState.TabType tabType, UUID userId) implements FeedUiEvent {}

    /**
     * Fired when the user types in the inline composer.
     */
    record UpdateComposerText(String text) implements FeedUiEvent {}

    /**
     * Triggers creating a new post from the composer content, with optional mediaUrl.
     */
    record SubmitPost(UUID authorId, String mediaUrl) implements FeedUiEvent {
        public SubmitPost(UUID authorId) {
            this(authorId, null);
        }
    }


    /**
     * Triggers liking/unliking a specific post.
     */
    record ToggleLike(UUID postId, UUID userId) implements FeedUiEvent {}

    /**
     * Triggers reposting/un-reposting a specific post.
     */
    record ToggleRepost(UUID postId, UUID userId) implements FeedUiEvent {}

    /**
     * Triggers deleting a specific post.
     */
    record DeletePost(UUID postId, UUID userId) implements FeedUiEvent {}

    /**
     * Triggers search matching posts by word.
     */
    record PerformSearch(String query, UUID userId) implements FeedUiEvent {}

    /**
     * Clears error banner state.
     */
    record ClearError() implements FeedUiEvent {}
}
