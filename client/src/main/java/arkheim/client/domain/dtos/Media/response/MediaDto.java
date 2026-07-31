package arkheim.client.domain.dtos.Media.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaDto (
        UUID id,
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy,
        LocalDateTime createdAt
){ }
