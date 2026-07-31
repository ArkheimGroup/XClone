package arkheim.server.infrastructure.repository;

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
public class JdbcPostRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcPostRepository postRepository;
    private JdbcUserRepository userRepository;

    private UserEntity userA;
    private UserEntity userB;
    private UserEntity userC;

    @BeforeEach
    void setUp() {
        postRepository = new JdbcPostRepository(jdbcTemplate);
        userRepository = new JdbcUserRepository(jdbcTemplate);

        // Create users
        userA = new UserEntity(UUID.randomUUID(), "user_a", "pw", "User A", "a@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);
        userB = new UserEntity(UUID.randomUUID(), "user_b", "pw", "User B", "b@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);
        userC = new UserEntity(UUID.randomUUID(), "user_c", "pw", "User C", "c@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);
    }

    @Test
    void testFindFeedForUser() {
        // Post by A
        PostEntity postA = new PostEntity(UUID.randomUUID(), userA.getUsername(), LocalDateTime.now().minusHours(2), "Hello from A", null, null);
        // Post by B
        PostEntity postB = new PostEntity(UUID.randomUUID(), userB.getUsername(), LocalDateTime.now().minusHours(1), "Hello from B", null, null);
        // Post by C
        PostEntity postC = new PostEntity(UUID.randomUUID(), userC.getUsername(), LocalDateTime.now(), "Hello from C", null, null);

        postRepository.save(postA);
        postRepository.save(postB);
        postRepository.save(postC);

        // Setup follows: A follows B, but not C
        jdbcTemplate.update("INSERT INTO follows (follower_id, following_id) VALUES (?, ?)", 
                new Object[]{
                        arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes(userA.getId()),
                        arkheim.server.infrastructure.utils.UuidBinaryConvertor.uuidToBytes(userB.getId())
                }
        );

        // Get feed for A
        List<PostEntity> feed = postRepository.findFollowingsPosts(userA.getId());

        // Assert
        assertNotNull(feed);
        assertEquals(2, feed.size());
        assertEquals(postB.getId(), feed.get(0).getId());
    }
}
