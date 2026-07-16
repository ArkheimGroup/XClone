package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.FollowPort;
import arkheim.client.domain.ports.dtos.UserDto;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer state and actions for following/unfollowing users
 * and viewing follower/following lists. Wraps {@link FollowPort} and
 * exposes JavaFX-bindable properties so the view never talks to the
 * port directly.
 */
public class FollowViewModel {

    private final FollowPort followPort;

    // --- follow state for the profile currently being viewed ---
    private final BooleanProperty isFollowing = new SimpleBooleanProperty(false);

    // --- loaded collections ---
    private final ObservableList<UserDto> followers = FXCollections.observableArrayList();
    private final ObservableList<UserDto> following = FXCollections.observableArrayList();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public FollowViewModel(FollowPort followPort) {
        this.followPort = followPort;
    }

    /**
     * Follows {@code followingId} on behalf of {@code followerId} via
     * {@link FollowPort#followUser}.
     * sets {@link #isFollowingProperty()} to {@code true} rather than
     * re-checking via {@link FollowPort#isFollowing}.
     */
    public void followUser(UUID followerId, UUID followingId){
        errorMessage.set("");
        try {
            followPort.followUser(followerId, followingId);
            isFollowing.set(true);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Unfollows {@code followingId} on behalf of {@code followerId} via
     * {@link FollowPort#unfollowUser}.
     * sets {@link #isFollowingProperty()} to {@code false} rather than
     * re-checking via {@link FollowPort#isFollowing}.
     */
    public void unfollowUser(UUID followerId, UUID followingId) {
        errorMessage.set("");
        try {
            followPort.unfollowUser(followerId, followingId);
            isFollowing.set(false);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Checks whether {@code followerId} follows {@code followingId} via
     * {@link FollowPort#isFollowing} and stores the result in
     * {@link #isFollowingProperty()}.
     * Call this when a profile is first loaded.
     */
    public void checkIsFollowing(UUID followerId, UUID followingId) {
        errorMessage.set("");
        try {
            isFollowing.set(followPort.isFollowing(followerId, followingId));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads the followers of the given user via
     * {@link FollowPort#getFollowers} and replaces the contents of
     * {@link #followersProperty()}.
     */
    public void loadFollowers(UUID userId) {
        errorMessage.set("");
        try {
            List<UserDto> loaded = followPort.getFollowers(userId);
            followers.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads the users the given user is following via
     * {@link FollowPort#getFollowing} and replaces the contents of
     * {@link #followingProperty()}.
     */
    public void loadFollowing(UUID userId) {
        errorMessage.set("");
        try {
            List<UserDto> loaded = followPort.getFollowing(userId);
            following.setAll(loaded);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- follow state getter ---
    public ReadOnlyBooleanProperty isFollowingProperty() { return isFollowing; }

    // --- collection getters ---
    public ObservableList<UserDto> followersProperty() { return followers; }
    public ObservableList<UserDto> followingProperty() { return following; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
