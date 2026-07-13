package arkheim.client.domain.ports.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Client-side DTO mirroring the server's UserProfileResponse}.
 * Returned by the user port when fetching a full profile.
 */
public record UserProfileDto(
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
