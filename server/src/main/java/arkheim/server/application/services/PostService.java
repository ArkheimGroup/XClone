package arkheim.server.application.services;

import arkheim.server.application.dtos.CreatePostRequest;
import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.domain.entities.Media;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;
import arkheim.server.application.exception.ErrorCode;
import arkheim.server.application.exception.ForbiddenException;
import arkheim.server.application.exception.NotFoundException;
import arkheim.server.domain.repository.LikeRepository;
import arkheim.server.domain.repository.MediaRepository;
import arkheim.server.domain.repository.PostRepository;
import arkheim.server.domain.repository.UserRepository;

import java.util.*;

public class PostService {
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository,
                       MediaRepository mediaRepository,
                       UserRepository userRepository,
                       LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    /**
     * @param postId id of the post
     * @param requesterId UUID of the user who requested to delete the post
     * */
    public void deletePost(UUID postId, UUID requesterId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new NotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found");
        }

        User requester = userRepository.findById(requesterId);
        if (requester == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        // Verify the requester is the owner of the post
        if (!post.getAuthorUsername().equals(requester.getUsername())) {
            throw new ForbiddenException(ErrorCode.USER_NOT_AUTHORIZED_TO_DELETE_POST, "User is not authorized to delete this post");
        }

        // Unlink the post's media
        List<Media> mediaList = mediaRepository.findByPostId(postId);
        if (mediaList != null) {
            for (Media media : mediaList) {
                mediaRepository.unLinkFromPost(postId, media.getId());
            }
        }

        // Delete reposts of this post
        List<Post> reposts = postRepository.findReposts(postId);
        if (reposts != null) {
            for (Post repost : reposts) {
                postRepository.delete(repost.getId());
            }
        }

        // Delete the post itself
        postRepository.delete(postId);
    }

    /**
     * @param createPostRequest Request data containing content, media, author, and parent post ID
     * @return {@link PostResponse} of the created post
     */
    public PostResponse createPost(CreatePostRequest createPostRequest) {
        User author = userRepository.findById(createPostRequest.authorId());
        if (author == null) {
            throw new NotFoundException(ErrorCode.AUTHOR_NOT_FOUND, "Author user not found");
        }

        UUID replyPostId = null;
        UUID repostPostId = null;

        if (createPostRequest.parentPostId() != null) {
            Post parentPost = postRepository.findById(createPostRequest.parentPostId());
            if (parentPost == null) {
                throw new NotFoundException(ErrorCode.PARENT_POST_NOT_FOUND, "Parent post not found");
            }
            // If the content is blank/empty, it's considered a retweet/repost, else it's a comment/reply
            if (createPostRequest.content() == null || createPostRequest.content().trim().isEmpty()) {
                repostPostId = createPostRequest.parentPostId();
            } else {
                replyPostId = createPostRequest.parentPostId();
            }
        }

        // Create the post entity
        Post post = new Post(
                author.getUsername(),
                createPostRequest.content(),
                replyPostId,
                repostPostId
        );

        postRepository.save(post);

        List<String> mediaUrls = Collections.emptyList();
        if (createPostRequest.mediaUrl() != null && !createPostRequest.mediaUrl().trim().isEmpty()) {
            Media media = new Media(createPostRequest.mediaUrl(), 0, 0, 0, author.getId());
            mediaRepository.save(media);
            mediaRepository.linkToPost(post.getId(), media.getId());
            mediaUrls = List.of(createPostRequest.mediaUrl());
        }

        return new PostResponse(
                post.getId(),
                author.getId(),
                author.getUsername(),
                author.getName(),
                author.getPfpUrl(),
                post.getDescription(),
                mediaUrls,
                post.getCreatedAt(),
                0,
                0,
                0,
                createPostRequest.parentPostId(),
                false,
                false
        );
    }

    /**
     * Toggles the like status of a user on a post.
     * @param userId UUID of the user
     * @param postId UUID of the post
     */
    public void toggleLike(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new NotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found");
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
        }

        if (likeRepository.isLikedByUser(userId, postId)) {
            likeRepository.unlike(userId, postId);
        } else {
            likeRepository.like(userId, postId);
        }
    }

    /**
     * searchs throw all the posts and finds ones containing a curtain word in their description.
     * @param word the word to be found in posts
     * @param requesterId UUID of the user who requested to find the posts
     * @return a List of {@link PostResponse} containing the received word
     */
    public List<PostResponse> findPostsByWord(String word, UUID requesterId){
        List<Post> posts = postRepository.findByWord(word);

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : posts){
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

            responses.add(new PostResponse(
                    post,
                    author,
                    medias,
                    likeCount,
                    repostCount,
                    replyCount,
                    post.getRepostPostId() != null ? post.getRepostPostId() : post.getReplyPostId(), // The post is eather reply or repost, or none so the value would be null anyway
                    isLikedByMe,
                    isRepostedByMe
            ));
        }

        return responses;
    }
}
