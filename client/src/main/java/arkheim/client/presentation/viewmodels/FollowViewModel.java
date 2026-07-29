package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.FollowPort;
import arkheim.client.domain.ports.dtos.UserDto;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    // --- broadcast property when follow status changes for a specific user ID ---
    private final ObjectProperty<UUID> lastFollowedUserId = new SimpleObjectProperty<>(null);

    // --- in-memory set of user IDs that the current user is following ---
    private final Set<UUID> followedUserIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile boolean followedUserIdsLoaded = false;

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
            if (followingId != null) {
                followedUserIds.add(followingId);
            }
            isFollowing.set(true);
            lastFollowedUserId.set(null);
            lastFollowedUserId.set(followingId);
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
            if (followingId != null) {
                followedUserIds.remove(followingId);
            }
            isFollowing.set(false);
            lastFollowedUserId.set(null);
            lastFollowedUserId.set(followingId);
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
            boolean result = followPort.isFollowing(followerId, followingId);
            if (result && followingId != null) {
                followedUserIds.add(followingId);
            } else if (!result && followingId != null) {
                followedUserIds.remove(followingId);
            }
            isFollowing.set(result);
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
            followedUserIds.clear();
            if (loaded != null) {
                for (UserDto u : loaded) {
                    if (u != null && u.id() != null) {
                        followedUserIds.add(u.id());
                    }
                }
            }
            followedUserIdsLoaded = true;
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Returns true if {@code followerId} is following {@code targetUserId}.
     */
    public boolean isFollowingUser(UUID followerId, UUID targetUserId) {
        if (followerId == null || targetUserId == null) {
            return false;
        }
        if (followedUserIds.contains(targetUserId)) {
            return true;
        }
        if (!followedUserIdsLoaded) {
            try {
                boolean follows = followPort.isFollowing(followerId, targetUserId);
                if (follows) {
                    followedUserIds.add(targetUserId);
                }
                return follows;
            } catch (Exception ignored) {
            }
        }
        return followedUserIds.contains(targetUserId);
    }

    // Getters

    // --- follow state getter ---
    public ReadOnlyBooleanProperty isFollowingProperty() { return isFollowing; }
    public ReadOnlyObjectProperty<UUID> lastFollowedUserIdProperty() { return lastFollowedUserId; }

    // --- collection getters ---
    public ObservableList<UserDto> followersProperty() { return followers; }
    public ObservableList<UserDto> followingProperty() { return following; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }

}
