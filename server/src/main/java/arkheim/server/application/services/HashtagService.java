package arkheim.server.application.services;

import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.domain.Entities.Hashtag;
import arkheim.server.domain.Repository.HashtagRepository;

import java.util.List;
import java.util.UUID;

public class HashtagService {
    private final HashtagRepository hashtagRepository;

    public HashtagService(HashtagRepository hashtagRepository) {
        this.hashtagRepository = hashtagRepository;
    }

    /**
     * Parses the description of a post, extracts all hashtags (words starting with #),
     * saves/finds them, and links them to the post.
     * @param postId the post's UUID
     * @param postDescription the content/description of the post
     */
    public void processHashtagsForPost(UUID postId, String postDescription) {
        // TODO: Scan postDescription for hashtag patterns (e.g., #topic).
        //  For each found hashtag:
        //  - Call hashtagRepository.findOrCreate(hashtagName) to get the Hashtag entity
        //  - Link it to the post using hashtagRepository.linkToPost(postId, hashtag.getId())
    }

    /**
     * Retrieves all hashtags associated with a specific post.
     * @param postId the post's UUID
     * @return List of Hashtag entities
     */
    public List<Hashtag> getHashtagsForPost(UUID postId) {
        // TODO: Retrieve hashtags using hashtagRepository.findByPostId(postId)
        return null;
    }

    /**
     * Retrieves all posts containing a specific hashtag.
     * @param hashtagName name of the hashtag (without the leading #)
     * @param requesterId the user requesting the posts (for calculating isLikedByMe/isRetweetedByMe)
     * @return List of PostResponse containing the hashtag
     */
    public List<PostResponse> getPostsByHashtag(String hashtagName, UUID requesterId) {
        // TODO: Fetch matching posts using hashtagRepository.findPostsByHashtag(hashtagName)
        //  Map posts to PostResponse DTOs.
        return null;
    }
}
