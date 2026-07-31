package arkheim.client.domain.ports;


import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.User.response.UserProfileDto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserPort {

    /**
     * HTTP: GET /dto/users/{userId}
     */
    UserProfileDto getUserProfileById(UUID userId);

    /**
     * HTTP: GET /dto/users/username/{username}
     */
    UserProfileDto getUserProfileByUsername(String username);

    /**
     * HTTP: PUT /dto/users
     */
    UserProfileDto updateProfile(UUID userId, String name, String biography, String pfpUrl, String bannerUrl, boolean isVerified, LocalDateTime dateOfBirth);

    /**
     * HTTP: PUT /dto/users/{userId}/pin/{postId}
     */
    ApiResponse pinPost(UUID userId, UUID postId);

    /**
     * HTTP: DELETE /dto/users/{userId}/pin
     */
    ApiResponse unpinPost(UUID userId);

    /**
     * HTTP: DELETE /dto/users/{userId}
     */
    ApiResponse deleteUser(UUID userId);
}
