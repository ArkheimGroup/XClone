package arkheim.server.application.services;

import arkheim.server.application.dtos.UserLoginRequest;
import arkheim.server.application.dtos.UserRegisterRequest;
import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.application.ports.PasswordEncoderPort;
import arkheim.server.domain.Entities.User;
import arkheim.server.domain.Repository.UserRepository;


// TEMP: Complete implementation as an example
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public AuthService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    public UserResponse login(UserLoginRequest loginRequest) {
        // Fetch user using userRepository
        User user = userRepository.findByEmail(loginRequest.email());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Verify the password matches
        boolean matches = passwordEncoderPort.matches(loginRequest.rawPassword(), user.getPasswordHash());
        if (!matches) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Confirm the login by returning a UserResponse
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getName());
    }

    public UserResponse register(UserRegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.username()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.email()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash the password
        String hashedPassword = passwordEncoderPort.encode(registerRequest.rawPassword());

        // Create new User entity
        User newUser = new User(
                registerRequest.username(),
                hashedPassword,
                registerRequest.name(),
                registerRequest.email(),
                registerRequest.dateOfBirth()
        );

        // Save the user in the database
        userRepository.save(newUser);

        // Return a UserResponse
        return new UserResponse(newUser.getId(), newUser.getUsername(), newUser.getEmail(), newUser.getName());
    }
}
