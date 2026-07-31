package arkheim.client.domain.dtos.User.response;

import java.util.UUID;

/**
 * Client-side DTO mirroring the server's UserResponse}.
 * Returned by the auth port on login / register.
 */
public record UserDto(
        UUID id,
        String username,
        String email,
        String name,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified
) {
    public UserDto(UUID id, String username, String email, String name, boolean isVerified) {
        this(id, username, email, name, "uploads/profile_pictures/default_pfp.png", "uploads/banners/default_banner.png", isVerified);
    }
}
