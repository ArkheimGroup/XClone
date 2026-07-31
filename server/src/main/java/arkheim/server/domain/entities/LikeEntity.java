package arkheim.server.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class LikeEntity {
    private final UUID userId;
    private final UUID postId;
    private final LocalDateTime createdAt;

    /**
     * Used for new likes
     * @param userId user who liked the post's id
     * @param postId liked post's id
     * */
    public LikeEntity(UUID userId, UUID postId) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
    }
    /**
     * Used for already existing likes
     */
    public LikeEntity(UUID userId, UUID postId, LocalDateTime createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

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
