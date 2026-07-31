package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.ApiResponse;
import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.User.commands.UpdateUserProfileCommand;
import arkheim.server.application.features.User.dtos.GetUserProfileDto;
import arkheim.server.application.features.User.mapper.UserMapper;
import arkheim.server.application.models.user.User;
import arkheim.server.application.services.UserService;
import arkheim.server.domain.exception.ResultCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for user profile operations (retrieval, updates, pinning posts, and account deletion).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService) {
        this.userService = userService;
        this.userMapper = new UserMapper();
    }

    /**
     * Retrieves the profile information of a user by their UUID.
     * HTTP Method: GET
     * Endpoint: /api/users/{userId}
     * @param userId the UUID of the user to retrieve
     * @return {@link ResponseEntity} containing {@link GenericApiResponse<GetUserProfileDto>} details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<GenericApiResponse<GetUserProfileDto>> getUserProfileById(@PathVariable UUID userId) {
        User user = userService.getUserProfileById(userId);

        GenericApiResponse<GetUserProfileDto> response = GenericApiResponse.success(ResultCode.USER_PROFILE_RETRIEVED, new UserMapper().map(user));

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile information of a user by their username.
     * HTTP Method: GET
     * Endpoint: /api/users/username/{username}
     * @param username the username of the user to retrieve
     * @return {@link ResponseEntity} containing {@link GenericApiResponse<GetUserProfileDto>} details
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<GenericApiResponse<GetUserProfileDto>> getUserProfileByUsername(@PathVariable String username) {
        User user = userService.getUserProfileByUsername(username);

        GenericApiResponse<GetUserProfileDto> response = GenericApiResponse.success(ResultCode.USER_PROFILE_RETRIEVED, new UserMapper().map(user));

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the user's profile details.
     * HTTP Method: PUT
     * Endpoint: /api/users
     * @param request {@link UpdateUserProfileCommand} the update request payload containing profile fields
     * @return {@link ResponseEntity} containing the updated {@link GenericApiResponse<GetUserProfileDto>} details
     */
    @PutMapping
    public ResponseEntity<GenericApiResponse<GetUserProfileDto>> updateProfile(@RequestBody UpdateUserProfileCommand request) {
        User user = userService.updateProfile(userMapper.map(request));

        GenericApiResponse<GetUserProfileDto> response = GenericApiResponse.success(ResultCode.USER_PROFILE_RETRIEVED, new UserMapper().map(user));

        return ResponseEntity.ok(response);
    }

    /**
     * Pins a specific post to the user's profile.
     * HTTP Method: PUT
     * Endpoint: /api/users/{userId}/pin/{postId}
     * @param userId the UUID of the user
     * @param postId the UUID of the post to pin
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @PutMapping("/{userId}/pin/{postId}")
    public ResponseEntity<ApiResponse> pinPost(@PathVariable UUID userId, @PathVariable UUID postId) {
        userService.pinPost(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.POST_PINNED));
    }

    /**
     * Unpins any pinned post from the user's profile.
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userId}/pin
     * @param userId the UUID of the user
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @DeleteMapping("/{userId}/pin")
    public ResponseEntity<Void> unpinPost(@PathVariable UUID userId) {
        userService.unpinPost(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes the user account by UUID.
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userId}
     * @param userId the UUID of the user to delete
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.USER_DELETED));
    }
}
