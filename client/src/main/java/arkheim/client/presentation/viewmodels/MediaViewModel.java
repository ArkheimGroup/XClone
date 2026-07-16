package arkheim.client.presentation.viewmodels;


import arkheim.client.domain.ports.MediaPort;
import arkheim.client.domain.ports.dtos.MediaDto;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer state and actions for managing media attached to
 * posts. Wraps {@link MediaPort} and exposes JavaFX-bindable properties
 * so the view never talks to the port directly.
 */
public class MediaViewModel {

    private final MediaPort mediaPort;

    // --- media currently associated with the post being viewed/composed ---
    private final ObservableList<MediaDto> postMedia = FXCollections.observableArrayList();

    // --- most recently registered media, e.g. right after an upload ---
    private final ObjectProperty<MediaDto> lastRegisteredMedia = new SimpleObjectProperty<>();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public MediaViewModel(MediaPort mediaPort) {
        this.mediaPort = mediaPort;
    }

    /**
     * Registers a new media record via {@link MediaPort#registerMedia} and
     * stores the result in {@link #lastRegisteredMediaProperty()}. Does not
     * itself add the result to {@link #postMediaProperty()} or link it to any post.
     */
    public void registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy) {
        errorMessage.set("");
        try {
            MediaDto registered = mediaPort.registerMedia(url, width, height, fileSize, uploadedBy);
            lastRegisteredMedia.set(registered);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Links the given media to the given post via
     * {@link MediaPort#linkMediaToPost}.
     * adds the media locally to {@link #postMediaProperty()} if not already present,
     * rather than re-fetching the full list.
     */
    public void linkMediaToPost(MediaDto media, UUID postId) {
        errorMessage.set("");
        try {
            mediaPort.linkMediaToPost(media.id(), postId);
            boolean alreadyPresent = postMedia.stream().anyMatch(m -> m.id().equals(media.id()));
            if (!alreadyPresent) {
                postMedia.add(media);
            }
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Unlinks the given media from the given post via
     * {@link MediaPort#unlinkMediaFromPost}.
     * removes the media locally from {@link #postMediaProperty()} rather than
     * re-fetching the full list.
     */
    public void unlinkMediaFromPost(UUID mediaId, UUID postId) {
        errorMessage.set("");
        try {
            mediaPort.unlinkMediaFromPost(mediaId, postId);
            postMedia.removeIf(m -> m.id().equals(mediaId));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Deletes the given media entirely via {@link MediaPort#deleteMedia}.
     * removes it locally from {@link #postMediaProperty()}
     * rather than re-fetching the full list.
     */
    public void deleteMedia(UUID mediaId) {
        errorMessage.set("");
        try {
            mediaPort.deleteMedia(mediaId);
            postMedia.removeIf(m -> m.id().equals(mediaId));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads all media attached to the given post via
     * {@link MediaPort#getMediaForPost} and replaces the contents of {@link #postMediaProperty()}.
     */
    public void loadMediaForPost(UUID postId) {
        errorMessage.set("");
        try {
            List<MediaDto> loaded = mediaPort.getMediaForPost(postId);
            postMedia.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- collection getter ---
    public ObservableList<MediaDto> postMediaProperty() { return postMedia; }

    // --- last registered media getter ---
    public ReadOnlyObjectProperty<MediaDto> lastRegisteredMediaProperty() { return lastRegisteredMedia; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
