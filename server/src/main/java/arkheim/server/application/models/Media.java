package arkheim.server.application.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record Media(
        UUID id,
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy,
        LocalDateTime createdAt
) {
}
