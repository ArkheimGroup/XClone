package arkheim.server.application.dtos.responses;

import arkheim.server.domain.Entities.Media;
import arkheim.server.domain.Entities.Post;
import arkheim.server.domain.Entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorUsername,
        String authorName,
        String authorPfpUrl,
        String content,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        int likeCount,
        int repostCount,
        int replyCount,
        UUID parentPostId, // Filled if this post is a Reply post
        boolean isLikedByMe, // Calculated on retrieval
        boolean isRepostedByMe
) {
    public PostResponse(Post post,
                        User author,
                        List<Media> medias,
                        int likeCount,
                        int repostCount,
                        int replyCount,
                        UUID parentPostId,
                        boolean isLikedByMe,
                        boolean isRepostedByMe
    ){

        List<String> mediaUrls = new ArrayList<>();
        for(Media media : medias){
            mediaUrls.add(media.getUrl());
        }

        this(
                post.getId(),
                author.getId(),
                author.getUsername(),
                author.getName(),
                author.getPfpUrl(),
                post.getDescription(),
                mediaUrls,
                post.getCreatedAt(),
                likeCount,
                repostCount,
                replyCount,
                parentPostId,
                isLikedByMe,
                isRepostedByMe
        );
    }
}
