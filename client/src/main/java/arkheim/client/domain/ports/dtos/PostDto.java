package arkheim.client.domain.ports.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Client-side DTO mirroring the server's PostResponse}.
 * Returned by the post port for any post-related query.
 */
public record PostDto(
        UUID id,
        UUID authorId,
        String authorUsername,
        String authorName,
        String authorPfpUrl,
        String content,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        int likeCount,
        int repostCount,
        int replyCount,
        UUID parentPostId,
        boolean likedByMe,
        boolean repostedByMe
) {}
