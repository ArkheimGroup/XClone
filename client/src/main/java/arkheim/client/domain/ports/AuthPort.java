package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.UserDto;

import java.time.LocalDateTime;

public interface AuthPort {

    /**
     * HTTP: POST /api/auth/login}
     */
    UserDto login(String email, String rawPassword);

    /**
     * HTTP: POST /api/auth/register}
     */
    UserDto register(String username, String name, String rawPassword, String email, LocalDateTime dateOfBirth);
}
