package arkheim.server.domain.repository;

import arkheim.server.domain.entities.HashtagEntity;
import arkheim.server.domain.entities.PostEntity;

import java.util.List;
import java.util.UUID;

public interface HashtagRepository {
    HashtagEntity findById(UUID id);

    /**
     * @return a HashtagEntity with specified name
     * */
    HashtagEntity findByName(String name);

    /**
     * @return Any hashtag that is present in a post
     * */
    List<HashtagEntity> findByPostId(UUID postId);

    /**
     * Fetches any post that contains a certain hashtag
     * */
    List<PostEntity> findPostsByHashtag(String name);

    /**
     * When a hashtag is used, creates the hashtag in DB or if it already exists just uses it
     * */
    HashtagEntity findOrCreate(String name);

    /**
     * Links hashtag to a post
     * */
    void linkToPost(UUID postId, UUID hashtagId);
}

