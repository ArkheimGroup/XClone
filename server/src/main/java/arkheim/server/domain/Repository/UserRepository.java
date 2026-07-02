package arkheim.server.domain.Repository;

import arkheim.server.domain.Entities.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    User findById(UUID id);
    User findByUsername(String username);
    User findByEmail(String email);
    void save(User user);
    void updateProfile(User user);
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
