package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.Hashtag.dtos.GetHashtagDto;
import arkheim.server.application.features.Hashtag.mapper.HashtagMapper;
import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.application.models.Hashtag;
import arkheim.server.application.services.HashtagService;
import arkheim.server.domain.exception.ResultCode;
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
    private final HashtagMapper hashtagMapper;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
        this.hashtagMapper = new HashtagMapper();
    }

    /**
     * Retrieves all posts containing a specific hashtag.
     * HTTP Method: GET
     * Endpoint: /api/hashtags/{hashtagName}/posts
     * @param hashtagName the name of the hashtag (without the leading '#')
     * @param requesterId the UUID of the user requesting posts (for user-specific calculations)
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link PostDetail} containing the hashtag
     */
    @GetMapping("/{hashtagName}/posts")
    public ResponseEntity<GenericApiResponse<List<PostDetail>>> getPostsByHashtag(@PathVariable String hashtagName, @RequestParam(required = false) UUID requesterId) {
        List<PostDetail> posts = hashtagService.getPostsByHashtag(hashtagName, requesterId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.POST_RETRIEVED, posts));
    }

    /**
     * Retrieves all hashtags associated with a specific post.
     * HTTP Method: GET
     * Endpoint: /api/hashtags/posts/{postId}
     * @param postId the UUID of the post
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link GetHashtagDto} models linked to the post
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<GenericApiResponse<List<GetHashtagDto>>> getHashtagsForPost(@PathVariable UUID postId) {
        List<Hashtag> hashtags = hashtagService.getHashtagsForPost(postId);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.HASHTAG_RETRIEVED, hashtags.stream().map(hashtagMapper::map).toList()));
    }
}
