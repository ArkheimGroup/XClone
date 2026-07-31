package arkheim.server.domain.repository;

import arkheim.server.domain.entities.MediaEntity;

import java.util.List;
import java.util.UUID;

public interface MediaRepository {
    MediaEntity findById(UUID id);
    MediaEntity findByUrl(String url);
    List<MediaEntity> findByPostId(UUID postId);

    /**
     * Warning: only saves the mediaEntity in DB, it doesn't link it into a post
     * linking should be done manually using {@link #linkToPost(UUID, UUID) linkToPost}
     * */
    void save(MediaEntity mediaEntity);

    /**
     * Warning: only links a media to a post
     * saving the media should be done manually using {@link #save(MediaEntity) save}
     * */
    void linkToPost(UUID postId, UUID mediaId);

    /**
     * Deletes a MediaEntity from DB
     * */
    void delete(UUID id);

    /**
     * Unlinks a media from a post, should be used after media removal {@link #delete delete}
     * */
    void unLinkFromPost(UUID postId, UUID mediaId);
}
