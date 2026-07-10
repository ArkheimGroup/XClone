package arkheim.server.application.services;

import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.domain.entities.User;
import arkheim.server.domain.repository.FollowRepository;
import arkheim.server.domain.repository.UserRepository;

import java.util.ArrayList;
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
        followRepository.follow(followerId, followingId);
        userRepository.incrementFollowingCount(followerId);
        userRepository.incrementFollowerCount(followingId);
    }

    /**
     * Unfollows a user and updates both user follower/following counts.
     * @param followerId user who wants to unfollow
     * @param followingId target user to be unfollowed
     */
    public void unfollowUser(UUID followerId, UUID followingId) {
        followRepository.unfollow(followerId, followingId);
        userRepository.decrementFollowingCount(followerId);
        userRepository.decrementFollowerCount(followingId);
    }

    /**
     * Checks if a user is following another user.
     * @param followerId candidate follower
     * @param followingId candidate following
     * @return true if followerId is following followingId, false otherwise
     */
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepository.isFollowing(followerId, followingId);
    }

    /**
     * Retrieves the list of users following the specified user.
     * @param userId target user id
     * @return List of {@link UserResponse} of the followers
     */
    public List<UserResponse> getFollowers(UUID userId) {
        List<User> followers = followRepository.findFollowers(userId);

        List<UserResponse> responses = new ArrayList<>();
        for(User follower : followers){
            responses.add(new UserResponse(follower));
        }

        return responses;
    }

    /**
     * Retrieves the list of users the specified user is following.
     * @param userId target user id
     * @return List of {@link UserResponse} of users being followed
     */
    public List<UserResponse> getFollowing(UUID userId) {
        List<User> followings = followRepository.findFollowing(userId);

        List<UserResponse> responses = new ArrayList<>();
        for(User following : followings){
            responses.add(new UserResponse(following));
        }

        return responses;
    }
}
