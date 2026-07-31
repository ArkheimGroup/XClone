package arkheim.server.domain.repository;

import arkheim.server.domain.entities.PostEntity;

import java.util.List;
import java.util.UUID;

public interface PostRepository {
    PostEntity findById(UUID id);
    List<PostEntity> findByAuthorUsername(String username);
    List<PostEntity> findByWord(String word);
    List<PostEntity> findReplies(UUID postId);
    List<PostEntity> findReposts(UUID postId);
    List<PostEntity> findFollowingsPosts(UUID userId);
    List<PostEntity> getAllPosts();
    void save(PostEntity postEntity);
    void delete(UUID id);
}
