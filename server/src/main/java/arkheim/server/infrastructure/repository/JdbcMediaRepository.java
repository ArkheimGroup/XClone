package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.MediaEntity;
import arkheim.server.domain.repository.MediaRepository;
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
public class  JdbcMediaRepository implements MediaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMediaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private MediaEntity mapRow(ResultSet rs) throws SQLException {
        UUID id = bytesToUuid(rs.getBytes("id"));
        String url = rs.getString("url");
        int width = rs.getInt("width");
        int height = rs.getInt("height");
        long fileSize = rs.getLong("file_size");
        byte[] uploadedByBytes = rs.getBytes("uploaded_by");
        UUID uploadedBy = uploadedByBytes != null ? bytesToUuid(uploadedByBytes) : null;
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        return new MediaEntity(id, url, width, height, fileSize, uploadedBy, createdAt);
    }

    private final RowMapper<MediaEntity> mediaRowMapper = (rs, rowNum) -> mapRow(rs);

    @Override
    public MediaEntity findById(UUID id) {
        String sql = "SELECT * FROM media WHERE id=?";
        List<MediaEntity> result = jdbcTemplate.query(sql, mediaRowMapper, (Object) uuidToBytes(id));
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public MediaEntity findByUrl(String url) {
        String sql = "SELECT * FROM media WHERE url=?";
        List<MediaEntity> result = jdbcTemplate.query(sql, mediaRowMapper, url);
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public List<MediaEntity> findByPostId(UUID postId) {
        String sql = "SELECT m.* FROM media m " +
                "JOIN post_media pm ON m.id = pm.media_id " +
                "WHERE pm.post_id = ?";
        return jdbcTemplate.query(sql, mediaRowMapper, (Object) uuidToBytes(postId));
    }

    @Override
    public void save(MediaEntity mediaEntity) {
        String sql = "INSERT INTO media (id, url, width, height, file_size, uploaded_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                uuidToBytes(mediaEntity.getId()),
                mediaEntity.getUrl(),
                mediaEntity.getWidth(),
                mediaEntity.getHeight(),
                mediaEntity.getFileSize(),
                mediaEntity.getUploadedBy() != null ? uuidToBytes(mediaEntity.getUploadedBy()) : null,
                mediaEntity.getCreatedAt()
        );
    }

    @Override
    public void linkToPost(UUID postId, UUID mediaId) {
        String sql = "INSERT INTO post_media (post_id, media_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, uuidToBytes(postId), uuidToBytes(mediaId));
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM media WHERE id = ?";
        jdbcTemplate.update(sql, (Object) uuidToBytes(id));
    }

    @Override
    public void unLinkFromPost(UUID postId, UUID mediaId) {
        String sql = "DELETE FROM post_media WHERE post_id = ? AND media_id = ?";
        jdbcTemplate.update(sql, uuidToBytes(postId), uuidToBytes(mediaId));
    }
}
