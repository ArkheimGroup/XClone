package arkheim.server.domain.repository;

import arkheim.server.domain.entities.UserEntity;

import java.util.List;
import java.util.UUID;

public interface FollowRepository {
    void follow(UUID followerId, UUID followingId);
    void unfollow(UUID followerId, UUID followingId);
    boolean isFollowing(UUID followerId, UUID followingId);
    List<UserEntity> findFollowers(UUID userId);
    List<UserEntity> findFollowing(UUID userId);
}

