package arkheim.server.application.dtos.responses;

import arkheim.server.domain.entities.User;

import java.util.UUID;

/**
 * This DTO is used when a registration or a login request was successful
 * */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String name,
        String pfpUrl,
        String bannerUrl,
        boolean isVerified
) {
    public UserResponse(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getPfpUrl(),
                user.getBannerUrl(),
                user.isVerified()
        );
    }

    public UserResponse(UUID id, String username, String email, String name, boolean isVerified) {
        this(id, username, email, name, "uploads/profile_pictures/default_pfp.png", "uploads/banners/default_banner.png", isVerified);
    }

    public UserResponse(UUID id, String username, String email, String name, String pfpUrl, String bannerUrl, boolean isVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.pfpUrl = pfpUrl;
        this.bannerUrl = bannerUrl;
        this.isVerified = isVerified;
    }
}