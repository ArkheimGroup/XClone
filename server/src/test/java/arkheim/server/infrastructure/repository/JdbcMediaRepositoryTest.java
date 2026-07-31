package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.MediaEntity;
import arkheim.server.domain.entities.PostEntity;
import arkheim.server.domain.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class JdbcMediaRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserRepository userRepository;
    private JdbcPostRepository postRepository;
    private JdbcMediaRepository mediaRepository;

    private UserEntity user;
    private PostEntity post;
    private MediaEntity media;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
        postRepository = new JdbcPostRepository(jdbcTemplate);
        mediaRepository = new JdbcMediaRepository(jdbcTemplate);

        user = new UserEntity(
                UUID.randomUUID(),
                "media_user",
                "pw",
                "Media User",
                "media@test.com",
                "bio",
                LocalDateTime.now(),
                "pfp",
                0,
                0,
                null,
                null
        );
        userRepository.save(user);

        post = new PostEntity(
                UUID.randomUUID(),
                user.getUsername(),
                LocalDateTime.now(),
                "Post with media",
                null,
                null
        );
        postRepository.save(post);

        media = new MediaEntity(
                UUID.randomUUID(),
                "http://example.com/image.png",
                1920,
                1080,
                1024L,
                user.getId(),
                LocalDateTime.now()
        );
    }

    @Test
    void testSaveFindLinkDelete() {
        // 1. Save
        mediaRepository.save(media);

        // 2. FindById
        MediaEntity found = mediaRepository.findById(media.getId());
        assertNotNull(found);
        assertEquals(media.getUrl(), found.getUrl());
        assertEquals(media.getWidth(), found.getWidth());
        assertEquals(media.getHeight(), found.getHeight());
        assertEquals(media.getFileSize(), found.getFileSize());
        assertEquals(media.getUploadedBy(), found.getUploadedBy());

        // 3. Link to Post
        mediaRepository.linkToPost(post.getId(), media.getId());

        // 4. FindByPostId
        List<MediaEntity> postMedia = mediaRepository.findByPostId(post.getId());
        assertEquals(1, postMedia.size());
        assertEquals(media.getId(), postMedia.get(0).getId());

        // 5. Unlink from Post
        mediaRepository.unLinkFromPost(post.getId(), media.getId());
        assertTrue(mediaRepository.findByPostId(post.getId()).isEmpty());

        // 6. Delete
        mediaRepository.delete(media.getId());
        assertNull(mediaRepository.findById(media.getId()));
    }
}
