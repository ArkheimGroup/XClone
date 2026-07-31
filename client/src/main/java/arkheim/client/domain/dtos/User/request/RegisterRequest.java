package arkheim.client.domain.dtos.User.request;

import java.time.LocalDateTime;

public record RegisterRequest(
        String username,
        String name,
        String rawPassword,
        String email,
        LocalDateTime dateOfBirth
) { }
