package arkheim.client.domain.dtos.User.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateUserProfileRequest(
        UUID userId,
        String name,
        String biography,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified,
        LocalDateTime dateOfBirth
) { }
