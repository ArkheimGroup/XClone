package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.UpdateProfileRequest;
import arkheim.server.application.dtos.responses.UserProfileResponse;
import arkheim.server.application.services.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile information of a user by their UUID.
     * HTTP Method: GET
     * Endpoint: /api/users/{userId}
     * @param userId the UUID of the user to retrieve
     * @return {@link ResponseEntity} containing {@link UserProfileResponse} details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileById(@PathVariable UUID userId) {
        UserProfileResponse response = userService.getUserProfileById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile information of a user by their username.
     * HTTP Method: GET
     * Endpoint: /api/users/username/{username}
     * @param username the username of the user to retrieve
     * @return {@link ResponseEntity} containing {@link UserProfileResponse} details
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfileByUsername(@PathVariable String username) {
        UserProfileResponse response = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the user's profile details.
     * HTTP Method: PUT
     * Endpoint: /api/users
     * @param updateProfileRequest the update request payload containing profile fields
     * @return {@link ResponseEntity} containing the updated {@link UserProfileResponse} details
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        UserProfileResponse response = userService.updateProfile(updateProfileRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Pins a specific post to the user's profile.
     * HTTP Method: PUT
     * Endpoint: /api/users/{userId}/pin/{postId}
     * @param userId the UUID of the user
     * @param postId the UUID of the post to pin
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @PutMapping("/{userId}/pin/{postId}")
    public ResponseEntity<Void> pinPost(@PathVariable UUID userId, @PathVariable UUID postId) {
        userService.pinPost(userId, postId);
        return ResponseEntity.ok().build();
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
     * @return {@link ResponseEntity} with no content (HTTP 204)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
