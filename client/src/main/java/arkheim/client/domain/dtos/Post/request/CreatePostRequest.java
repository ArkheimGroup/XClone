package arkheim.client.domain.dtos.Post.request;

import java.util.UUID;

public record CreatePostRequest(
        UUID authorId,
        String content,
        String mediaUrl,
        UUID parentPostId
) { }
