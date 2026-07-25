package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.HashtagPort;
import arkheim.client.domain.ports.dtos.HashtagDto;
import arkheim.client.domain.ports.dtos.PostDto;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer state and actions for hashtag-based post discovery.
 * Wraps {@link HashtagPort} and exposes JavaFX-bindable properties so
 * the view never talks to the port directly.
 */
public class HashtagViewModel {

    private final HashtagPort hashtagPort;

    // --- posts tagged with the hashtag currently being browsed ---
    private final ObservableList<PostDto> postsByHashtag = FXCollections.observableArrayList();

    // --- hashtags attached to a single post currently being viewed ---
    private final ObservableList<HashtagDto> hashtagsForPost = FXCollections.observableArrayList();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public HashtagViewModel(HashtagPort hashtagPort) {
        this.hashtagPort = hashtagPort;
    }

    /**
     * Loads posts tagged with the given hashtag via
     * {@link HashtagPort#getPostsByHashtag} and replaces the contents of
     * {@link #postsByHashtagProperty()}.
     */
    public void loadPostsByHashtag(String hashtagName, UUID requesterId) {
        errorMessage.set("");
        try {
            List<PostDto> loaded = hashtagPort.getPostsByHashtag(hashtagName, requesterId);
            postsByHashtag.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Searches for posts tagged with a hashtag query (handling leading '#' if present).
     */
    public void searchPostsByHashtag(String query, UUID requesterId) {
        if (query == null || query.isBlank()) {
            postsByHashtag.clear();
            return;
        }
        String cleanTag = query.trim();
        if (cleanTag.startsWith("#")) {
            cleanTag = cleanTag.substring(1).trim();
        }
        loadPostsByHashtag(cleanTag, requesterId);
    }

    /**
     * Loads the hashtags attached to the given post via
     * {@link HashtagPort#getHashtagsForPost} and replaces the contents of
     * {@link #hashtagsForPostProperty()}.
     */
    public void loadHashtagsForPost(UUID postId) {
        errorMessage.set("");
        try {
            List<HashtagDto> loaded = hashtagPort.getHashtagsForPost(postId);
            hashtagsForPost.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- collection getters ---
    public ObservableList<PostDto> postsByHashtagProperty() { return postsByHashtag; }
    public ObservableList<HashtagDto> hashtagsForPostProperty() { return hashtagsForPost; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
