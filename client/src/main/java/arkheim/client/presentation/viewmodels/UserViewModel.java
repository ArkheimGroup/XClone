package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.UserPort;
import arkheim.client.domain.ports.dtos.UserProfileDto;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Presentation-layer state and actions for viewing and editing a user
 * profile. Wraps {@link UserPort} and exposes JavaFX-bindable properties
 * so the view never talks to the port directly.
 */
public class UserViewModel {

    private final UserPort userPort;

    // --- loaded profile (read-only from the view's perspective) ---
    private final ObjectProperty<UserProfileDto> currentProfile = new SimpleObjectProperty<>();

    // --- edit form fields, populated from currentProfile on demand ---
    private final StringProperty editName = new SimpleStringProperty("");
    private final StringProperty editBiography = new SimpleStringProperty("");
    private final StringProperty editPfpUrl = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> editDateOfBirth = new SimpleObjectProperty<>();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public UserViewModel(UserPort userPort) {
        this.userPort = userPort;
    }

    /**
     * Loads a profile by user id via {@link UserPort#getUserProfileById}
     * and stores it in {@link #currentProfileProperty()} which is a {@link ObjectProperty<UserProfileDto>}.
     */
    public void loadProfileById(UUID userId){
        errorMessage.set("");
        try{
            currentProfile.set(userPort.getUserProfileById(userId));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Loads a profile by user id via {@link UserPort#getUserProfileByUsername}
     * and stores it in {@link #currentProfileProperty()} which is a {@link ObjectProperty<UserProfileDto>}.
     */
    public void loadProfileByUsername(String username) {
        errorMessage.set("");
        try {
            currentProfile.set(userPort.getUserProfileByUsername(username));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Synchronously/directly fetches a user profile by username via {@link UserPort#getUserProfileByUsername}.
     */
    public UserProfileDto fetchProfileByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        try {
            return userPort.getUserProfileByUsername(username.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Copies values from {@link #currentProfileProperty()} into the edit
     * form fields. Call this when entering edit mode so the form starts
     * from the currently loaded profile rather than blank values.
     */
    public void loadFormFromCurrentProfile(){
        UserProfileDto profile = currentProfile.get();
        if(profile == null)
            return;

        editName.set(profile.name());
        editBiography.set(profile.biography());
        editPfpUrl.set(profile.pfpUrl());
        editDateOfBirth.set(profile.dateOfBirth() != null
                ? profile.dateOfBirth().toLocalDate()
                : null);
    }

    /**
     * Submits the current edit form field values via
     * {@link UserPort#updateProfile} and replaces
     * {@link #currentProfileProperty()} with the server's updated profile.
     */
    public void updateProfile(UUID userId){
        errorMessage.set("");
        try{
            LocalDateTime dob = editDateOfBirth.get() != null
                    ? editDateOfBirth.get().atStartOfDay()
                    : null;

            UserProfileDto updated = userPort.updateProfile(
                    userId,
                    editName.get(),
                    editBiography.get(),
                    editPfpUrl.get(),
                    dob
            );
            currentProfile.set(updated);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Pins the given post via {@link UserPort#pinPost}.
     * updates {@link #currentProfileProperty()}'s pinned post id locally rather than re-fetching the whole profile.
     */
    public void pinPost(UUID userId, UUID postId){
        errorMessage.set("");
        try{
            userPort.pinPost(userId, postId);
            replaceCurrentProfilePinnedPostId(postId);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Unpins the current user's pinned post via {@link UserPort#unpinPost}.
     * clears {@link #currentProfileProperty()}'s pinned post id locally rather than re-fetching the whole profile.
     */
    public void unpinPost(UUID userId) {
        errorMessage.set("");
        try {
            userPort.unpinPost(userId);
            replaceCurrentProfilePinnedPostId(null);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Rebuilds {@link #currentProfileProperty()} with a new pinned post id.
     * Updating currentProfile and not re-fetching the whole profile with UserPort#pinPost is not a risky move since only the user himself can change pinned post.
     * This action would not be suitable for functionalities like followerCount/followingCount
     */
    private void replaceCurrentProfilePinnedPostId(UUID pinnedPostId){
        UserProfileDto p = currentProfile.get();
        if(p == null)
            return;

        currentProfile.set(new UserProfileDto(
                p.id(),
                p.username(),
                p.name(),
                p.email(),
                p.biography(),
                p.dateOfBirth(),
                p.pfpUrl(),
                p.followerCount(),
                p.followingCount(),
                p.createdAt(),
                pinnedPostId
        ));
    }

    /**
     * Deletes the given user via {@link UserPort#deleteUser}.
     */
    public void deleteUser(UUID userId){
        errorMessage.set("");
        try {
            userPort.deleteUser(userId);
            currentProfile.set(new UserProfileDto(
                    null,
                    "",
                    "",
                    "",
                    "",
                    null,
                    null,
                    0,
                    0,
                    null,
                    null
            ));
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    // Getters

    // --- loaded profile getter ---
    public ReadOnlyObjectProperty<UserProfileDto> currentProfileProperty() { return currentProfile; }

    // --- edit form property getters ---
    public StringProperty editNameProperty() { return editName; }
    public StringProperty editBiographyProperty() { return editBiography; }
    public StringProperty editPfpUrlProperty() { return editPfpUrl; }
    public ObjectProperty<LocalDate> editDateOfBirthProperty() { return editDateOfBirth; }

    // --- shared state getter ---
    public StringProperty errorMessageProperty() { return errorMessage; }
}
