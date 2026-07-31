package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.PostDto;

import java.util.List;
import java.util.UUID;

/**
 * Port for post and timeline operations.
 *
 * The infrastructure adapter implements this interface and is injected into
 * ViewModels from the composition root in Launcher}.
 *
 * Maps to server endpoints under /api/posts}.
 */
public interface PostPort {

    /**
     * HTTP: POST /api/posts
     */
    PostDto createPost(UUID authorId, String content, String mediaUrl, UUID parentPostId);

    /**
     * HTTP: DELETE /api/posts/{postId}?requesterId={requesterId}
     */
    void deletePost(UUID postId, UUID requesterId);

    /**
     * HTTP: POST /api/posts/{postId}/like?userId={userId}
     */
    void toggleLike(UUID postId, UUID userId);

    /**
     * HTTP: GET /api/posts/timeline?requesterId={requesterId}
     */
    List<PostDto> getUserTimeline(UUID requesterId);

    /**
     * HTTP: GET /api/posts/{postId}?requesterId={requesterId}
     */
    PostDto getPostDetails(UUID postId, UUID requesterId);

    /**
     * HTTP: GET /api/posts/{postId}/replies?requesterId={requesterId}
     */
    List<PostDto> getPostReplies(UUID postId, UUID requesterId);
    
    /**
     * HTTP: GET /api/posts/byword/{word}
     */
    List<PostDto> findPostsByWord(String word, UUID requesterId);

    /**
     * HTTP: GET /api/posts/user/{username}?requesterId={requesterId}
     */
    List<PostDto> getUserPosts(String username, UUID requesterId);

    List<PostDto> getUserPosts(String username);
}
