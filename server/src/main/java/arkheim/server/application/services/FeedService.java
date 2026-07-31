package arkheim.server.application.services;

import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.domain.entities.PostEntity;
import arkheim.server.domain.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FeedService {
    private final PostRepository postRepository;
    private final PostService postService;

    public FeedService(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    /**
     * Retrieves the home feed timeline for a user (posts from users they follow).
     * @param userId the user retrieving the feed
     * @return List of {@link PostDetail} of posts that users followings have created
     */
    public List<PostDetail> getFollowingsFeed(UUID userId) {
        List<PostEntity> feedPostEntities = postRepository.findFollowingsPosts(userId);

        List<PostDetail> responses = new ArrayList<>();

        for(PostEntity postEntity : feedPostEntities){
            responses.add(postService.getPostDetail(postEntity, userId));
        }

        return responses;
    }

    /**
     * Retrieves a user's profile timeline (posts created by the user).
     * @param requesterId the user who is viewing the timeline (for calculating isLikedByMe/isRepostedByMe)
     * @return List of {@link PostDetail} of created posts
     */
    public List<PostDetail> getUserTimeline(UUID requesterId) {
        List<PostEntity> postEntities = postRepository.getAllPosts();

        List<PostDetail> responses = new ArrayList<>();

        for(PostEntity postEntity : postEntities){
            responses.add(postService.getPostDetail(postEntity, requesterId));
        }

        return responses;
    }

    /**
     * Retrieves a single post's details.
     * @param postId the post to retrieve
     * @param requesterId the user who is viewing the post
     * @return {@link PostDetail} of the target post
     */
    public PostDetail getPostDetails(UUID postId, UUID requesterId) {
        PostEntity postEntity = postRepository.findById(postId);
        return postService.getPostDetail(postEntity, requesterId);
    }

    /**
     * Retrieves replies to a specific post.
     * @param postId the parent post id
     * @param requesterId the user who is viewing the replies
     * @return List of {@link PostDetail} representing replies
     */
    public List<PostDetail> getPostReplies(UUID postId, UUID requesterId) {
        List<PostEntity> replies = postRepository.findReplies(postId);

        List<PostDetail> responses = new ArrayList<>();

        for(PostEntity postEntity : replies){
            responses.add(postService.getPostDetail(postEntity, requesterId));
        }

        return responses;
    }
}
