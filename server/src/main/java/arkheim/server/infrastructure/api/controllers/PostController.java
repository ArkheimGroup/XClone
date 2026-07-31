package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.ApiResponse;
import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.Post.commands.CreatePostCommand;
import arkheim.server.application.features.Post.dtos.GetPostDto;
import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.application.features.Post.mapper.PostMapper;
import arkheim.server.application.services.HashtagService;
import arkheim.server.application.services.PostService;
import arkheim.server.application.services.FeedService;
import arkheim.server.domain.exception.ResultCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing post creations, deletions, likes, and timelines.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final FeedService feedService;
    private final PostMapper postMapper;
    private final HashtagService hashtagService;

    public PostController(PostService postService, FeedService feedService, HashtagService hashtagService) {
        this.postService = postService;
        this.feedService = feedService;
        this.hashtagService = hashtagService;
        this.postMapper = new PostMapper();
    }

    /**
     * Creates a new post and automatically processes any hashtags inside the post description.
     * HTTP Method: POST
     * Endpoint: /api/posts
     * @param createPostCommand the payload containing post details (author ID, content, media URL, parent post ID)
     * @return {@link ResponseEntity} containing {@link GenericApiResponse<GetPostDto>} of the created post
     */
    @PostMapping
    public ResponseEntity<GenericApiResponse<GetPostDto>> createPost(@RequestBody CreatePostCommand createPostCommand) {
        GetPostDto postDto = postMapper.map(postService.createPost(postMapper.map(createPostCommand)));

        // Process hashtags from the post content
        hashtagService.processHashtagsForPost(postDto.id(), createPostCommand.content());

        GenericApiResponse<GetPostDto> response = GenericApiResponse.success(ResultCode.POST_CREATED, postDto);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific post by its UUID.
     * HTTP Method: DELETE
     * Endpoint: /api/posts/{postId}
     * @param postId the UUID of the post to delete
     * @param requesterId the UUID of the user requesting deletion (ownership verification is checked)
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        postService.deletePost(postId, requesterId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.POST_DELETED));
    }

    /**
     * Toggles a user's like status on a specific post (likes if not liked, otherwise unlikes).
     * HTTP Method: POST
     * Endpoint: /api/posts/{postId}/like
     * @param postId the UUID of the post to like/unlike
     * @param userId the UUID of the user performing the action
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse> toggleLike(@PathVariable UUID postId, @RequestParam UUID userId) {
        postService.toggleLike(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.LIKE_TOGGLED));
    }

    /**
     * Retrieves a specific user's timeline (reposts and posts created by them).
     * HTTP Method: GET
     * Endpoint: /api/posts/timeline
     * @param requesterId the UUID of the user viewing the timeline (for status context check)
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link PostDetail} representing the user timeline
     */
    @GetMapping("/timeline")
    public ResponseEntity<GenericApiResponse<List<PostDetail>>> getUserTimeline(@RequestParam UUID requesterId) {
        List<PostDetail> timeline = feedService.getUserTimeline(requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, timeline));
    }

    /**
     * Retrieves the full details of a specific post.
     * HTTP Method: GET
     * Endpoint: /api/posts/{postId}
     * @param postId the UUID of the post
     * @param requesterId the UUID of the user retrieving details
     * @return {@link ResponseEntity} containing the {@link GenericApiResponse<PostDetail>}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<GenericApiResponse<PostDetail>> getPostDetails(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        PostDetail details = feedService.getPostDetails(postId, requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, details));
    }

    /**
     * Retrieves replies/comments attached to a specific post.
     * HTTP Method: GET
     * Endpoint: /api/posts/{postId}/replies
     * @param postId the UUID of the parent post
     * @param requesterId the UUID of the user retrieving replies
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link PostDetail} representing the replies
     */
    @GetMapping("/{postId}/replies")
    public ResponseEntity<GenericApiResponse<List<PostDetail>>> getPostReplies(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        List<PostDetail> replies = feedService.getPostReplies(postId, requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, replies));
    }


    /**
     * Retrieves any posts containing the word
     * HTTP Method: GET
     * Endpoint: /api/posts/byword/{word}
     * @param requesterId the UUID of the user retrieving replies
     * @param word searching word
     * @return {@link ResponseEntity} containing a list of {@link GetPostDto} representing the posts
     */
    @GetMapping("/byword/{word}")
    public ResponseEntity<GenericApiResponse<List<PostDetail>>> getPostsByWord(@PathVariable String word, @RequestParam(required = false) UUID requesterId){
        List<PostDetail> posts = postService.findPostsByWord(word, requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, posts));
    }

    /**
     * Retrieves every post authored by user
     * @param username username of the user requesting for its posts
     * HTTP Method: GET
     * Endpoint: /api/posts/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<GenericApiResponse<List<PostDetail>>> getUserPosts(@PathVariable String username, @RequestParam(required = false) UUID requesterId){
        List<PostDetail> posts = postService.getUserPosts(username, requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, posts));
    }
}
