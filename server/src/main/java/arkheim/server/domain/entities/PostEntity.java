package arkheim.server.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a PostEntity
 * */
public class PostEntity {
    private final UUID id;
    private String authorUsername;
    private LocalDateTime createdAt;
    private String description;
    private UUID replyPostId;
    private UUID repostPostId;

    /**
     * Creates a new post authored by user
     * @param authorUsername username of the author
     * @param description description of the post
     * @param repostPostId the reposted post's id (if exists)
     * @param replyPostId the replied post's id (if exists)
     * */
    public PostEntity(String authorUsername, String description, UUID replyPostId, UUID repostPostId) {
        this.authorUsername = authorUsername;
        this.description = description;
        this.replyPostId = replyPostId;
        this.repostPostId = repostPostId;

        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Used for posts that already exit in database
     * */
    public PostEntity(UUID id, String authorUsername, LocalDateTime createdAt, String description, UUID replyPostId, UUID repostPostId) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.createdAt = createdAt;
        this.description = description;
        this.replyPostId = replyPostId;
        this.repostPostId = repostPostId;
    }

    public UUID getId() {
        return id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public UUID getReplyPostId() {
        return replyPostId;
    }

    public UUID getRepostPostId() {
        return repostPostId;
    }
}
