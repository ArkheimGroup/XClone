package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.UserProfileDto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserPort {

    /**
     * HTTP: GET /api/users/{userId}
     */
    UserProfileDto getUserProfileById(UUID userId);

    /**
     * HTTP: GET /api/users/username/{username}
     */
    UserProfileDto getUserProfileByUsername(String username);

    /**
     * HTTP: PUT /api/users
     */
    UserProfileDto updateProfile(UUID userId, String name, String biography, String pfpUrl, String bannerUrl, boolean isVerified, LocalDateTime dateOfBirth);

    /**
     * HTTP: PUT /api/users/{userId}/pin/{postId}
     */
    void pinPost(UUID userId, UUID postId);

    /**
     * HTTP: DELETE /api/users/{userId}/pin
     */
    void unpinPost(UUID userId);

    /**
     * HTTP: DELETE /api/users/{userId}
     */
    void deleteUser(UUID userId);
}
