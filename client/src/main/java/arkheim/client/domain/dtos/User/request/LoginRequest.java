package arkheim.client.domain.dtos.User.request;

public record LoginRequest(
        String email,
        String rawPassword
) { }
