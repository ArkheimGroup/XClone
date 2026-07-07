package arkheim.server.application.services;

import arkheim.server.domain.Entities.Media;
import arkheim.server.domain.Repository.MediaRepository;

import java.util.List;
import java.util.UUID;

public class MediaService {
    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    /**
     * Registers a new uploaded media entity in the database.
     * @param url physical URL of the uploaded file
     * @param width width of the image/video if applicable
     * @param height height of the image/video if applicable
     * @param fileSize size of the file in bytes
     * @param uploadedBy user UUID who uploaded the media
     * @return Created Media entity
     */
    public Media registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy) {
        // TODO: Instantiate Media entity and save using mediaRepository.save(media)
        return null;
    }

    /**
     * Links an existing media entity to a post.
     * @param postId the post's UUID
     * @param mediaId the media's UUID
     */
    public void linkMediaToPost(UUID postId, UUID mediaId) {
        // TODO: Link using mediaRepository.linkToPost(postId, mediaId)
    }

    /**
     * Unlinks an existing media entity from a post.
     * @param postId the post's UUID
     * @param mediaId the media's UUID
     */
    public void unlinkMediaFromPost(UUID postId, UUID mediaId) {
        // TODO: Unlink using mediaRepository.unLinkFromPost(postId, mediaId)
    }

    /**
     * Deletes a media entry from database and performs cleanup.
     * @param mediaId media's UUID
     */
    public void deleteMedia(UUID mediaId) {
        // TODO: Fetch media details, delete file from storage if necessary, then call mediaRepository.delete(mediaId)
    }

    /**
     * Retrieves media items attached to a specific post.
     * @param postId the post's UUID
     * @return List of Media items
     */
    public List<Media> getMediaForPost(UUID postId) {
        // TODO: Retrieve using mediaRepository.findByPostId(postId)
        return null;
    }
}
