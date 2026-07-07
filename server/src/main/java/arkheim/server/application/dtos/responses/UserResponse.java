package arkheim.server.application.dtos.responses;

import java.util.UUID;

/**
 * This DTO is used when a registration or a login request was successful
 * */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String name
) {
    public UserResponse(UUID id, String username, String email, String name) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
    }
}