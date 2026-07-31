package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.Authentication.commands.LoginCommand;
import arkheim.server.application.features.Authentication.commands.RegisterCommand;
import arkheim.server.application.features.User.dtos.GetUserDto;
import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.MinimalUser;
import arkheim.server.application.services.AuthService;
import arkheim.server.domain.exception.ResultCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user authentication operations (login and registration).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.userMapper = new UserMapper();
    }

    /**
     * Authenticates a user with email and password.
     * HTTP Method: POST
     * Endpoint: /api/auth/login
     * @param loginCommand the credentials payload containing email and raw password
     * @return {@link ResponseEntity<GenericApiResponse>} containing {@link GetUserDto} of the logged-in user
     */
    @PostMapping("/login")
    public ResponseEntity<GenericApiResponse<GetUserDto>> login(@RequestBody LoginCommand loginCommand){
        MinimalUser response = authService.login(loginCommand);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.USER_REGISTERED, userMapper.map(response)));
    }

    /**
     * Registers a new user account.
     * HTTP Method: POST
     * Endpoint: /api/auth/register
     * @param registerCommand the registration payload containing details (username, name, email, raw password, date of birth)
     * @return {@link ResponseEntity<GenericApiResponse>} containing {@link GetUserDto} of the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<GenericApiResponse<GetUserDto>> register(@RequestBody RegisterCommand registerCommand){
        MinimalUser response = authService.register(registerCommand);

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.USER_LOGGED_IN, userMapper.map(response)));
    }
}
