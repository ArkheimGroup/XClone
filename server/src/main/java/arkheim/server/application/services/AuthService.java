package arkheim.server.application.services;

import arkheim.server.application.features.Authentication.commands.LoginCommand;
import arkheim.server.application.features.Authentication.commands.RegisterCommand;
import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.MinimalUser;
import arkheim.server.application.ports.PasswordEncoderPort;
import arkheim.server.domain.entities.UserEntity;
import arkheim.server.domain.exception.BadArgumentException;
import arkheim.server.domain.exception.ConflictException;
import arkheim.server.domain.exception.ResultCode;
import arkheim.server.domain.repository.UserRepository;



public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.userMapper = new UserMapper();
    }

    /**
     * @param loginCommand request data containing email and raw password
     * @return {@link MinimalUser} of the logged-in user
     */
    public MinimalUser login(LoginCommand loginCommand) {
        // Fetch userEntity using userRepository
        UserEntity userEntity = userRepository.findByEmail(loginCommand.email());
        if (userEntity == null) {
            throw new BadArgumentException(ResultCode.INVALID_EMAIL_OR_PASSWORD, "Invalid email or password");
        }

        // Verify the password matches
        boolean matches = passwordEncoderPort.matches(loginCommand.rawPassword(), userEntity.getPasswordHash());
        if (!matches) {
            throw new BadArgumentException(ResultCode.INVALID_EMAIL_OR_PASSWORD, "Invalid email or password");
        }

        return userMapper.mapToMinimalUser(userEntity);
    }

    /**
     * @param registerCommand request data containing username, name, email, raw password and dat of birth
     * @return {@link MinimalUser} of the created/registered user
     */
    public MinimalUser register(RegisterCommand registerCommand) {
        // Check if username already exists
        if (userRepository.findByUsername(registerCommand.username()) != null) {
            throw new ConflictException(ResultCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerCommand.email()) != null) {
            throw new ConflictException(ResultCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        // Hash the password
        String hashedPassword = passwordEncoderPort.encode(registerCommand.rawPassword());

        // Create new UserEntity entity
        UserEntity newUserEntity = new UserEntity(
                registerCommand.username(),
                hashedPassword,
                registerCommand.name(),
                registerCommand.email(),
                registerCommand.dateOfBirth()
        );

        // Save the user in the database
        userRepository.save(newUserEntity);

        return userMapper.mapToMinimalUser(newUserEntity);
    }
}
