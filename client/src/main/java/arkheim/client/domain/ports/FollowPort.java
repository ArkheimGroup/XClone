package arkheim.client.domain.ports;

import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.User.response.IsFollowingDto;
import arkheim.client.domain.dtos.User.response.UserDto;

import java.util.List;
import java.util.UUID;

public interface FollowPort {

    /**
     * HTTP: POST /dto/follows/{followerId}/to/{followingId}
     */
    ApiResponse followUser(UUID followerId, UUID followingId);

    /**
     * HTTP: DELETE /dto/follows/{followerId}/to/{followingId}
     */
    ApiResponse unfollowUser(UUID followerId, UUID followingId);

    /**
     * HTTP: GET /dto/follows/{followerId}/checking/{followingId}
     */
    IsFollowingDto isFollowing(UUID followerId, UUID followingId);

    /**
     * HTTP: GET /dto/follows/followers/{userId}
     */
    List<UserDto> getFollowers(UUID userId);

    /**
     * HTTP: GET /dto/follows/following/{userId}
     */
    List<UserDto> getFollowing(UUID userId);
}
