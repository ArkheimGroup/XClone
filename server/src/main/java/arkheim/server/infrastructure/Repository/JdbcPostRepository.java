package arkheim.server.infrastructure.Repository;

import arkheim.server.domain.Entities.Post;
import arkheim.server.domain.Repository.PostRepository;

import static arkheim.server.infrastructure.Utils.UuidBinaryConvertor.bytesToUuid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        UUID id = bytesToUuid(rs.getBytes("id"));
        String authorUsername = rs.getString("author_username");
        LocalDateTime createdAt = rs.getTimestamp("")
    }

    @Override
    public Post findById(UUID id) {
        return null;
    }

    @Override
    public List<Post> findByAuthorUsername(String username) {
        return List.of();
    }

    @Override
    public List<Post> findReplies(UUID postId) {
        return List.of();
    }

    @Override
    public List<Post> findReposts(UUID postId) {
        return List.of();
    }

    @Override
    public List<Post> findFeedForUser(UUID userId) {
        return List.of();
    }

    @Override
    public void save(Post post) {

    }

    @Override
    public void delete(UUID id) {

    }
}
