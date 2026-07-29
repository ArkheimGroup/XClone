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
        String pfpUrl
) {
    public UserResponse(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getPfpUrl()
        );
    }

    public UserResponse(UUID id, String username, String email, String name) {
        this(id, username, email, name, "uploads/profile_pictures/default_pfp.png");
    }

    public UserResponse(UUID id, String username, String email, String name, String pfpUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.pfpUrl = pfpUrl;
    }
}