package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.application.services.FollowUserService;
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

    public FollowController(FollowUserService followUserService) {
        this.followUserService = followUserService;
    }

    /**
     * Follows a target user.
     * HTTP Method: POST
     * Endpoint: /api/follows/{followerId}/to/{followingId}
     * @param followerId the UUID of the user who wants to follow
     * @param followingId the UUID of the target user to be followed
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @PostMapping("/{followerId}/to/{followingId}")
    public ResponseEntity<Void> followUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        followUserService.followUser(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unfollows a target user.
     * HTTP Method: DELETE
     * Endpoint: /api/follows/{followerId}/to/{followingId}
     * @param followerId the UUID of the user who wants to unfollow
     * @param followingId the UUID of the target user to be unfollowed
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @DeleteMapping("/{followerId}/to/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        followUserService.unfollowUser(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    /**
     * Checks if a user is currently following another user.
     * HTTP Method: GET
     * Endpoint: /api/follows/{followerId}/checking/{followingId}
     * @param followerId the candidate follower UUID
     * @param followingId the candidate following UUID
     * @return {@link ResponseEntity} containing a boolean (true if following, false otherwise)
     */
    @GetMapping("/{followerId}/checking/{followingId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        boolean isFollowing = followUserService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(isFollowing);
    }

    /**
     * Retrieves the list of users following the specified user.
     * HTTP Method: GET
     * Endpoint: /api/follows/followers/{userId}
     * @param userId the target user UUID
     * @return {@link ResponseEntity} containing a list of {@link UserResponse} of the followers
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable UUID userId) {
        List<UserResponse> followers = followUserService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Retrieves the list of users the specified user is following.
     * HTTP Method: GET
     * Endpoint: /api/follows/following/{userId}
     * @param userId the target user UUID
     * @return {@link ResponseEntity} containing a list of {@link UserResponse} representing followed users
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable UUID userId) {
        List<UserResponse> following = followUserService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
