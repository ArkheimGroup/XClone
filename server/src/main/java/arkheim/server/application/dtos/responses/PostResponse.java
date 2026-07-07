package arkheim.server.application.dtos.responses;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorUsername,
        String authorName,
        String authorPfpUrl,
        String content,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        int likeCount,
        int retweetCount,
        int replyCount,
        UUID parentPostId, // Filled if this post is a Reply post
        boolean isLikedByMe, // Calculated on retrieval
        boolean isRetweetedByMe
) {}
