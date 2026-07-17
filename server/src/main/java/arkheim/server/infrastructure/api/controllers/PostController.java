package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.CreatePostRequest;
import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.application.services.HashtagService;
import arkheim.server.application.services.PostService;
import arkheim.server.application.services.FeedService;
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
    private final HashtagService hashtagService;

    public PostController(PostService postService, FeedService feedService, HashtagService hashtagService) {
        this.postService = postService;
        this.feedService = feedService;
        this.hashtagService = hashtagService;
    }

    /**
     * Creates a new post and automatically processes any hashtags inside the post description.
     * HTTP Method: POST
     * Endpoint: /api/posts
     * @param createPostRequest the payload containing post details (author ID, content, media URL, parent post ID)
     * @return {@link ResponseEntity} containing {@link PostResponse} of the created post
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest createPostRequest) {
        PostResponse response = postService.createPost(createPostRequest);
        // Process hashtags from the post content
        hashtagService.processHashtagsForPost(response.id(), createPostRequest.content());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific post by its UUID.
     * HTTP Method: DELETE
     * Endpoint: /api/posts/{postId}
     * @param postId the UUID of the post to delete
     * @param requesterId the UUID of the user requesting deletion (ownership verification is checked)
     * @return {@link ResponseEntity} with no content (HTTP 204)
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        postService.deletePost(postId, requesterId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles a user's like status on a specific post (likes if not liked, otherwise unlikes).
     * HTTP Method: POST
     * Endpoint: /api/posts/{postId}/like
     * @param postId the UUID of the post to like/unlike
     * @param userId the UUID of the user performing the action
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable UUID postId, @RequestParam UUID userId) {
        postService.toggleLike(userId, postId);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a specific user's timeline (reposts and posts created by them).
     * HTTP Method: GET
     * Endpoint: /api/posts/user/{username}
     * @param username the username of the user whose timeline is being queried
     * @param requesterId the UUID of the user viewing the timeline (for status context check)
     * @return {@link ResponseEntity} containing a list of {@link PostResponse} representing the user timeline
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostResponse>> getUserTimeline(@PathVariable String username, @RequestParam UUID requesterId) {
        List<PostResponse> timeline = feedService.getUserTimeline(username, requesterId);
        return ResponseEntity.ok(timeline);
    }

    /**
     * Retrieves the full details of a specific post.
     * HTTP Method: GET
     * Endpoint: /api/posts/{postId}
     * @param postId the UUID of the post
     * @param requesterId the UUID of the user retrieving details
     * @return {@link ResponseEntity} containing the {@link PostResponse}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetails(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        PostResponse details = feedService.getPostDetails(postId, requesterId);
        return ResponseEntity.ok(details);
    }

    /**
     * Retrieves replies/comments attached to a specific post.
     * HTTP Method: GET
     * Endpoint: /api/posts/{postId}/replies
     * @param postId the UUID of the parent post
     * @param requesterId the UUID of the user retrieving replies
     * @return {@link ResponseEntity} containing a list of {@link PostResponse} representing the replies
     */
    @GetMapping("/{postId}/replies")
    public ResponseEntity<List<PostResponse>> getPostReplies(@PathVariable UUID postId, @RequestParam UUID requesterId) {
        List<PostResponse> replies = feedService.getPostReplies(postId, requesterId);
        return ResponseEntity.ok(replies);
    }
}
