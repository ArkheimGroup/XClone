package arkheim.server.domain.repository;

import arkheim.server.domain.entities.UserEntity;

import java.util.UUID;

public interface UserRepository {
    UserEntity findById(UUID id);
    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
    void save(UserEntity userEntity);
    void updateProfile(UserEntity userEntity);
    void delete(UUID id);

    /**
     * Atomic increment inside DB
     * */
    void incrementFollowerCount(UUID userId);

    /**
     * Atomic decrement inside DB
     * */
    void decrementFollowerCount(UUID userId);

    /**
     * Atomic increment inside DB
     * */
    void incrementFollowingCount(UUID userId);

    /**
     * Atomic decrement inside DB
     * */
    void decrementFollowingCount(UUID userId);

    void updatePinnedPost(UUID userId, UUID postId);
}
