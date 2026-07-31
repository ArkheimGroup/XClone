package arkheim.server.application.models.user;

import java.util.UUID;

public record MinimalUser(
        UUID id,
        String username,
        String email,
        String name,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified
) { }
