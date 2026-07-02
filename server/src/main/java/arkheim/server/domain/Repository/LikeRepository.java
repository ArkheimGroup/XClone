package arkheim.server.domain.Repository;

import arkheim.server.domain.Entities.User;

import java.util.List;
import java.util.UUID;

public interface LikeRepository {
    void like(UUID userId, UUID postId);
    void unlike(UUID userId, UUID postId);
    boolean isLikedByUser(UUID userId, UUID postId);
    List<User> findUsersWhoLiked(UUID postId);
    int countLikesForPost(UUID postId);
}
