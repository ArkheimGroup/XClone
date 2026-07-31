package arkheim.client.domain.ports;


import arkheim.client.domain.dtos.Hashtag.response.HashtagDto;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;

import java.util.List;
import java.util.UUID;

public interface HashtagPort {

    /**
     * HTTP: GET /dto/hashtags/{hashtagName}/posts?requesterId={requesterId}
     */
    List<PostDetailDto> getPostsByHashtag(String hashtagName, UUID requesterId);

    /**
     * HTTP: GET /dto/hashtags/posts/{postId}
     */
    List<HashtagDto> getHashtagsForPost(UUID postId);
}
