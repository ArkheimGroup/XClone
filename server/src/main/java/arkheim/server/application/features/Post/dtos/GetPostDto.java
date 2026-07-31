package arkheim.server.application.features.Post.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetPostDto(
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
        UUID parentPostId, // Filled if this post is a Reply post
        boolean isLikedByMe, // Calculated on retrieval
        boolean isRepostedByMe
) { }
