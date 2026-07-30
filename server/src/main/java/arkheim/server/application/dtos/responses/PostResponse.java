package arkheim.server.application.dtos.responses;

import arkheim.server.domain.entities.Media;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorUsername,
        String authorName,
        String authorPfpUrl,
        boolean authorVerified,
        String content,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        int likeCount,
        int repostCount,
        int replyCount,
        UUID parentPostId, // Filled if this post is a Reply or Repost
        String repliedUsername, // Username of the parent post's author
        boolean isRepost,
        String repostedFromUsername,
        boolean isLikedByMe, // Calculated on retrieval
        boolean isRepostedByMe
) {
    public PostResponse(Post post,
                        String content,
                        User author,
                        List<Media> medias,
                        int likeCount,
                        int repostCount,
                        int replyCount,
                        UUID parentPostId,
                        String repliedUsername,
                        boolean isRepost,
                        String repostedFromUsername,
                        boolean isLikedByMe,
                        boolean isRepostedByMe
    ){

        List<String> mediaUrls = new ArrayList<>();
        if (medias != null) {
            for(Media media : medias){
                mediaUrls.add(media.getUrl());
            }
        }

        this(
                post.getId(),
                author.getId(),
                author.getUsername(),
                author.getName(),
                author.getPfpUrl(),
                author != null && author.isVerified(),
                content != null ? content : post.getDescription(),
                mediaUrls,
                post.getCreatedAt(),
                likeCount,
                repostCount,
                replyCount,
                parentPostId,
                repliedUsername,
                isRepost,
                repostedFromUsername,
                isLikedByMe,
                isRepostedByMe
        );
    }

    public PostResponse(Post post,
                        User author,
                        List<Media> medias,
                        int likeCount,
                        int repostCount,
                        int replyCount,
                        UUID parentPostId,
                        String repliedUsername,
                        boolean isRepost,
                        String repostedFromUsername,
                        boolean isLikedByMe,
                        boolean isRepostedByMe
    ){
        this(
                post,
                post != null ? post.getDescription() : "",
                author,
                medias,
                likeCount,
                repostCount,
                replyCount,
                parentPostId,
                repliedUsername,
                isRepost,
                repostedFromUsername,
                isLikedByMe,
                isRepostedByMe);
    }
}
