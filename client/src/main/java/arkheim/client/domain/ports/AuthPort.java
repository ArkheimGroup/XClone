package arkheim.client.domain.ports;

import arkheim.client.domain.dtos.User.response.UserDto;

import java.time.LocalDateTime;

public interface AuthPort {

    /**
     * HTTP: POST /dto/auth/login}
     */
    UserDto login(String email, String rawPassword);

    /**
     * HTTP: POST /dto/auth/register}
     */
    UserDto register(String username, String name, String rawPassword, String email, LocalDateTime dateOfBirth);
}
