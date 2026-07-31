package arkheim.server.application.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record Like(
        UUID userId,
        UUID postId,
        LocalDateTime createdAt)
{ }
