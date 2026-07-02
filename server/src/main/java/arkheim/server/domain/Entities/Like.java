package arkheim.server.domain.Entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Like {
    private final UUID userId;
    private final UUID postId;
    private final LocalDateTime createdAt;

    /**
     * Used for new likes
     * @param userId user who liked the post's id
     * @param postId liked post's id
     * */
    public Like(UUID userId, UUID postId) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
    }
    /**
     * Used for already existing likes
     */
    public Like(UUID userId, UUID postId, LocalDateTime createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }
}
