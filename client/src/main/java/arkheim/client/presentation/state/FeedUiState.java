package arkheim.client.presentation.state;

import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import java.util.List;

/**
 * Immutable UI State representing the current visual state of the Feed timeline.
 * Built for clean unidirectional data flow (UDF) and reactive UI patterns.
 */
public record FeedUiState(
        boolean isLoading,
        List<PostDetailDto> posts,
        String error,
        String composerText,
        boolean isPosting,
        TabType activeTab,
        String searchQuery,
        List<PostDetailDto> searchResults
) {
    public enum TabType {
        FOR_YOU,
        FOLLOWING
    }

    /**
     * @return The initial default state.
     */
    public static FeedUiState initial() {
        return new FeedUiState(
                false,
                List.of(),
                null,
                "",
                false,
                TabType.FOR_YOU,
                "",
                List.of()
        );
    }

    public FeedUiState withLoading(boolean isLoading) {
        return new FeedUiState(isLoading, this.posts, this.error, this.composerText, this.isPosting, this.activeTab, this.searchQuery, this.searchResults);
    }

    public FeedUiState withPosts(List<PostDetailDto> posts) {
        return new FeedUiState(this.isLoading, List.copyOf(posts), this.error, this.composerText, this.isPosting, this.activeTab, this.searchQuery, this.searchResults);
    }

    public FeedUiState withError(String error) {
        return new FeedUiState(this.isLoading, this.posts, error, this.composerText, this.isPosting, this.activeTab, this.searchQuery, this.searchResults);
    }

    public FeedUiState withComposerText(String composerText) {
        return new FeedUiState(this.isLoading, this.posts, this.error, composerText, this.isPosting, this.activeTab, this.searchQuery, this.searchResults);
    }

    public FeedUiState withPosting(boolean isPosting) {
        return new FeedUiState(this.isLoading, this.posts, this.error, this.composerText, isPosting, this.activeTab, this.searchQuery, this.searchResults);
    }

    public FeedUiState withActiveTab(TabType activeTab) {
        return new FeedUiState(this.isLoading, this.posts, this.error, this.composerText, this.isPosting, activeTab, this.searchQuery, this.searchResults);
    }

    public boolean isSearching() {
        return searchQuery != null && !searchQuery.isBlank();
    }

    public FeedUiState withSearch(String searchQuery, List<PostDetailDto> searchResults) {
        return new FeedUiState(this.isLoading, this.posts, this.error, this.composerText, this.isPosting, this.activeTab, searchQuery, List.copyOf(searchResults));
    }
}
