package arkheim.server.domain.entities;

import java.util.UUID;

/**
 * Represents a HashtagEntity
 * */
public class HashtagEntity {
    private final UUID id;
    private String name;

    /**
     * Creates new hashtag
     * @param name Name of the hashtag
     * */
    public HashtagEntity(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    /**
     * Used for already exiting hashtags
     * */
    public HashtagEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
