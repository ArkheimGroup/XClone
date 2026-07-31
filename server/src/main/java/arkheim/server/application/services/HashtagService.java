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
    private final PostRepository postRepository;
    private final PostService postService;
    private final HashtagMapper hashtagMapper;

    public HashtagService(
            HashtagRepository hashtagRepository,
            PostRepository postRepository,
            PostService postService
    ) {
        this.hashtagRepository = hashtagRepository;
        this.postRepository = postRepository;
        this.postService = postService;
        this.hashtagMapper = new HashtagMapper();
    }

    public HashtagService(
            HashtagRepository hashtagRepository,
            PostService postService
    ) {
        this(hashtagRepository, null, postService);
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

        // Support letters (including Unicode), digits, and underscores in hashtags
        Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");
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
     * @param hashtagName name of the hashtag (without or with the leading #)
     * @param requesterId the user requesting the posts (for calculating isLikedByMe/isRepostedByMe)
     * @return List of {@link PostDetail} containing the hashtag
     */
    public List<PostDetail> getPostsByHashtag(String hashtagName, UUID requesterId) {
        String cleanTag = hashtagName != null ? hashtagName.trim() : "";
        if (cleanTag.startsWith("#")) {
            cleanTag = cleanTag.substring(1).trim();
        }
        if (cleanTag.isBlank()) {
            return List.of();
        }
        cleanTag = cleanTag.toLowerCase();

        Map<UUID, PostEntity> postMap = new LinkedHashMap<>();

        // posts linked in post_hashtags table
        List<PostEntity> linkedPosts = hashtagRepository.findPostsByHashtag(cleanTag);
        for (PostEntity post : linkedPosts) {
            postMap.put(post.getId(), post);
        }

        // posts matching "#tag" in content
        if (postRepository != null) {
            List<PostEntity> wordMatches = postRepository.findByWord("#" + cleanTag);
            for (PostEntity post : wordMatches) {
                if (!postMap.containsKey(post.getId())) {
                    postMap.put(post.getId(), post);
                }
            }
        }

        List<PostEntity> sortedEntities = new ArrayList<>(postMap.values());
        sortedEntities.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        List<PostDetail> responses = new ArrayList<>();
        for(PostEntity postEntity : sortedEntities){
            responses.add(postService.getPostDetail(postEntity, requesterId));
        }

        return responses;
    }
}
