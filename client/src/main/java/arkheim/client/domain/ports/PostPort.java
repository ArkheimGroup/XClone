package arkheim.client.domain.ports;


import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;

import java.util.List;
import java.util.UUID;

/**
 * Port for post and timeline operations.
 * <p>
 * The infrastructure adapter implements this interface and is injected into
 * ViewModels from the composition root in Launcher.
 * <p>
 * Maps to server endpoints under /dto/posts.
 */
public interface PostPort {

    /**
     * HTTP: POST /dto/posts
     */
    PostDetailDto createPost(UUID authorId, String content, String mediaUrl, UUID parentPostId);

    /**
     * HTTP: DELETE /dto/posts/{postId}?requesterId={requesterId}
     */
    ApiResponse deletePost(UUID postId, UUID requesterId);

    /**
     * HTTP: POST /dto/posts/{postId}/like?userId={userId}
     */
    ApiResponse toggleLike(UUID postId, UUID userId);

    /**
     * HTTP: GET /dto/posts/timeline?requesterId={requesterId}
     */
    List<PostDetailDto> getUserTimeline(UUID requesterId);

    /**
     * HTTP: GET /dto/posts/{postId}?requesterId={requesterId}
     */
    PostDetailDto getPostDetails(UUID postId, UUID requesterId);

    /**
     * HTTP: GET /dto/posts/{postId}/replies?requesterId={requesterId}
     */
    List<PostDetailDto> getPostReplies(UUID postId, UUID requesterId);
    
    /**
     * HTTP: GET /dto/posts/byword/{word}
     */
    List<PostDetailDto> findPostsByWord(String word, UUID requesterId);

    /**
     * HTTP: GET /api/posts/user/{username}?requesterId={requesterId}
     */
    List<PostDetailDto> getUserPosts(String username, UUID requesterId);

    List<PostDetailDto> getUserPosts(String username);
}

