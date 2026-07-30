package arkheim.server.application.services;

import arkheim.server.application.dtos.UpdateProfileRequest;
import arkheim.server.application.dtos.responses.UserProfileResponse;
import arkheim.server.domain.entities.User;
import arkheim.server.application.exception.ErrorCode;
import arkheim.server.application.exception.NotFoundException;
import arkheim.server.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the profile of a user by their UUID
     * @param userId user's UUID
     * @return {@link UserProfileResponse} details
     */
    public UserProfileResponse getUserProfileById(UUID userId) {
        // Fetch user by id.
        User user = userRepository.findById(userId);
        if(user == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        return new UserProfileResponse(user);
    }

    /**
     * Retrieves the profile of a user by their unique username
     * @param username user's username
     * @return {@link UserProfileResponse} details
     */
    public UserProfileResponse getUserProfileByUsername(String username) {
        // Fetch user by username.
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        return new UserProfileResponse(user);
    }

    /**
     * Updates the user's profile details
     * @param request {@link UpdateProfileRequest} object containing new profile details
     * @return Updated {@link UserProfileResponse} details
     */
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        // Fetch user by id
        User user = userRepository.findById(request.userId());
        if(user == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        // Update user
        String pfpUrl = (request.pfpUrl() != null && !request.pfpUrl().isBlank()) ? request.pfpUrl() : user.getPfpUrl(); // this approach would only update the actual updated fields
        String bannerUrl = (request.bannerUrl() != null && !request.bannerUrl().isBlank()) ? request.bannerUrl() : user.getPfpUrl();
        String name = (request.name() != null && !request.name().isBlank()) ? request.name() : user.getName();
        String biography = request.biography() != null ? request.biography() : user.getBiography();
        LocalDateTime datOfBirth = request.dateOfBirth() != null ? request.dateOfBirth() : user.getDateOfBirth();
        boolean isVerified = request.isVerified();

        User updatedUser = new User(
                request.userId(),
                user.getUsername(),
                user.getPasswordHash(),
                name,
                user.getEmail(),
                biography,
                user.getCreatedAt(),
                pfpUrl,
                bannerUrl,
                user.getFollowerCount(),
                user.getFollowingCount(),
                user.getPinnedPostId(),
                datOfBirth,
                isVerified
        );
        userRepository.updateProfile(updatedUser);

        return new UserProfileResponse(updatedUser);
    }

    /**
     * Pins a specific post to the user's profile.
     * @param userId user's UUID
     * @param postId post's UUID to pin
     */
    public void pinPost(UUID userId, UUID postId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.updatePinnedPost(userId, postId);
    }

    /**
     * Unpins any pinned post on the user's profile.
     * @param userId user's UUID
     */
    public void unpinPost(UUID userId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.updatePinnedPost(userId, null);
    }

    /**
     * Deletes the user account.
     * @param userId user's UUID
     */
    public void deleteUser(UUID userId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.delete(userId);
    }
}
