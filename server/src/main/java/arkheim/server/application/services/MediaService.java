package arkheim.server.application.services;

import arkheim.server.application.exception.BadArgumentException;
import arkheim.server.domain.entities.Media;
import arkheim.server.application.exception.ErrorCode;
import arkheim.server.application.exception.NotFoundException;
import arkheim.server.domain.repository.MediaRepository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

public class MediaService {
    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    /**
     * Registers a new uploaded {@link Media} entity in the database.
     * @param url physical URL of the uploaded file
     * @param width width of the image/video if applicable
     * @param height height of the image/video if applicable
     * @param fileSize size of the file in bytes
     * @param uploadedBy user UUID who uploaded the media
     * @return Created {@link Media} entity
     */
    public Media registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy) {
        Media media = new Media(
                url,
                width,
                height,
                fileSize,
                uploadedBy
        );

        mediaRepository.save(media);
        return media;
    }

    /**
     * Links an existing {@link Media} entity to a post.
     * @param postId the post's UUID
     * @param mediaId the media's UUID
     */
    public void linkMediaToPost(UUID postId, UUID mediaId) {
        if(mediaRepository.findById(mediaId) == null){
            throw new NotFoundException(ErrorCode.MEDIA_NOT_FOUND, "Media not found");
        }

        mediaRepository.linkToPost(postId, mediaId);
    }

    /**
     * Unlinks an existing {@link Media} entity from a post.
     * @param postId the post's UUID
     * @param mediaId the media's UUID
     */
    public void unlinkMediaFromPost(UUID postId, UUID mediaId) {
        if(mediaRepository.findById(mediaId) == null) {
            throw new NotFoundException(ErrorCode.MEDIA_NOT_FOUND, "Media not found");
        }

        mediaRepository.unLinkFromPost(postId, mediaId);
    }

    /**
     * Deletes a media entry from database and performs cleanup.
     * @param mediaId media's UUID
     */
    public void deleteMedia(UUID mediaId) {
        mediaRepository.delete(mediaId);
    }

    /**
     * Uploads a media file to the server storage and registers it in the database.
     * @param file the uploaded file
     * @param uploadedBy user UUID who uploaded the media
     */
    public Media uploadAndRegisterMedia(MultipartFile file, UUID uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new BadArgumentException(ErrorCode.INVALID_MEDIA_FILE, "Files can't be empty");
        }

        long maxSizeBytes = 15L * 1024 * 1024; // 15MB
        if (file.getSize() > maxSizeBytes) {
            throw new BadArgumentException(ErrorCode.INVALID_MEDIA_FILE, "File size exceeds maximum allowed limit of 15MB");
        }

        try {
            Path uploadDir = Paths.get("uploads/media");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            List<String> allowedImageExtensions = List.of(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");
            if (!allowedImageExtensions.contains(extension)) {
                throw new BadArgumentException(ErrorCode.INVALID_MEDIA_FILE, "Only image files (.png, .jpg, .jpeg, .gif, .bmp, .webp) are allowed");
            }
            String fileName = UUID.randomUUID().toString() + extension; // to store files using UUID
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "uploads/media/" + fileName;

            long fileSize = file.getSize();
            int width = 0;
            int height = 0;

            try (InputStream in = Files.newInputStream(filePath)) {
                BufferedImage image = ImageIO.read(in);
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
            } catch (Exception ignored) {
            }

            return registerMedia(fileUrl, width, height, fileSize, uploadedBy);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    /**
     * Retrieves media items attached to a specific post.
     * @param postId the post's UUID
     * @return List of {@link Media} items
     */
    public List<Media> getMediaForPost(UUID postId) {
        return mediaRepository.findByPostId(postId);
    }
}
