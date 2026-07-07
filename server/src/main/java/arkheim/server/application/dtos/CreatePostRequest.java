package arkheim.server.application.dtos;

import java.util.UUID;

public record CreatePostRequest(
        UUID authorId,
        String content,
        String mediaUrl,
        UUID parentPostId
) {}
