package arkheim.server.application.dtos.responses;

import arkheim.server.domain.Entities.User;

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
) {
    public UserProfileResponse(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getBiography(),
                user.getDateOfBirth(),
                user.getPfpUrl(),
                user.getFollowerCount(),
                user.getFollowingCount(),
                user.getCreatedAt(),
                user.getPinnedPostId()
        );
    }
}
