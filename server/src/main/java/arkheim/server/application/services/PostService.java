package arkheim.server.application.services;

import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.application.models.post.CreatePostModel;
import arkheim.server.application.models.post.Post;
import arkheim.server.domain.entities.MediaEntity;
import arkheim.server.domain.entities.PostEntity;
import arkheim.server.domain.entities.UserEntity;
import arkheim.server.domain.exception.ResultCode;
import arkheim.server.domain.exception.ForbiddenException;
import arkheim.server.domain.exception.NotFoundException;
import arkheim.server.domain.repository.*;

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
     * @param username username of the user
     * @return posts posted by the user
     */
    public List<PostDetail> getUserPosts(String username) {
        return getUserPosts(username, null);
    }

    public List<PostDetail> getUserPosts(String username, UUID requesterId) {
        UserEntity user = userRepository.findByUsername(username);
        UUID actualRequesterId = requesterId != null ? requesterId : (user != null ? user.getId() : null);

        List<PostEntity> posts = postRepository.findByAuthorUsername(username);

        List<PostDetail> responses = new ArrayList<>();

        for (PostEntity post : posts) {
            responses.add(getPostDetail(post, actualRequesterId));
        }

        if (user != null && user.getPinnedPostId() != null) {
            UUID pinnedId = user.getPinnedPostId();
            responses.sort((a, b) -> {
                if (a.id().equals(pinnedId)) return -1;
                if (b.id().equals(pinnedId)) return 1;
                return 0;
            });
        }

        return responses;
    }

    /**
     * @param postId id of the post
     * @param requesterId UUID of the user who requested to delete the post
     * */
    public void deletePost(UUID postId, UUID requesterId) {
        PostEntity postEntity = postRepository.findById(postId);
        if (postEntity == null) {
            throw new NotFoundException(ResultCode.POST_NOT_FOUND, "PostEntity not found");
        }

        UserEntity requester = userRepository.findById(requesterId);
        if (requester == null) {
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "UserEntity not found");
        }

        // Verify the requester is the owner of the postEntity
        if (!postEntity.getAuthorUsername().equalsIgnoreCase(requester.getUsername())) {
            throw new ForbiddenException(ResultCode.USER_NOT_AUTHORIZED_TO_DELETE_POST, "UserEntity is not authorized to delete this postEntity");
        }

        // Delete the post itself
        postRepository.delete(postId);
    }

    /**
     * @param newPost {@link CreatePostModel} Request data containing content, media, author, and parent post ID
     * @return {@link PostDetail} of the created post
     */
    public PostDetail createPost(CreatePostModel newPost) {
        UserEntity author = userRepository.findById(newPost.authorId());
        if (author == null) {
            throw new NotFoundException(ResultCode.AUTHOR_NOT_FOUND, "Author user not found");
        }

        UUID replyPostId = null;
        UUID repostPostId = null;

        if (newPost.parentPostId() != null) {
            PostEntity parentPostEntity = postRepository.findById(newPost.parentPostId());
            if (parentPostEntity == null) {
                throw new NotFoundException(ResultCode.PARENT_POST_NOT_FOUND, "Parent postEntity not found");
            }
            // If the content is blank/empty, it's considered a retweet/repost, else it's a comment/reply
            if (newPost.content() == null || newPost.content().trim().isEmpty()) {
                repostPostId = newPost.parentPostId();
            } else {
                replyPostId = newPost.parentPostId();
            }
        }

        // Create the postEntity entity
        PostEntity postEntity = new PostEntity(
                author.getUsername(),
                newPost.content(),
                replyPostId,
                repostPostId
        );

        postRepository.save(postEntity);

        List<String> mediaUrls = new ArrayList<>();
        if (newPost.mediaUrl() != null && !newPost.mediaUrl().trim().isEmpty()) {
            String rawUrls = newPost.mediaUrl().trim();
            String[] splitUrls = rawUrls.split(",");
            for (String urlStr : splitUrls) {
                String url = urlStr.trim();
                if (url.isEmpty()) continue;
                MediaEntity mediaEntity = mediaRepository.findByUrl(url);
                if (mediaEntity == null) {
                    mediaEntity = new MediaEntity(url, 0, 0, 0, author.getId());
                    mediaRepository.save(mediaEntity);
                }
                mediaRepository.linkToPost(postEntity.getId(), mediaEntity.getId());
                mediaUrls.add(url);
            }
        }

        boolean isRepost = repostPostId != null;
        UUID parentPostId = newPost.parentPostId();
        String repliedUsername = null;
        String repostedFromUsername = null;
        String content = postEntity.getDescription();
        if (parentPostId != null) {
            PostEntity parentPost = postRepository.findById(parentPostId);
            if (parentPost != null) {
                if (isRepost) {
                    repostedFromUsername = parentPost.getAuthorUsername();
                    if (content == null || content.isBlank()) {
                        content = parentPost.getDescription();
                    }
                } else {
                    repliedUsername = parentPost.getAuthorUsername();
                }
            }
        }

        return new PostDetail(
                postEntity.getId(),
                author.getId(),
                author.getUsername(),
                author.getName(),
                author.getPfpUrl(),
                author.isVerified(),
                content,
                mediaUrls,
                postEntity.getCreatedAt(),
                0,
                0,
                0,
                parentPostId,
                repliedUsername,
                isRepost,
                repostedFromUsername,
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
        PostEntity postEntity = postRepository.findById(postId);
        if (postEntity == null) {
            throw new NotFoundException(ResultCode.POST_NOT_FOUND, "PostEntity not found");
        }

        UserEntity userEntity = userRepository.findById(userId);
        if (userEntity == null) {
            throw new NotFoundException(ResultCode.USER_NOT_FOUND, "UserEntity not found");
        }

        if (likeRepository.isLikedByUser(userId, postId)) {
            likeRepository.unlike(userId, postId);
        } else {
            likeRepository.like(userId, postId);
        }
    }

    /**
     * searches through all the posts and finds ones containing a certain word in their description.
     * @param word the word to be found in posts
     * @param requesterId UUID of the user who requested to find the posts
     * @return a List of {@link Post} containing the received word
     */
    public List<PostDetail> findPostsByWord(String word, UUID requesterId){
        List<PostEntity> postEntities = postRepository.findByWord(word);

        List<PostDetail> responses = new ArrayList<>();

        for(PostEntity postEntity : postEntities){
            responses.add(getPostDetail(postEntity, requesterId));
        }

        return responses;
    }

    /**
     * Maps a {@link PostEntity} entity to a {@link PostDetail} DTO,
     * resolving all the data needed (author details, media URLs, like/repost counts, isLikedByMe/isRepostedByMe).
     * @param postEntity the postEntity to map
     * @param requesterId the ID of the user requesting the response (for calculating isLikedByMe/isRepostedByMe)
     * @return a {@link PostDetail} for the given postEntity
     */
    public PostDetail getPostDetail(PostEntity postEntity, UUID requesterId) {
        UserEntity author = userRepository.findByUsername(postEntity.getAuthorUsername());
        List<PostEntity> reposts = postRepository.findReposts(postEntity.getId());
        List<MediaEntity> medias = mediaRepository.findByPostId(postEntity.getId());
        int likeCount = likeRepository.countLikesForPost(postEntity.getId());
        boolean isLikedByMe = likeRepository.isLikedByUser(requesterId, postEntity.getId());
        int repostCount = reposts.size();
        UserEntity requester = requesterId != null ? userRepository.findById(requesterId) : null;
        String requesterUsername = requester != null ? requester.getUsername() : null;
        boolean isRepostedByMe = requesterUsername != null && reposts.stream()
                .anyMatch(r -> Objects.equals(r.getAuthorUsername(), requesterUsername));
        int replyCount = postRepository.findReplies(postEntity.getId()).size();

        boolean isRepost = postEntity.getRepostPostId() != null;
        UUID parentPostId = isRepost ? postEntity.getRepostPostId() : postEntity.getReplyPostId();
        String repliedUsername = null;
        String repostedFromUsername = null;
        String content = postEntity.getDescription();
        if (parentPostId != null) {
            PostEntity parentPost = postRepository.findById(parentPostId);
            if (parentPost != null) {
                if (isRepost) {
                    repostedFromUsername = parentPost.getAuthorUsername();
                    if (content == null || content.isBlank()) {
                        content = parentPost.getDescription();
                    }
                    if (medias.isEmpty()) {
                        medias = mediaRepository.findByPostId(parentPost.getId());
                    }
                } else {
                    repliedUsername = parentPost.getAuthorUsername();
                }
            }
        }

        return new PostDetail(
                postEntity.getId(),
                author.getId(),
                author.getUsername(),
                author.getName(),
                author.getPfpUrl(),
                author.isVerified(),
                content,
                medias.stream().map(MediaEntity::getUrl).toList(),
                postEntity.getCreatedAt(),
                likeCount,
                repostCount,
                replyCount,
                parentPostId,
                repliedUsername,
                isRepost,
                repostedFromUsername,
                isLikedByMe,
                isRepostedByMe
        );

    }
}
