package arkheim.server.application.models.post;

import java.time.LocalDateTime;
import java.util.UUID;

public record Post(
        UUID id,
        String authorUsername,
        String description,
        UUID replyPostId,
        UUID repostPostId,
        LocalDateTime createdAt
) {
}
