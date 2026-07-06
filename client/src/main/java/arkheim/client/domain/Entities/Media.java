package arkheim.client.domain.Entities;

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
     * Used for medias that already exist in database.
     * Media creation cannot be done in Client layers.
     */
    public Media(UUID id, String url, int width, int height, long fileSize, UUID uploadedBy, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    /**
     * Calculates the width-to-height ratio of the media.
     * Useful for laying out media containers.
     * @return the aspect ratio, (width divided by height)
     */
    public double getAspectRatio(){
        return (double) width / height;
    }

    /**
     * figuring out the format of the media
     */
    public boolean isLandscape(){
        return width > height;
    }
    public boolean isPortrait(){
        return height > width;
    }
    public boolean isSquare(){
        return width == height;
    }

    /**
     * Formats the file into a human-readable string.
     * @return the file sie formatted as "B", "KB" or "MB"
     */
    public String getFormattedFileSize(){
        if(fileSize < 1024) return fileSize + " B";
        if(fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media)) return false;
        return id.equals(((Media) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "MediaModel{" +
                "id=" + id +
                ", uploadedBy=" + uploadedBy +
                ", url='" + url + '\'' +
                '}';
    }

    // Getters

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
