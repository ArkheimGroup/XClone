package arkheim.server.application.features.Authentication.commands;

public record LoginCommand(
        String email,
        String rawPassword
) { }
