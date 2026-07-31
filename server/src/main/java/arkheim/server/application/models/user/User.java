package arkheim.server.application.models.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String name,
        String email,
        String biography,
        LocalDateTime dateOfBirth,
        String pfpUrl,
        Integer followerCount,
        Integer followingCount,
        LocalDateTime createdAt,
        UUID pinnedPostId,
        String bannerUrl,
        boolean isVerified
) {
}
