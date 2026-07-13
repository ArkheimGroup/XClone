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
        String name
) {}
