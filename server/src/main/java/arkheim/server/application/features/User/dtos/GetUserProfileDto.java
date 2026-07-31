package arkheim.server.application.features.User.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetUserProfileDto (
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
){ }
