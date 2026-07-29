package arkheim.server.domain.repository;

import arkheim.server.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface FollowRepository {
    void follow(UUID followerId, UUID followingId);
    void unfollow(UUID followerId, UUID followingId);
    boolean isFollowing(UUID followerId, UUID followingId);
    List<User> findFollowers(UUID userId);
    List<User> findFollowing(UUID userId);
}

