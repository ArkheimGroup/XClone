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
import arkheim.server.domain.entities.Hashtag;
import arkheim.server.domain.repository.HashtagRepository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostService {
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final HashtagRepository hashtagRepository;

    public PostService(PostRepository postRepository,
                       MediaRepository mediaRepository,
                       UserRepository userRepository,
                       LikeRepository likeRepository,
                       HashtagRepository hashtagRepository) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.hashtagRepository = hashtagRepository;
    }

    /**
     * @param username username of the user
     * @return posts posted by the user
     */
    public List<PostResponse> getUserPosts(String username){
        List<Post> posts;
        posts = postRepository.findByAuthorUsername(username);

        List<PostResponse> responses = new ArrayList<>();

        User user = userRepository.findByUsername(username);
        UUID requesterId = user.getId();

        for(Post post : posts){
            User author = userRepository.findByUsername(post.getAuthorUsername());
            List<Post> reposts = postRepository.findReposts(post.getId());
            List<Media> medias = mediaRepository.findByPostId(post.getId());
            int likeCount = likeRepository.countLikesForPost(post.getId());
            boolean isLikedByMe = likeRepository.isLikedByUser(requesterId, post.getId());
            User requester = requesterId != null ? userRepository.findById(requesterId) : null;
            String requesterUsername = requester != null ? requester.getUsername() : null;
            boolean isRepostedByMe = requesterUsername != null && reposts.stream()
                    .anyMatch(r -> Objects.equals(r.getAuthorUsername(), requesterUsername));
            int repostCount = reposts.size();
            int replyCount = postRepository.findReplies(post.getId()).size();

            boolean isRepost = post.getRepostPostId() != null;
            UUID parentPostId = isRepost ? post.getRepostPostId() : post.getReplyPostId();
            String repliedUsername = null;
            String repostedFromUsername = null;
            if (parentPostId != null) {
                Post parentPost = postRepository.findById(parentPostId);
                if (parentPost != null) {
                    if (isRepost) {
                        repostedFromUsername = parentPost.getAuthorUsername();
                    } else {
                        repliedUsername = parentPost.getAuthorUsername();
                    }
                }
            }

            responses.add(new PostResponse(
                    post,
                    author,
                    medias,
                    likeCount,
                    repostCount,
                    replyCount,
                    parentPostId,
                    repliedUsername,
                    isRepost,
                    repostedFromUsername,
                    isLikedByMe,
                    isRepostedByMe
            ));
        }

        return responses;
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
        if (!post.getAuthorUsername().equalsIgnoreCase(requester.getUsername())) {
            throw new ForbiddenException(ErrorCode.USER_NOT_AUTHORIZED_TO_DELETE_POST, "User is not authorized to delete this post");
        }

        // Delete the post (cascading cleanup handled by repository)
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

        boolean isRepost = repostPostId != null;
        UUID parentPostId = createPostRequest.parentPostId();
        String repliedUsername = null;
        String repostedFromUsername = null;
        if (parentPostId != null) {
            Post parentPost = postRepository.findById(parentPostId);
            if (parentPost != null) {
                if (isRepost) {
                    repostedFromUsername = parentPost.getAuthorUsername();
                } else {
                    repliedUsername = parentPost.getAuthorUsername();
                }
            }
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
        if (word == null || word.isBlank()) {
            return Collections.emptyList();
        }

        String cleanWord = word.trim();
        if (cleanWord.startsWith("#")) {
            cleanWord = cleanWord.substring(1).trim();
        }

        List<Post> postsByWord = postRepository.findByWord(cleanWord);
        List<Post> postsByHashtag = hashtagRepository != null ? hashtagRepository.findPostsByHashtag(cleanWord) : Collections.emptyList();

        Map<UUID, Post> postMap = new LinkedHashMap<>();
        for (Post p : postsByWord) {
            postMap.put(p.getId(), p);
        }
        for (Post p : postsByHashtag) {
            postMap.putIfAbsent(p.getId(), p);
        }

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : postMap.values()){
            User author = userRepository.findByUsername(post.getAuthorUsername());
            List<Post> reposts = postRepository.findReposts(post.getId());
            List<Media> medias = mediaRepository.findByPostId(post.getId());
            int likeCount = likeRepository.countLikesForPost(post.getId());
            boolean isLikedByMe = requesterId != null && likeRepository.isLikedByUser(requesterId, post.getId());
            User requester = requesterId != null ? userRepository.findById(requesterId) : null;
            String requesterUsername = requester != null ? requester.getUsername() : null;
            boolean isRepostedByMe = requesterUsername != null && reposts.stream()
                    .anyMatch(r -> Objects.equals(r.getAuthorUsername(), requesterUsername));
            int repostCount = reposts.size();
            int replyCount = postRepository.findReplies(post.getId()).size();

            boolean isRepost = post.getRepostPostId() != null;
            UUID parentPostId = isRepost ? post.getRepostPostId() : post.getReplyPostId();
            String repliedUsername = null;
            String repostedFromUsername = null;
            if (parentPostId != null) {
                Post parentPost = postRepository.findById(parentPostId);
                if (parentPost != null) {
                    if (isRepost) {
                        repostedFromUsername = parentPost.getAuthorUsername();
                    } else {
                        repliedUsername = parentPost.getAuthorUsername();
                    }
                }
            }

            responses.add(new PostResponse(
                    post,
                    author,
                    medias,
                    likeCount,
                    repostCount,
                    replyCount,
                    parentPostId,
                    repliedUsername,
                    isRepost,
                    repostedFromUsername,
                    isLikedByMe,
                    isRepostedByMe
            ));
        }

        return responses;
    }
}
