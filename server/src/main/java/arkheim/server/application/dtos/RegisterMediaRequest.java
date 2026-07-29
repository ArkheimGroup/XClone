package arkheim.server.application.dtos;

import java.util.UUID;

public record RegisterMediaRequest(
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy
) {}
