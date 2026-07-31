package arkheim.server.application.services;

import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.MinimalUser;
import arkheim.server.domain.entities.UserEntity;
import arkheim.server.domain.repository.FollowRepository;
import arkheim.server.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FollowUserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;

    public FollowUserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.userMapper = new UserMapper();
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
     * @return List of {@link MinimalUser} of the followers
     */
    public List<MinimalUser> getFollowers(UUID userId) {
        List<UserEntity> followers = followRepository.findFollowers(userId);

        List<MinimalUser> responses = new ArrayList<>();
        for(UserEntity follower : followers){
            responses.add(userMapper.mapToMinimalUser(follower));
        }

        return responses;
    }

    /**
     * Retrieves the list of users the specified user is following.
     * @param userId target user id
     * @return List of {@link MinimalUser} of users being followed
     */
    public List<MinimalUser> getFollowing(UUID userId) {
        List<UserEntity> followings = followRepository.findFollowing(userId);

        List<MinimalUser> responses = new ArrayList<>();
        for(UserEntity following : followings){
            responses.add(userMapper.mapToMinimalUser(following));
        }

        return responses;
    }
}
