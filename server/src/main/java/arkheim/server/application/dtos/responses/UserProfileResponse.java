package arkheim.server.application.dtos.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String name,
        String email,
        String biography,
        LocalDateTime dateOfBirth,
        String pfpUrl,
        int followerCount,
        int followingCount,
        LocalDateTime createdAt,
        UUID pinnedPostId
) {}
