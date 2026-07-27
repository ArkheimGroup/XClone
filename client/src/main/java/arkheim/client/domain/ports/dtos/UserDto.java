package arkheim.client.domain.ports.dtos;

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
        String pfpUrl
) {
    public UserDto(UUID id, String username, String email, String name) {
        this(id, username, email, name, "uploads/profile_pictures/default_pfp.png");
    }
}
