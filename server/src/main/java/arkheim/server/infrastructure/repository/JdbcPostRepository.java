package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.Post;
import arkheim.server.domain.repository.PostRepository;

import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.bytesToUuid;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        String description = rs.getString("description");
        byte[] replyPostBytes = rs.getBytes("reply_post_id");
        UUID replyPostId = replyPostBytes != null ? bytesToUuid(replyPostBytes) : null;
        byte[] repostPostBytes = rs.getBytes("repost_post_id");
        UUID repostPostId = repostPostBytes != null ? bytesToUuid(repostPostBytes) : null;
        return new Post(id, authorUsername, createdAt, description, replyPostId, repostPostId);
    }

    private final RowMapper<Post> postRowMapper = (rs, rowNum) -> mapRow(rs);

    @Override
    public Post findById(UUID id) {
        String sql = "SELECT * FROM posts WHERE id=?";
        List<Post> result = jdbcTemplate.query(sql, postRowMapper, (Object) uuidToBytes(id));
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public List<Post> findByAuthorUsername(String username) {
        String sql = "SELECT * FROM posts WHERE author_username=?";
        return jdbcTemplate.query(sql, postRowMapper, username);
    }

    @Override
    public List<Post> findByWord(String word) {
        String sql = "SELECT * FROM posts WHERE description LIKE ?";
        String pattern = "%" + word + "%";
        return jdbcTemplate.query(sql, postRowMapper, pattern);
    }

    @Override
    public List<Post> findReplies(UUID postId) {
        String sql = "SELECT * FROM posts WHERE reply_post_id=?";
        return jdbcTemplate.query(sql, postRowMapper, (Object) uuidToBytes(postId));
    }

    @Override
    public List<Post> findReposts(UUID postId) {
        String sql = "SELECT * FROM posts WHERE repost_post_id=?";
        return jdbcTemplate.query(sql, postRowMapper, (Object) uuidToBytes(postId));
    }

    @Override
    public List<Post> findFollowingsPosts(UUID userId) {
        String sql = "SELECT p.* FROM posts p " +
                "JOIN users u ON p.author_username = u.username " +
                "JOIN follows f ON u.id = f.following_id " +
                "WHERE f.follower_id = ? " +
                "ORDER BY p.created_at DESC";
        return jdbcTemplate.query(sql, postRowMapper, (Object) uuidToBytes(userId));
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT INTO posts (id, author_username, created_at, description, reply_post_id, repost_post_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                uuidToBytes(post.getId()),
                post.getAuthorUsername(),
                post.getCreatedAt(),
                post.getDescription(),
                post.getReplyPostId() != null ? uuidToBytes(post.getReplyPostId()) : null,
                post.getRepostPostId() != null ? uuidToBytes(post.getRepostPostId()) : null
        );
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM posts WHERE id=?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(id));
    }

    /**
     * Used in JdbcHashtagRepository
     * */
    public RowMapper<Post> getPostRowMapper() {
        return postRowMapper;
    }
}
