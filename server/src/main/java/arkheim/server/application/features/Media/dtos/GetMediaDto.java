package arkheim.server.application.features.Media.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetMediaDto (
        UUID id,
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy,
        LocalDateTime createdAt
){ }
