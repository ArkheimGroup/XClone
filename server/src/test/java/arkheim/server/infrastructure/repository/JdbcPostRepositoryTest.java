package arkheim.server.infrastructure.repository;

import arkheim.server.domain.entities.Post;
import arkheim.server.domain.entities.User;
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

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        postRepository = new JdbcPostRepository(jdbcTemplate);
        userRepository = new JdbcUserRepository(jdbcTemplate);

        // Create users
        userA = new User(UUID.randomUUID(), "user_a", "pw", "User A", "a@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);
        userB = new User(UUID.randomUUID(), "user_b", "pw", "User B", "b@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);
        userC = new User(UUID.randomUUID(), "user_c", "pw", "User C", "c@test.com", "bio", LocalDateTime.now(), "pfp", 0, 0, null, null);

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);
    }

    @Test
    void testFindFeedForUser() {
        // Post by A
        Post postA = new Post(UUID.randomUUID(), userA.getUsername(), LocalDateTime.now().minusHours(2), "Hello from A", null, null);
        // Post by B
        Post postB = new Post(UUID.randomUUID(), userB.getUsername(), LocalDateTime.now().minusHours(1), "Hello from B", null, null);
        // Post by C
        Post postC = new Post(UUID.randomUUID(), userC.getUsername(), LocalDateTime.now(), "Hello from C", null, null);

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
        List<Post> feed = postRepository.findFollowingsPosts(userA.getId());

        // Assert
        assertNotNull(feed);
        assertEquals(1, feed.size());
        assertEquals(postB.getId(), feed.get(0).getId());
    }
}
