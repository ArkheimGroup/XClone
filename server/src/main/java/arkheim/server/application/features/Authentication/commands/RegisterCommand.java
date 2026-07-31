package arkheim.server.application.features.Authentication.commands;

import java.time.LocalDateTime;

public record RegisterCommand(
        String username,
        String name,
        String rawPassword,
        String email,
        LocalDateTime dateOfBirth
) { }
