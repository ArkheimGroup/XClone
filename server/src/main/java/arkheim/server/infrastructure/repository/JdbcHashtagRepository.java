package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.Hashtag;
import arkheim.server.domain.entities.Post;
import arkheim.server.domain.repository.HashtagRepository;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.bytesToUuid;
import static arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


@Repository
public class JdbcHashtagRepository implements HashtagRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcPostRepository jdbcPostRepository;

    public JdbcHashtagRepository(JdbcTemplate jdbcTemplate, JdbcPostRepository jdbcPostRepository){
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcPostRepository = jdbcPostRepository;
    }

    private Hashtag mapRow(ResultSet rs) throws SQLException {
        UUID id = bytesToUuid(rs.getBytes("id"));
        String name = rs.getString("name");

        return new Hashtag(id, name);
    }

    private final RowMapper<Hashtag> hashtagMapper = (rs, rowNum) -> mapRow(rs);

    @Override
    public Hashtag findById(UUID id) {
        String sql = "SELECT * FROM hashtags WHERE id=?";
        List<Hashtag> result = jdbcTemplate.query(sql, hashtagMapper, (Object) uuidToBytes(id));
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public Hashtag findByName(String name) {
        String sql = "SELECT * FROM hashtags WHERE name=?";
        List<Hashtag> result = jdbcTemplate.query(sql, hashtagMapper, name);
        return  result.stream().findFirst().orElse(null);
    }

    @Override
    public List<Hashtag> findByPostId(UUID postId) {
        String sql = "SELECT h.id, h.name FROM hashtags h " +
                "JOIN post_hashtags ph ON h.id = ph.hashtag_id " +
                "WHERE ph.post_id = ?";
        return jdbcTemplate.query(sql, hashtagMapper, (Object) uuidToBytes(postId));
    }

    @Override
    public List<Post> findPostsByHashtag(String name) {
        String sql = "SELECT p.* FROM posts p " +
                "JOIN post_hashtags ph ON ph.post_id = p.id " +
                "JOIN hashtags h ON h.id = ph.hashtag_id " +
                "WHERE h.name=?";
        return jdbcTemplate.query(sql, jdbcPostRepository.getPostRowMapper(), name);
    }

    @Override
    public Hashtag findOrCreate(String name) {
        Hashtag hashtag = findByName(name);
        if (hashtag != null) {
            return hashtag;
        }
        Hashtag newHashtag = new Hashtag(name);
        String sql = "INSERT INTO hashtags (id, name) VALUES (?, ?)";
        jdbcTemplate.update(sql, uuidToBytes(newHashtag.getId()), newHashtag.getName());
        return newHashtag;
    }

    @Override
    public void linkToPost(UUID postId, UUID hashtagId) {
        String sql = "INSERT INTO post_hashtags (hashtag_id, post_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, uuidToBytes(hashtagId), uuidToBytes(postId));
    }
}
