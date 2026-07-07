package arkheim.server.infrastructure.repository;

import arkheim.server.domain.Entities.User;
import arkheim.server.domain.Repository.FollowRepository;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcFollowRepository implements FollowRepository {
    private final JdbcTemplate jdbcTemplate;
    private final JdbcUserRepository jdbcUserRepository;

    public JdbcFollowRepository(JdbcTemplate jdbcTemplate, JdbcUserRepository jdbcUserRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcUserRepository = jdbcUserRepository;
    }
    
    @Override
    public void follow(UUID followerId, UUID followingId) {
        String sql = "INSERT INTO follows (follower_id, following_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, uuidToBytes(followerId), uuidToBytes(followingId));
    }

    @Override
    public void unfollow(UUID followerId, UUID followingId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND following_id = ?";
        jdbcTemplate.update(sql, uuidToBytes(followerId), uuidToBytes(followingId));
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND following_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uuidToBytes(followerId), uuidToBytes(followingId));
        return count != null && count > 0;
    }

    @Override
    public List<User> findFollowers(UUID userId) {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN follows f ON u.id = f.follower_id " +
                     "WHERE f.following_id = ?";
        return jdbcTemplate.query(sql, jdbcUserRepository.getUserRowMapper(), (Object) uuidToBytes(userId));
    }

    @Override
    public List<User> findFollowing(UUID userId) {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN follows f ON u.id = f.following_id " +
                     "WHERE f.follower_id = ?";
        return jdbcTemplate.query(sql, jdbcUserRepository.getUserRowMapper(), (Object) uuidToBytes(userId));
    }
}
