package arkheim.server.application.services;

import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.domain.entities.Hashtag;
import arkheim.server.domain.entities.Media;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;
import arkheim.server.domain.repository.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagService {
    private final HashtagRepository hashtagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final LikeRepository likeRepository;

    public HashtagService(
            HashtagRepository hashtagRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            MediaRepository mediaRepository,
            LikeRepository likeRepository
    ) {
        this.hashtagRepository = hashtagRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.likeRepository = likeRepository;
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
            Hashtag hashtag = hashtagRepository.findOrCreate(hashtagName);
            hashtagRepository.linkToPost(postId, hashtag.getId());
        }

    }

    /**
     * Retrieves all hashtags associated with a specific post.
     * @param postId the post's UUID
     * @return List of {@link Hashtag} entities
     */
    public List<Hashtag> getHashtagsForPost(UUID postId) {
        return hashtagRepository.findByPostId(postId);
    }

    /**
     * Retrieves all posts containing a specific hashtag.
     * @param hashtagName name of the hashtag (without the leading #)
     * @param requesterId the user requesting the posts (for calculating isLikedByMe/isRepostedByMe)
     * @return List of {@link PostResponse} containing the hashtag
     */
    public List<PostResponse> getPostsByHashtag(String hashtagName, UUID requesterId) {
        List<Post> posts = hashtagRepository.findPostsByHashtag(hashtagName);

        List<PostResponse> responses = new ArrayList<>();

        for(Post post : posts){
            User author = userRepository.findByUsername(post.getAuthorUsername());
            List<Post> reposts = postRepository.findReposts(post.getId());
            List<Media> medias = mediaRepository.findByPostId(post.getId());
            int likeCount = likeRepository.countLikesForPost(post.getId());
            boolean isLikedByMe = likeRepository.isLikedByUser(requesterId, post.getId());
            int repostCount = reposts.size();
            String requesterUsername = userRepository.findById(requesterId).getUsername();
            boolean isRepostedByMe = reposts.stream()
                    .anyMatch(r -> Objects.equals(r.getAuthorUsername(), requesterUsername)); // True if a post from reposts is found that has the same username as the requester
            int replyCount = postRepository.findReplies(post.getId()).size();

            responses.add(new PostResponse(
                    post,
                    author,
                    medias,
                    likeCount,
                    repostCount,
                    replyCount,
                    post.getRepostPostId() != null ? post.getRepostPostId() : post.getReplyPostId(),
                    isLikedByMe,
                    isRepostedByMe
            ));
        }

        return responses;
    }
}
