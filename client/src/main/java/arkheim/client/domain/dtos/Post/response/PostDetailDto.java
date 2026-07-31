package arkheim.client.domain.dtos.Post.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailDto(
        UUID id,
        UUID authorId,
        String authorUsername,
        String authorName,
        String authorPfpUrl,
        boolean authorVerified,
        String content,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        int likeCount,
        int repostCount,
        int replyCount,
        UUID parentPostId, // Filled if this post is a Reply or Repost
        String repliedUsername, // Username of the parent post's author
        boolean isRepost,
        String repostedFromUsername,
        boolean isLikedByMe, // Calculated on retrieval
        boolean isRepostedByMe
) { }