package arkheim.server.domain.Repository;

import arkheim.server.domain.Entities.Post;

import java.util.List;
import java.util.UUID;

public interface PostRepository {
    Post findById(UUID id);
    List<Post> findByAuthorUsername(String username);
    List<Post> findReplies(UUID postId);
    List<Post> findReposts(UUID postId);
    List<Post> findFeedForUser(UUID userId);
    void save(Post post);
    void delete(UUID id);
}
