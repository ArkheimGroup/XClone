package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.HashtagDto;
import arkheim.client.domain.ports.dtos.PostDto;

import java.util.List;
import java.util.UUID;

public interface HashtagPort {

    /**
     * HTTP: GET /api/hashtags/{hashtagName}/posts?requesterId={requesterId}
     */
    List<PostDto> getPostsByHashtag(String hashtagName, UUID requesterId);

    /**
     * HTTP: GET /api/hashtags/posts/{postId}
     */
    List<HashtagDto> getHashtagsForPost(UUID postId);
}
