package arkheim.server.application.dtos.responses;

import arkheim.server.domain.entities.User;

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
        String bannerUrl,
        int followerCount,
        int followingCount,
        LocalDateTime createdAt,
        UUID pinnedPostId,
        boolean isVerified
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
                user.getBannerUrl(),
                user.getFollowerCount(),
                user.getFollowingCount(),
                user.getCreatedAt(),
                user.getPinnedPostId(),
                user.isVerified()
        );
    }
}
