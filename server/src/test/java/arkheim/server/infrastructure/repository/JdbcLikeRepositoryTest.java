package arkheim.server.infrastructure.repository;

import arkheim.server.domain.Entities.Post;
import arkheim.server.domain.Entities.User;
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
public class JdbcLikeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserRepository userRepository;
    private JdbcPostRepository postRepository;
    private JdbcLikeRepository likeRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
        postRepository = new JdbcPostRepository(jdbcTemplate);
        likeRepository = new JdbcLikeRepository(jdbcTemplate, userRepository);

        testUser = new User(
                UUID.randomUUID(),
                "like_user",
                "pw",
                "Like User",
                "like@test.com",
                "bio",
                LocalDateTime.now(),
                "pfp",
                0,
                0,
                null,
                null
        );
        userRepository.save(testUser);

        testPost = new Post(
                UUID.randomUUID(),
                testUser.getUsername(),
                LocalDateTime.now(),
                "Test post for liking",
                null,
                null
        );
        postRepository.save(testPost);
    }

    @Test
    void testLikeAndUnlike() {
        // Assert initial state
        assertFalse(likeRepository.isLikedByUser(testUser.getId(), testPost.getId()));
        assertEquals(0, likeRepository.countLikesForPost(testPost.getId()));

        // Act - like
        likeRepository.like(testUser.getId(), testPost.getId());

        // Assert liked state
        assertTrue(likeRepository.isLikedByUser(testUser.getId(), testPost.getId()));
        assertEquals(1, likeRepository.countLikesForPost(testPost.getId()));

        List<User> usersWhoLiked = likeRepository.findUsersWhoLiked(testPost.getId());
        assertEquals(1, usersWhoLiked.size());
        assertEquals(testUser.getId(), usersWhoLiked.get(0).getId());

        // Act - unlike
        likeRepository.unlike(testUser.getId(), testPost.getId());

        // Assert unliked state
        assertFalse(likeRepository.isLikedByUser(testUser.getId(), testPost.getId()));
        assertEquals(0, likeRepository.countLikesForPost(testPost.getId()));
    }
}
