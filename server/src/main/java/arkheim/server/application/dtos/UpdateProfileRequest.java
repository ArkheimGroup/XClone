package arkheim.server.application.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateProfileRequest(
        UUID userId,
        String name,
        String biography,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified,
        LocalDateTime dateOfBirth
) {}
