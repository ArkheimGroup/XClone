package arkheim.server.domain.repository;

import arkheim.server.domain.entities.Media;

import java.util.List;
import java.util.UUID;

public interface MediaRepository {
    Media findById(UUID id);
    List<Media> findByPostId(UUID postId);

    /**
     * Warning: only saves the media in DB, it doesn't link it into a post
     * linking should be done manually using {@link #linkToPost(UUID, UUID) linkToPost}
     * */
    void save(Media media);

    /**
     * Warning: only links a media to a post
     * saving the media should be done manually using {@link #save(Media) save}
     * */
    void linkToPost(UUID postId, UUID mediaId);

    /**
     * Deletes a Media from DB
     * */
    void delete(UUID id);

    /**
     * Unlinks a media from a post, should be used after media removal {@link #delete delete}
     * */
    void unLinkFromPost(UUID postId, UUID mediaId);
}
