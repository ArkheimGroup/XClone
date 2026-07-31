package arkheim.server.application.services;

import arkheim.server.application.features.Hashtag.mapper.HashtagMapper;
import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.application.models.Hashtag;
import arkheim.server.domain.entities.*;
import arkheim.server.domain.repository.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagService {
    private final HashtagRepository hashtagRepository;
    private final PostService postService;
    private final HashtagMapper hashtagMapper;

    public HashtagService(
            HashtagRepository hashtagRepository,
            PostService postService
    ) {
        this.hashtagRepository = hashtagRepository;
        this.postService = postService;
        this.hashtagMapper = new HashtagMapper();
    }

    /**
     * Parses the description of a post, extracts all hashtags (words starting with #),
     * saves/finds them, and links them to the post.
     * @param postId the post's UUID
     * @param postDescription the content/description of the post
     */
    public void processHashtagsForPost(UUID postId, String postDescription) {
        if(postDescription == null || postDescription.isBlank())
            return;

        // Find hashtags
        Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)"); // NOTE: make this a class field if needed somewhere else
        Set<String> hashtagNames = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(postDescription);
        while(matcher.find()){
            hashtagNames.add(matcher.group(1).toLowerCase());
        }

        // Link hashtags to post ( after find/create hashtags )
        for(String hashtagName : hashtagNames){
            HashtagEntity hashtagEntity = hashtagRepository.findOrCreate(hashtagName);
            hashtagRepository.linkToPost(postId, hashtagEntity.getId());
        }

    }

    /**
     * Retrieves all hashtags associated with a specific post.
     * @param postId the post's UUID
     * @return List of {@link HashtagEntity} models
     */
    public List<Hashtag> getHashtagsForPost(UUID postId) {
        List<HashtagEntity> entities = hashtagRepository.findByPostId(postId);

        return entities.stream().map(hashtagMapper::map).toList();
    }

    /**
     * Retrieves all posts containing a specific hashtag.
     * @param hashtagName name of the hashtag (without the leading #)
     * @param requesterId the user requesting the posts (for calculating isLikedByMe/isRepostedByMe)
     * @return List of {@link PostDetail} containing the hashtag
     */
    public List<PostDetail> getPostsByHashtag(String hashtagName, UUID requesterId) {
        String cleanTag = hashtagName != null ? hashtagName.trim() : "";
        if (cleanTag.startsWith("#")) {
            cleanTag = cleanTag.substring(1).trim();
        }
        cleanTag = cleanTag.toLowerCase();

        List<PostEntity> postEntities = hashtagRepository.findPostsByHashtag(cleanTag);

        List<PostDetail> responses = new ArrayList<>();

        for(PostEntity postEntity : postEntities){
            responses.add(postService.getPostDetail(postEntity, requesterId));
        }

        return responses;
    }
}
