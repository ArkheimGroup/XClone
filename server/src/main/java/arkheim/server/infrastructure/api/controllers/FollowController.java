package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.ApiResponse;
import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.User.dtos.GetIsFollowingDto;
import arkheim.server.application.features.User.dtos.GetUserDto;
import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.MinimalUser;
import arkheim.server.application.services.FollowUserService;
import arkheim.server.domain.exception.ResultCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for user follow relationship operations (following, unfollowing, follow status checks, and lists).
 */
@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowUserService followUserService;
    private final UserMapper userMapper;

    public FollowController(FollowUserService followUserService) {
        this.followUserService = followUserService;
        this.userMapper = new UserMapper();
    }

    /**
     * Follows a target user.
     * HTTP Method: POST
     * Endpoint: /api/follows/{followerId}/to/{followingId}
     * @param followerId the UUID of the user who wants to follow
     * @param followingId the UUID of the target user to be followed
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @PostMapping("/{followerId}/to/{followingId}")
    public ResponseEntity<ApiResponse> followUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        followUserService.followUser(followerId, followingId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.FOLLOWED_USER));
    }

    /**
     * Unfollows a target user.
     * HTTP Method: DELETE
     * Endpoint: /api/follows/{followerId}/to/{followingId}
     * @param followerId the UUID of the user who wants to unfollow
     * @param followingId the UUID of the target user to be unfollowed
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @DeleteMapping("/{followerId}/to/{followingId}")
    public ResponseEntity<ApiResponse> unfollowUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        followUserService.unfollowUser(followerId, followingId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.UNFOLLOWED_USER));
    }

    /**
     * Checks if a user is currently following another user.
     * HTTP Method: GET
     * Endpoint: /api/follows/{followerId}/checking/{followingId}
     * @param followerId the candidate follower UUID
     * @param followingId the candidate following UUID
     * @return {@link ResponseEntity<GenericApiResponse>} containing {@link GetIsFollowingDto} which simply contains a boolean (true if following and false if otherwise)
     */
    @GetMapping("/{followerId}/checking/{followingId}")
    public ResponseEntity<GenericApiResponse<GetIsFollowingDto>> isFollowing(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        boolean isFollowing = followUserService.isFollowing(followerId, followingId);

        GetIsFollowingDto response = new GetIsFollowingDto(isFollowing);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.DATA_RETRIEVED, response));
    }

    /**
     * Retrieves the list of users following the specified user.
     * HTTP Method: GET
     * Endpoint: /api/follows/followers/{userId}
     * @param userId the target user UUID
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link GetUserDto} of the followers
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<GenericApiResponse<List<GetUserDto>>> getFollowers(@PathVariable UUID userId) {
        List<MinimalUser> followers = followUserService.getFollowers(userId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.USER_RETRIEVED, followers.stream().map(userMapper::map).toList()));
    }

    /**
     * Retrieves the list of users the specified user is following.
     * HTTP Method: GET
     * Endpoint: /api/follows/following/{userId}
     * @param userId the target user UUID
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link GetUserDto} representing followed users
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<GenericApiResponse<List<GetUserDto>>> getFollowing(@PathVariable UUID userId) {
        List<MinimalUser> following = followUserService.getFollowing(userId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.USER_RETRIEVED, following.stream().map(userMapper::map).toList()));
    }
}
