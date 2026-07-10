package arkheim.server.application.services;

import arkheim.server.application.dtos.CreatePostRequest;
import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.domain.entities.Media;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;
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
            throw new NoSuchElementException("Post not found");
        }

        User requester = userRepository.findById(requesterId);
        if (requester == null) {
            throw new NoSuchElementException("User not found");
        }

        // Verify the requester is the owner of the post
        if (!post.getAuthorUsername().equals(requester.getUsername())) {
            throw new SecurityException("User is not authorized to delete this post");
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
            throw new NoSuchElementException("Author user not found");
        }

        UUID replyPostId = null;
        UUID repostPostId = null;

        if (createPostRequest.parentPostId() != null) {
            Post parentPost = postRepository.findById(createPostRequest.parentPostId());
            if (parentPost == null) {
                throw new NoSuchElementException("Parent post not found");
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
            throw new NoSuchElementException("Post not found");
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }

        if (likeRepository.isLikedByUser(userId, postId)) {
            likeRepository.unlike(userId, postId);
        } else {
            likeRepository.like(userId, postId);
        }
    }
}
