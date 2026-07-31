package arkheim.client.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Post implements Comparable<Post> {

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
    public Post(String authorUsername, String description, UUID replyPostId, UUID repostPostId) {
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
    public Post(UUID id, String authorUsername, LocalDateTime createdAt, String description, UUID replyPostId, UUID repostPostId) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.createdAt = createdAt;
        this.description = description;
        this.replyPostId = replyPostId;
        this.repostPostId = repostPostId;
    }

    /**
     * @return true if is a reply
     */
    public boolean isReply(){
        return replyPostId != null;
    }

    /**
     * @return true if is a repost
     */
    public boolean isRepost(){
        return repostPostId != null;
    }

    /**
     * @return true if this is the original post and not a reply or repost
     */
    public boolean isOriginalPost(){
        return !isReply() && !isRepost();
    }

    /**
     * Compares this post to another by creation time, in descending order (most recent first).
     * @param other the post to be compared.
     * @return a negative number if this post is newer than other,
     *         a positive number if this post is older than other,
     *         zero if they were created at the same time
     */
    @Override
    public int compareTo(Post other) {
        return other.createdAt.compareTo(this.createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        return id.equals(((Post) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PostModel{" +
                "id=" + id +
                ", authorUsername='" + authorUsername + '\'' +
                '}';
    }

    // Getters

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
