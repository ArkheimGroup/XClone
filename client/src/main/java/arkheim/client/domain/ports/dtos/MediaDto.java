package arkheim.client.domain.ports.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Client-side DTO mirroring the server's Media} entity as returned by MediaController}.
 */
public record MediaDto(
        UUID id,
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy,
        LocalDateTime createdAt
) {}
