package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.UserEntity;
import arkheim.server.domain.repository.UserRepository;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.bytesToUuid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private UserEntity mapRow(ResultSet rs) throws SQLException {
        UUID id = bytesToUuid(rs.getBytes("id"));
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String biography = rs.getString("biography");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        Timestamp dobTs = rs.getTimestamp("date_of_birth");
        LocalDateTime dateOfBirth = dobTs != null ? dobTs.toLocalDateTime() : null;

        String pfpUrl = rs.getString("pfp_url");
        String bannerUrl = rs.getString("banner_url");
        int followerCount = rs.getInt("follower_count");
        int followingCount = rs.getInt("following_count");
        byte[] pinnedPostBytes = rs.getBytes("pinned_post_id");
        UUID pinnedPostId = pinnedPostBytes != null ? bytesToUuid(pinnedPostBytes) : null;
        boolean isVerified = rs.getBoolean("is_verified");

        return new UserEntity(id, username, passwordHash, name, email,biography, createdAt, pfpUrl, bannerUrl, followerCount, followingCount, pinnedPostId, dateOfBirth, isVerified);
    }

    private final RowMapper<UserEntity> userRowMapper = ((rs, rowNum) -> mapRow(rs));

    /**
     * @return UserEntity with specified UUID if found, else it would return null
     * */
    @Override
    public UserEntity findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id=?";
        // JDBC Template queries always return a list no matter how many rows are found in the query
        List<UserEntity> result = jdbcTemplate.query(sql, userRowMapper, (Object) uuidToBytes(id));
        return result.stream().findFirst().orElse(null);
    }

    /**
     * @return UserEntity with specified username if found, else it would return null
     * */
    @Override
    public UserEntity findByUsername(String username) {
        if (username == null){
            return null;
        }
        String sql = "SELECT * FROM users WHERE username=?";
        List<UserEntity> result = jdbcTemplate.query(sql, userRowMapper, username);
        return result.stream().findFirst().orElse(null);
    }

    /**
     * @return UserEntity with specified email if found, else it would return null
     * */
    @Override
    public UserEntity findByEmail(String email) {
        if (email == null){
            return null;
        }
        String sql = "SELECT * FROM users WHERE email=?";
        List<UserEntity> result = jdbcTemplate.query(sql, userRowMapper, email);
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public void save(UserEntity userEntity) {
        String sql = "INSERT INTO users (id, username, name, email, password_hash, biography, date_of_birth, pfp_url, banner_url, follower_count, following_count, created_at, pinned_post_id, is_verified) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                uuidToBytes(userEntity.getId()),
                userEntity.getUsername(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPasswordHash(),
                userEntity.getBiography(),
                userEntity.getDateOfBirth(),
                userEntity.getPfpUrl(),
                userEntity.getBannerUrl(),
                userEntity.getFollowerCount(),
                userEntity.getFollowingCount(),
                userEntity.getCreatedAt(),
                userEntity.getPinnedPostId() != null ? uuidToBytes(userEntity.getPinnedPostId()) : null,
                userEntity.isVerified()
        );
    }

    @Override
    public void updateProfile(UserEntity userEntity) {
        String sql = "UPDATE users SET name=?, biography=?, pfp_url=?, banner_url=?, is_verified=?, date_of_birth=? " +
                "WHERE id=?";

        jdbcTemplate.update(sql,
                userEntity.getName(),
                userEntity.getBiography(),
                userEntity.getPfpUrl(),
                userEntity.getBannerUrl(),
                userEntity.isVerified(),
                userEntity.getDateOfBirth(),
                uuidToBytes(userEntity.getId())
        );
    }

    @Override
    public void updatePinnedPost(UUID userId, UUID postId) {
        String sql = "UPDATE users SET pinned_post_id =? WHERE id=?";
        jdbcTemplate.update(sql,
                postId != null ? uuidToBytes(postId) : null,
                uuidToBytes(userId)
        );
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM users WHERE id=?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(id));
    }

    @Override
    public void incrementFollowerCount(UUID userId) {
        String sql = "UPDATE users SET follower_count = follower_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(userId));
    }

    @Override
    public void decrementFollowerCount(UUID userId) {
        String sql = "UPDATE users SET follower_count = follower_count - 1 WHERE id = ?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(userId));
    }

    @Override
    public void incrementFollowingCount(UUID userId) {
        String sql = "UPDATE users SET following_count = following_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(userId));
    }

    @Override
    public void decrementFollowingCount(UUID userId) {
        String sql = "UPDATE users SET following_count = following_count - 1 WHERE id = ?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(userId));
    }

    public RowMapper<UserEntity> getUserRowMapper(){
        return userRowMapper;
    }
}
