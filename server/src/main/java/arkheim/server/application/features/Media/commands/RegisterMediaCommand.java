package arkheim.server.application.features.Media.commands;

import java.util.UUID;

public record RegisterMediaCommand(
        String url,
        int width,
        int height,
        long fileSize,
        UUID uploadedBy
) {}
