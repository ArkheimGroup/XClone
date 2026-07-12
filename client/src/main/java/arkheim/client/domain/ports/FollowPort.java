package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.UserDto;

import java.util.List;
import java.util.UUID;

public interface FollowPort {

    /**
     * HTTP: POST /api/follows/{followerId}/to/{followingId}
     */
    void followUser(UUID followerId, UUID followingId);

    /**
     * HTTP: DELETE /api/follows/{followerId}/to/{followingId}
     */
    void unfollowUser(UUID followerId, UUID followingId);

    /**
     * HTTP: GET /api/follows/{followerId}/checking/{followingId}
     */
    boolean isFollowing(UUID followerId, UUID followingId);

    /**
     * HTTP: GET /api/follows/followers/{userId}
     */
    List<UserDto> getFollowers(UUID userId);

    /**
     * HTTP: GET /api/follows/following/{userId}
     */
    List<UserDto> getFollowing(UUID userId);
}
