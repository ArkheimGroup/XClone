package arkheim.server.application.services;

import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.domain.Repository.FollowRepository;
import arkheim.server.domain.Repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class FollowUserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public FollowUserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * Follows a user and updates both users' follower/following counts.
     * @param followerId user who wants to follow
     * @param followingId target user to be followed
     */
    public void followUser(UUID followerId, UUID followingId) {
        // TODO: Follow target user using followRepository.
        //  Also increment following count for followerId and follower count for followingId in userRepository.
    }

    /**
     * Unfollows a user and updates both user follower/following counts.
     * @param followerId user who wants to unfollow
     * @param followingId target user to be unfollowed
     */
    public void unfollowUser(UUID followerId, UUID followingId) {
        // TODO: Unfollow target user using followRepository.
        //  Also decrement following count for followerId and follower count for followingId in userRepository.
    }

    /**
     * Checks if a user is following another user.
     * @param followerId candidate follower
     * @param followingId candidate following
     * @return true if followerId is following followingId, false otherwise
     */
    public boolean isFollowing(UUID followerId, UUID followingId) {
        // TODO: Check if followerId is following followingId using followRepository
        return false;
    }

    /**
     * Retrieves the list of users following the specified user.
     * @param userId target user id
     * @return List of UserResponse of the followers
     */
    public List<UserResponse> getFollowers(UUID userId) {
        // TODO: Find followers using followRepository and map them to UserResponse DTOs
        return null;
    }

    /**
     * Retrieves the list of users the specified user is following.
     * @param userId target user id
     * @return List of UserResponse of users being followed
     */
    public List<UserResponse> getFollowing(UUID userId) {
        // TODO: Find following using followRepository and map them to UserResponse DTOs
        return null;
    }
}
