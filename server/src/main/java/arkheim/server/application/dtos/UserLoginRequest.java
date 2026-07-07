package arkheim.server.application.dtos;

public record UserLoginRequest(
        String email,
        String rawPassword
) {}