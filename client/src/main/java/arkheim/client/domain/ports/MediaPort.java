package arkheim.client.domain.ports;


import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.Media.response.MediaDto;

import java.util.List;
import java.util.UUID;

/**
 * Port for media management operations.
 * <p>
 * The infrastructure adapter implements this interface and is injected into
 * ViewModels from the composition root in Launcher.
 * <p>
 * Maps to server endpoints under /dto/media.
 */
public interface MediaPort {

    /**
     * HTTP: POST /dto/media
     */
    MediaDto registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy);

    /**
     * HTTP: POST /dto/media/{mediaId}/link/{postId}
     */
    ApiResponse linkMediaToPost(UUID mediaId, UUID postId);

    /**
     * HTTP: DELETE /dto/media/{mediaId}/link/{postId}
     */
    ApiResponse unlinkMediaFromPost(UUID mediaId, UUID postId);

    /**
     * HTTP: DELETE /dto/media/{mediaId}
     */
    ApiResponse deleteMedia(UUID mediaId);

    /**
     * HTTP: POST /dto/media/upload
     */
    MediaDto uploadMedia(java.io.File file, UUID uploadedBy);

    /**
     * HTTP: GET /dto/media/post/{postId}
     */
    List<MediaDto> getMediaForPost(UUID postId);
}
