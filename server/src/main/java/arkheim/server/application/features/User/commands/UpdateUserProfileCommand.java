package arkheim.server.application.features.User.commands;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateUserProfileCommand(
        UUID userId,
        String name,
        String biography,
        String pfpUrl,
        LocalDateTime dateOfBirth,
        String bannerUrl,
        boolean isVerified
) { }
