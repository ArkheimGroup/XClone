package arkheim.server.domain.repository;

import arkheim.server.domain.entities.UserEntity;

import java.util.List;
import java.util.UUID;

public interface LikeRepository {
    void like(UUID userId, UUID postId);
    void unlike(UUID userId, UUID postId);
    boolean isLikedByUser(UUID userId, UUID postId);
    List<UserEntity> findUsersWhoLiked(UUID postId);
    int countLikesForPost(UUID postId);
}
