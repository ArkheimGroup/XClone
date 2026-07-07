package arkheim.server.application.dtos;

import java.time.LocalDateTime;

public record UserRegisterRequest(
        String username,
        String name,
        String rawPassword,
        String email,
        LocalDateTime dateOfBirth
) {}
