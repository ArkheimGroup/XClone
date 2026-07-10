package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.application.services.HashtagService;
import arkheim.server.domain.entities.Hashtag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for hashtag lookup operations and retrieving posts containing hashtags.
 */
@RestController
@RequestMapping("/api/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    /**
     * Retrieves all posts containing a specific hashtag.
     * HTTP Method: GET
     * Endpoint: /api/hashtags/{hashtagName}/posts
     * @param hashtagName the name of the hashtag (without the leading '#')
     * @param requesterId the UUID of the user requesting posts (for user-specific calculations)
     * @return {@link ResponseEntity} containing a list of {@link PostResponse} containing the hashtag
     */
    @GetMapping("/{hashtagName}/posts")
    public ResponseEntity<List<PostResponse>> getPostsByHashtag(@PathVariable String hashtagName, @RequestParam UUID requesterId) {
        List<PostResponse> posts = hashtagService.getPostsByHashtag(hashtagName, requesterId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Retrieves all hashtags associated with a specific post.
     * HTTP Method: GET
     * Endpoint: /api/hashtags/posts/{postId}
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} containing a list of {@link Hashtag} entities linked to the post
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<Hashtag>> getHashtagsForPost(@PathVariable UUID postId) {
        List<Hashtag> hashtags = hashtagService.getHashtagsForPost(postId);
        return ResponseEntity.ok(hashtags);
    }
}
