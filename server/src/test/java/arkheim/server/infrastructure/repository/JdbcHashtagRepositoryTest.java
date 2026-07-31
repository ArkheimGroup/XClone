package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.HashtagEntity;
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
public class JdbcHashtagRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserRepository userRepository;
    private JdbcPostRepository postRepository;
    private JdbcHashtagRepository hashtagRepository;

    private UserEntity user;
    private PostEntity post;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
        postRepository = new JdbcPostRepository(jdbcTemplate);
        hashtagRepository = new JdbcHashtagRepository(jdbcTemplate, postRepository);

        user = new UserEntity(
                UUID.randomUUID(),
                "hash_user",
                "pw",
                "Hash User",
                "hash@test.com",
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
                "Post with hashtags",
                null,
                null
        );
        postRepository.save(post);
    }

    @Test
    void testFindOrCreateAndLink() {
        // 1. Create or find
        HashtagEntity hashtag = hashtagRepository.findOrCreate("Java");
        assertNotNull(hashtag);
        assertEquals("Java", hashtag.getName());

        // Call again to ensure it returns the same instance/doesn't throw UNIQUE constraint violation
        HashtagEntity existing = hashtagRepository.findOrCreate("Java");
        assertEquals(hashtag.getId(), existing.getId());

        // 2. Find by ID
        HashtagEntity foundById = hashtagRepository.findById(hashtag.getId());
        assertNotNull(foundById);
        assertEquals("Java", foundById.getName());

        // 3. Find by Name
        HashtagEntity foundByName = hashtagRepository.findByName("Java");
        assertNotNull(foundByName);
        assertEquals(hashtag.getId(), foundByName.getId());

        // 4. Link to Post
        hashtagRepository.linkToPost(post.getId(), hashtag.getId());

        // 5. Find by Post ID
        List<HashtagEntity> hashtags = hashtagRepository.findByPostId(post.getId());
        assertEquals(1, hashtags.size());
        assertEquals(hashtag.getId(), hashtags.get(0).getId());

        // 6. Find Posts by Hashtag Name
        List<PostEntity> posts = hashtagRepository.findPostsByHashtag("Java");
        assertEquals(1, posts.size());
        assertEquals(post.getId(), posts.get(0).getId());
    }
}
