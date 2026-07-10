package arkheim.server.application.services;

import arkheim.server.application.dtos.UserLoginRequest;
import arkheim.server.application.dtos.UserRegisterRequest;
import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.application.ports.PasswordEncoderPort;
import arkheim.server.domain.entities.User;
import arkheim.server.domain.repository.UserRepository;



public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public AuthService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    /**
     * @param loginRequest request data containing email and raw password
     * @return {@link UserResponse} of the logged-in user
     */
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

    /**
     * @param registerRequest request data containing username, name, email, raw password and dat of birth
     * @return {@link UserResponse} of the created/registered user
     */
    public UserResponse register(UserRegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.username()) != null) {
            throw new IllegalStateException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.email()) != null) {
            throw new IllegalStateException("Email already exists");
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
