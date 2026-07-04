package arkheim.server.infrastructure.Repository;

import arkheim.server.domain.Entities.Hashtag;
import arkheim.server.domain.Entities.Post;
import arkheim.server.domain.Repository.HashtagRepository;
import static arkheim.server.infrastructure.Utils.UuidBinaryConvertor.bytesToUuid;
import static arkheim.server.infrastructure.Utils.UuidBinaryConvertor.uuidToBytes;

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

    public JdbcHashtagRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
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
        return jdbcTemplate.query(sql)
    }

    @Override
    public Hashtag findOrCreate(String name) {
        return null;
    }

    @Override
    public void linkToPost(UUID postId, UUID hashtagId) {

    }
}
