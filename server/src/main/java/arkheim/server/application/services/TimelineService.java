package arkheim.server.application.services;

import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.domain.entities.Media;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;
import arkheim.server.domain.repository.LikeRepository;
import arkheim.server.domain.repository.MediaRepository;
import arkheim.server.domain.repository.PostRepository;
import arkheim.server.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TimelineService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final MediaRepository mediaRepository;

    public TimelineService(PostRepository postRepository, UserRepository userRepository,
                           LikeRepository likeRepository, MediaRepository mediaRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.mediaRepository = mediaRepository;
    }

    /**
     * Retrieves the home feed timeline for a user (posts from users they follow).
     * @param userId the user retrieving the feed
     * @return List of {@link PostResponse} representing the feed
     */
    public List<PostResponse> getHomeFeed(UUID userId) {
        List<Post> feedPosts = postRepository.findFeedForUser(userId);

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : feedPosts){
            responses.add(toPostResponse(post, userId));
        }

        return responses;
    }

    /**
     * Retrieves a user's profile timeline (posts created by the user).
     * @param username the username of the profile owner
     * @param requesterId the user who is viewing the timeline (for calculating isLikedByMe/isRepostedByMe)
     * @return List of {@link PostResponse} representing the user's posts
     */
    public List<PostResponse> getUserTimeline(String username, UUID requesterId) {
        List<Post> posts = postRepository.findByAuthorUsername(username);

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : posts){
            responses.add(toPostResponse(post, requesterId));
        }

        return responses;
    }

    /**
     * Retrieves a single post's details.
     * @param postId the post to retrieve
     * @param requesterId the user who is viewing the post
     * @return {@link PostResponse} of the target post
     */
    public PostResponse getPostDetails(UUID postId, UUID requesterId) {
        Post post = postRepository.findById(postId);
        return toPostResponse(post, requesterId);
    }

    /**
     * Retrieves replies to a specific post.
     * @param postId the parent post id
     * @param requesterId the user who is viewing the replies
     * @return List of {@link PostResponse} representing replies
     */
    public List<PostResponse> getPostReplies(UUID postId, UUID requesterId) {
        List<Post> replies = postRepository.findReplies(postId);

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : replies){
            responses.add(toPostResponse(post, requesterId));
        }

        return responses;
    }

    /**
     * Maps a {@link Post} entity to a {@link PostResponse} DTO,
     * resolving all the data needed (author details, media URLs, like/repost counts, isLikedByMe/isRepostedByMe).
     * @param post the post to map
     * @param requesterId the ID of the user requesting the response (for calculating isLikedByMe/isRepostedByMe)
     * @return a {@link PostResponse} for the given post
     */
    public PostResponse toPostResponse(Post post, UUID requesterId){
        User author = userRepository.findByUsername(post.getAuthorUsername());
        List<Post> reposts = postRepository.findReposts(post.getId());
        List<Media> medias = mediaRepository.findByPostId(post.getId());
        int likeCount = likeRepository.countLikesForPost(post.getId());
        boolean isLikedByMe = likeRepository.isLikedByUser(requesterId, post.getId());
        int repostCount = reposts.size();
        String requesterUsername = userRepository.findById(requesterId).getUsername();
        boolean isRepostedByMe = reposts.stream()
                .anyMatch(r -> Objects.equals(r.getAuthorUsername(), requesterUsername)); // True if a post from reposts is found that has the same username as the requester
        int replyCount = postRepository.findReplies(post.getId()).size();

        return new PostResponse(
                post,
                author,
                medias,
                likeCount,
                repostCount,
                replyCount,
                post.getRepostPostId() != null ? post.getRepostPostId() : post.getReplyPostId(), // The post is eather reply or repost, or none so the value would be null anyway
                isLikedByMe,
                isRepostedByMe
        );
    }
}
