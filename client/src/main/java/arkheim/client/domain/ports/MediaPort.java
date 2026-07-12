package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.MediaDto;

import java.util.List;
import java.util.UUID;

/**
 * Port for media management operations.
 *
 * The infrastructure adapter implements this interface and is injected into
 * ViewModels from the composition root in Launcher}.
 *
 * Maps to server endpoints under /api/media}.
 */
public interface MediaPort {

    /**
     * HTTP: POST /api/media
     */
    MediaDto registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy);

    /**
     * HTTP: POST /api/media/{mediaId}/link/{postId}
     */
    void linkMediaToPost(UUID mediaId, UUID postId);

    /**
     * HTTP: DELETE /api/media/{mediaId}/link/{postId}
     */
    void unlinkMediaFromPost(UUID mediaId, UUID postId);

    /**
     * HTTP: DELETE /api/media/{mediaId}
     */
    void deleteMedia(UUID mediaId);

    /**
     * HTTP: GET /api/media/post/{postId}
     */
    List<MediaDto> getMediaForPost(UUID postId);
}
