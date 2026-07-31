package arkheim.server.infrastructure.api.controllers;

import arkheim.server.application.dtos.ApiResponse;
import arkheim.server.application.dtos.GenericApiResponse;
import arkheim.server.application.features.Media.commands.RegisterMediaCommand;
import arkheim.server.application.features.Media.dtos.GetMediaDto;
import arkheim.server.application.features.Media.mapper.MediaMapper;
import arkheim.server.application.models.Media;
import arkheim.server.application.services.MediaService;
import arkheim.server.domain.exception.ResultCode;
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
    private final MediaMapper mediaMapper;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
        this.mediaMapper = new MediaMapper();
    }

    /**
     * Registers a new uploaded media item in the database.
     * HTTP Method: POST
     * Endpoint: /api/media
     * @param request the registration details containing file URL, dimensions, size, and uploader UUID
     * @return {@link ResponseEntity<GenericApiResponse>} containing the registered {@link GetMediaDto}
     */
    @PostMapping
    public ResponseEntity<GenericApiResponse<GetMediaDto>> registerMedia(@RequestBody RegisterMediaCommand request) {
        Media media = mediaService.registerMedia(
                request.url(),
                request.width(),
                request.height(),
                request.fileSize(),
                request.uploadedBy()
        );

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.MEDIA_REGISTERED, mediaMapper.map(media)));
    }

    /**
     * Links an existing media file to a specific post.
     * HTTP Method: POST
     * Endpoint: /api/media/{mediaId}/link/{postId}
     * @param mediaId the UUID of the media
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @PostMapping("/{mediaId}/link/{postId}")
    public ResponseEntity<ApiResponse> linkMediaToPost(@PathVariable UUID mediaId, @PathVariable UUID postId) {
        mediaService.linkMediaToPost(postId, mediaId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.MEDIA_LINKED_TO_POST));
    }

    /**
     * Unlinks a media file from a specific post.
     * HTTP Method: DELETE
     * Endpoint: /api/media/{mediaId}/link/{postId}
     * @param mediaId the UUID of the media
     * @param postId the UUID of the post
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @DeleteMapping("/{mediaId}/link/{postId}")
    public ResponseEntity<ApiResponse> unlinkMediaFromPost(@PathVariable UUID mediaId, @PathVariable UUID postId) {
        mediaService.unlinkMediaFromPost(postId, mediaId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.MEDIA_UNLINKED_FROM_POST));
    }

    /**
     * Deletes a media entry from the database.
     * HTTP Method: DELETE
     * Endpoint: /api/media/{mediaId}
     * @param mediaId the UUID of the media to delete
     * @return {@link ResponseEntity} containing {@link ApiResponse} with HTTP 200 status
     */
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse> deleteMedia(@PathVariable UUID mediaId) {
        mediaService.deleteMedia(mediaId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.MEDIA_DELETED));
    }

    /**
     * Uploads a media file to the server and registers it in the database.
     * HTTP Method: POST
     * Endpoint: /api/media/upload
     * @param file the binary file to upload
     * @param uploadedBy the optional UUID of the user uploading the file
     */
    @PostMapping("/upload")
    public ResponseEntity<GenericApiResponse<GetMediaDto>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false) UUID uploadedBy) {
        Media media = mediaService.uploadAndRegisterMedia(file, uploadedBy);
        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.MEDIA_REGISTERED, mediaMapper.map(media)));
    }

    /**
     * Retrieves all media files attached to a specific post.
     * HTTP Method: GET
     * Endpoint: /api/media/post/{postId}
     * @param postId the UUID of the post
     * @return {@link ResponseEntity<GenericApiResponse>} containing a list of {@link GetMediaDto} attached to the post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<GenericApiResponse<List<GetMediaDto>>> getMediaForPost(@PathVariable UUID postId) {
        List<Media> mediaList = mediaService.getMediaForPost(postId);

        List<GetMediaDto> response = mediaList.stream().map(mediaMapper::map).toList();

        return ResponseEntity.ok(GenericApiResponse.success(ResultCode.MEDIA_RETRIEVED, response));
    }
}
