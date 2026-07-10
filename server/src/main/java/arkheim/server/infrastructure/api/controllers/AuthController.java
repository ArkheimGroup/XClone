package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.UserLoginRequest;
import arkheim.server.application.dtos.UserRegisterRequest;
import arkheim.server.application.dtos.responses.UserResponse;
import arkheim.server.application.services.AuthService;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user with email and password.
     * HTTP Method: POST
     * Endpoint: /api/auth/login
     * @param userLoginRequest the credentials payload containing email and raw password
     * @return {@link ResponseEntity} containing {@link UserResponse} of the logged-in user
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody UserLoginRequest userLoginRequest){
        UserResponse response = authService.login(userLoginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user account.
     * HTTP Method: POST
     * Endpoint: /api/auth/register
     * @param userRegisterRequest the registration payload containing details (username, name, email, raw password, date of birth)
     * @return {@link ResponseEntity} containing {@link UserResponse} of the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest userRegisterRequest){
        UserResponse response = authService.register(userRegisterRequest);
        return ResponseEntity.ok(response);
    }
}
