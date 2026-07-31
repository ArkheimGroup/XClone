package arkheim.server.application.features.User.dtos;

import java.util.UUID;

public record GetUserDto (
        UUID id,
        String username,
        String email,
        String name,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified
) { }
