package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.RegisterMediaRequest;
import arkheim.server.application.services.MediaService;
import arkheim.server.domain.entities.Media;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing uploaded media files, including registration, linking to posts, and deletion.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Registers a new uploaded media item in the database.
     * HTTP Method: POST
     * Endpoint: /api/media
     * @param request the registration details containing file URL, dimensions, size, and uploader UUID
     * @return {@link ResponseEntity} containing the registered {@link Media} entity
     */
    @PostMapping
    public ResponseEntity<Media> registerMedia(@RequestBody RegisterMediaRequest request) {
        Media media = mediaService.registerMedia(
                request.url(),
                request.width(),
                request.height(),
                request.fileSize(),
                request.uploadedBy()
        );
        return ResponseEntity.ok(media);
    }

    /**
     * Links an existing media file to a specific post.
     * HTTP Method: POST
     * Endpoint: /api/media/{mediaId}/link/{postId}
     * @param mediaId the UUID of the media
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @PostMapping("/{mediaId}/link/{postId}")
    public ResponseEntity<Void> linkMediaToPost(@PathVariable UUID mediaId, @PathVariable UUID postId) {
        mediaService.linkMediaToPost(postId, mediaId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unlinks a media file from a specific post.
     * HTTP Method: DELETE
     * Endpoint: /api/media/{mediaId}/link/{postId}
     * @param mediaId the UUID of the media
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} with HTTP 200 status
     */
    @DeleteMapping("/{mediaId}/link/{postId}")
    public ResponseEntity<Void> unlinkMediaFromPost(@PathVariable UUID mediaId, @PathVariable UUID postId) {
        mediaService.unlinkMediaFromPost(postId, mediaId);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a media entry from the database.
     * HTTP Method: DELETE
     * Endpoint: /api/media/{mediaId}
     * @param mediaId the UUID of the media to delete
     * @return {@link ResponseEntity} with no content (HTTP 204)
     */
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads a media file to the server and registers it in the database.
     * HTTP Method: POST
     * Endpoint: /api/media/upload
     * @param file the binary file to upload
     * @param uploadedBy the optional UUID of the user uploading the file
     */
    @PostMapping("/upload")
    public ResponseEntity<Media> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false) UUID uploadedBy) {
        Media media = mediaService.uploadAndRegisterMedia(file, uploadedBy);
        return ResponseEntity.ok(media);
    }

    /**
     * Retrieves all media files attached to a specific post.
     * HTTP Method: GET
     * Endpoint: /api/media/post/{postId}
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} containing a list of {@link Media} attached to the post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Media>> getMediaForPost(@PathVariable UUID postId) {
        List<Media> mediaList = mediaService.getMediaForPost(postId);
        return ResponseEntity.ok(mediaList);
    }
}
