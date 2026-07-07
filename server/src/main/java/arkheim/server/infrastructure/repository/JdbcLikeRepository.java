package arkheim.server.infrastructure.repository;

import arkheim.server.domain.Entities.User;
import arkheim.server.domain.Repository.LikeRepository;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcLikeRepository implements LikeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcUserRepository jdbcUserRepository;

    public JdbcLikeRepository(JdbcTemplate jdbcTemplate, JdbcUserRepository jdbcUserRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcUserRepository = jdbcUserRepository;
    }

    @Override
    public void like(UUID userId, UUID postId) {
        String sql = "INSERT INTO likes (user_id, post_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, uuidToBytes(userId), uuidToBytes(postId));
    }

    @Override
    public void unlike(UUID userId, UUID postId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND post_id = ?";
        jdbcTemplate.update(sql, uuidToBytes(userId), uuidToBytes(postId));
    }

    @Override
    public boolean isLikedByUser(UUID userId, UUID postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND post_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uuidToBytes(userId), uuidToBytes(postId));
        return count != null && count > 0;
    }

    @Override
    public List<User> findUsersWhoLiked(UUID postId) {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN likes l ON u.id = l.user_id " +
                     "WHERE l.post_id = ?";
        return jdbcTemplate.query(sql, jdbcUserRepository.getUserRowMapper(), (Object) uuidToBytes(postId));
    }

    @Override
    public int countLikesForPost(UUID postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, (Object) uuidToBytes(postId));
        return count != null ? count : 0;
    }
}
