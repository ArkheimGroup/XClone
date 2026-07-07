package arkheim.server.application.services;

import arkheim.server.application.dtos.UpdateProfileRequest;
import arkheim.server.application.dtos.responses.UserProfileResponse;
import arkheim.server.domain.Repository.UserRepository;

import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the profile of a user by their UUID
     * @param userId user's UUID
     * @return UserProfileResponse details
     */
    public UserProfileResponse getUserProfileById(UUID userId) {
        // TODO: Fetch user by id using userRepository, map to UserProfileResponse and return.
        return null;
    }

    /**
     * Retrieves the profile of a user by their unique username
     * @param username user's username
     * @return UserProfileResponse details
     */
    public UserProfileResponse getUserProfileByUsername(String username) {
        // TODO: Fetch user by username using userRepository, map to UserProfileResponse and return.
        return null;
    }

    /**
     * Updates the user's profile details
     * @param request UpdateProfileRequest object containing new profile details
     * @return Updated UserProfileResponse details
     */
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        // TODO: Fetch user, apply changes, call userRepository.updateProfile(user), and return updated response.
        return null;
    }

    /**
     * Pins a specific post to the user's profile.
     * @param userId user's UUID
     * @param postId post's UUID to pin
     */
    public void pinPost(UUID userId, UUID postId) {
        // TODO: Update the user's pinned post using userRepository.updatePinnedPost(userId, postId)
    }

    /**
     * Unpins any pinned post on the user's profile.
     * @param userId user's UUID
     */
    public void unpinPost(UUID userId) {
        // TODO: Set pinned post to null using userRepository.updatePinnedPost(userId, null)
    }

    /**
     * Deletes the user account.
     * @param userId user's UUID
     */
    public void deleteUser(UUID userId) {
        // TODO: Delete user using userRepository.delete(userId)
    }
}
