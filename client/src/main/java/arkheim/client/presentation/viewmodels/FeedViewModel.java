package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.FeedPort;
import arkheim.client.domain.ports.dtos.PostDto;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer state and actions for the home feed. Wraps
 * {@link FeedPort} and exposes JavaFX-bindable properties so the view
 * never talks to the port directly.
 */
public class FeedViewModel {

    private final FeedPort feedPort;

    // --- loaded feed ---
    private final ObservableList<PostDto> homeFeed = FXCollections.observableArrayList();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public FeedViewModel(FeedPort feedPort) {
        this.feedPort = feedPort;
    }

    /**
     * Loads the home feed for the given user via {@link FeedPort#getHomeFeed}
     * and replaces the contents of {@link #homeFeedProperty()}.
     */
    public void loadHomeFeed(UUID userId) {
        errorMessage.set("");
        try {
            List<PostDto> loaded = feedPort.getHomeFeed(userId);
            homeFeed.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- collection getter ---
    public ObservableList<PostDto> homeFeedProperty() { return homeFeed; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
