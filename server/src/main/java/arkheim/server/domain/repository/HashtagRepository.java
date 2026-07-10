package arkheim.server.domain.repository;

import arkheim.server.domain.entities.Hashtag;
import arkheim.server.domain.entities.Post;

import java.util.List;
import java.util.UUID;

public interface HashtagRepository {
    Hashtag findById(UUID id);

    /**
     * @return a Hashtag with specified name
     * */
    Hashtag findByName(String name);

    /**
     * @return Any hashtag that is present in a post
     * */
    List<Hashtag> findByPostId(UUID postId);

    /**
     * Fetches any post that contains a certain hashtag
     * */
    List<Post> findPostsByHashtag(String name);

    /**
     * When a hashtag is used, creates the hashtag in DB or if it already exists just uses it
     * */
    Hashtag findOrCreate(String name);

    /**
     * Links hashtag to a post
     * */
    void linkToPost(UUID postId, UUID hashtagId);
}

