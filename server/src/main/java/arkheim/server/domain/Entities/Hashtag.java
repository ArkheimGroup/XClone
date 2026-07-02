package arkheim.server.domain.Entities;

import java.util.UUID;

/**
 * Represents a Hashtag
 * */
public class Hashtag {
    private final UUID id;
    private String name;

    /**
     * Creates new hashtag
     * @param name Name of the hashtag
     * */
    public Hashtag(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    /**
     * Used for already exiting hashtags
     * */
    public Hashtag(UUID id, String name) {
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
