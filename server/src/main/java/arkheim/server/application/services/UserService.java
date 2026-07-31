package arkheim.server.application.services;

import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.User;
import arkheim.server.domain.entities.UserEntity;
import arkheim.server.domain.exception.ResultCode;
import arkheim.server.domain.exception.NotFoundException;
import arkheim.server.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userMapper = new UserMapper();
    }

    /**
     * Retrieves the profile of a user by their UUID
     * @param userId user's UUID
     * @return {@link User} details
     */
    public User getUserProfileById(UUID userId) {
        // Fetch userEntity by id.
        UserEntity userEntity = userRepository.findById(userId);
        if(userEntity == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        return userMapper.map(userEntity);
    }

    /**
     * Retrieves the profile of a user by their unique username
     * @param username user's username
     * @return {@link User} details
     */
    public User getUserProfileByUsername(String username) {
        // Fetch userEntity by username.
        UserEntity userEntity = userRepository.findByUsername(username);
        if(userEntity == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        return userMapper.map(userEntity);
    }

    /**
     * Updates the user's profile details
     * @param newUser {@link User} object containing new profile details
     * @return Updated {@link User} details
     */
    public User updateProfile(User newUser) {
        // Fetch userEntity by id
        UserEntity userEntity = userRepository.findById(newUser.id());
        if(userEntity == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        // Update userEntity
        String pfpUrl = (newUser.pfpUrl() != null && !newUser.pfpUrl().isBlank()) ? newUser.pfpUrl() : userEntity.getPfpUrl(); // this approach would only update the actual updated fields
        String bannerUrl = (newUser.bannerUrl() != null && !newUser.bannerUrl().isBlank()) ? newUser.bannerUrl() : userEntity.getPfpUrl();
        String name = (newUser.name() != null && !newUser.name().isBlank()) ? newUser.name() : userEntity.getName();
        String biography = newUser.biography() != null ? newUser.biography() : userEntity.getBiography();
        LocalDateTime dateOfBirth = newUser.dateOfBirth() != null ? newUser.dateOfBirth() : userEntity.getDateOfBirth();
        boolean isVerified = newUser.isVerified();

        UserEntity updatedUserEntity = new UserEntity(
                newUser.id(),
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                name,
                userEntity.getEmail(),
                biography,
                userEntity.getCreatedAt(),
                pfpUrl,
                bannerUrl,
                userEntity.getFollowerCount(),
                userEntity.getFollowingCount(),
                userEntity.getPinnedPostId(),
                dateOfBirth,
                isVerified
        );
        userRepository.updateProfile(updatedUserEntity);

        return userMapper.map(updatedUserEntity);
    }

    /**
     * Pins a specific post to the user's profile.
     * @param userId user's UUID
     * @param postId post's UUID to pin
     */
    public void pinPost(UUID userId, UUID postId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.updatePinnedPost(userId, postId);
    }

    /**
     * Unpins any pinned post on the user's profile.
     * @param userId user's UUID
     */
    public void unpinPost(UUID userId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.updatePinnedPost(userId, null);
    }

    /**
     * Deletes the user account.
     * @param userId user's UUID
     */
    public void deleteUser(UUID userId) {
        if(userRepository.findById(userId) == null){
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "User not found");
        }

        userRepository.delete(userId);
    }
}
