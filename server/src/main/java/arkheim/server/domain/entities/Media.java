package arkheim.server.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Media {
    private final UUID id;
    private String url;
    private int width;
    private int height;
    private long fileSize;
    private UUID uploadedBy;
    private LocalDateTime createdAt;

    /**
     * Creates new media related to a post
     * @param url Media's url
     * @param width Media's width
     * @param height Media's height
     * @param fileSize Media's file size
     * @param uploadedBy author's id
     * */
    public Media(String url, int width, int height, long fileSize, UUID uploadedBy) {
        this.id = UUID.randomUUID();
        this.url = url;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Used for already existing media
     * */
    public Media(UUID id, String url, int width, int height, long fileSize, UUID uploadedBy, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getFileSize() {
        return fileSize;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
