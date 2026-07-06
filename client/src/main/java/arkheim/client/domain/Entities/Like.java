package arkheim.client.domain.Entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Like {

    private final UUID userId;
    private final UUID postId;
    private final LocalDateTime createdAt;

    /**
     * Used for already existing likes
     */
    public Like(UUID userId, UUID postId, LocalDateTime createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like)) return false;
        Like other = (Like) o;
        return userId.equals(other.userId) && postId.equals(other.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }

    @Override
    public String toString() {
        return "LikeModel{" +
                "userId=" + userId +
                ", postId=" + postId +
                '}';
    }

    // Getters

    public UUID getUserId() {
        return userId;
    }

    public UUID getPostId() {
        return postId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
