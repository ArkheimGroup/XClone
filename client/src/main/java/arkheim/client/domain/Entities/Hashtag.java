package arkheim.client.domain.Entities;

import java.util.UUID;

public class Hashtag {

    private final UUID id;
    private String name;

    /**
     * Used for already exiting hashtags
     * */
    public Hashtag(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return "#hashtag_name"
     */
    public String getDisplayTag(){
        return "#" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hashtag)) return false;
        return id.equals(((Hashtag) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "HashtagModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
