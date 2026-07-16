package arkheim.server.domain.repository;

import arkheim.server.domain.entities.Post;

import java.util.List;
import java.util.UUID;

public interface PostRepository {
    Post findById(UUID id);
    List<Post> findByAuthorUsername(String username);
    List<Post> findByWord(String word);
    List<Post> findReplies(UUID postId);
    List<Post> findReposts(UUID postId);
    List<Post> findFollowingsPosts(UUID userId);
    List<Post> getAllPosts();
    void save(Post post);
    void delete(UUID id);
}
