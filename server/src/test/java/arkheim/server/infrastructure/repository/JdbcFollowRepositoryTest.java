package arkheim.server.infrastructure.repository;

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
public class JdbcFollowRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserRepository userRepository;
    private JdbcFollowRepository followRepository;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
        followRepository = new JdbcFollowRepository(jdbcTemplate, userRepository);

        follower = new User(
                UUID.randomUUID(),
                "follower_u",
                "pw",
                "Follower",
                "follower@test.com",
                "bio",
                LocalDateTime.now(),
                "pfp",
                0,
                0,
                null,
                null
        );
        following = new User(
                UUID.randomUUID(),
                "following_u",
                "pw",
                "Following",
                "following@test.com",
                "bio",
                LocalDateTime.now(),
                "pfp",
                0,
                0,
                null,
                null
        );

        userRepository.save(follower);
        userRepository.save(following);
    }

    @Test
    void testFollowAndUnfollow() {
        // Assert initial state
        assertFalse(followRepository.isFollowing(follower.getId(), following.getId()));

        // Act - follow
        followRepository.follow(follower.getId(), following.getId());

        // Assert followed state
        assertTrue(followRepository.isFollowing(follower.getId(), following.getId()));

        // Check followers list of following user
        List<User> followers = followRepository.findFollowers(following.getId());
        assertEquals(1, followers.size());
        assertEquals(follower.getId(), followers.get(0).getId());

        // Check following list of follower user
        List<User> followings = followRepository.findFollowing(follower.getId());
        assertEquals(1, followings.size());
        assertEquals(following.getId(), followings.get(0).getId());

        // Act - unfollow
        followRepository.unfollow(follower.getId(), following.getId());

        // Assert unfollowed state
        assertFalse(followRepository.isFollowing(follower.getId(), following.getId()));
        assertTrue(followRepository.findFollowers(following.getId()).isEmpty());
        assertTrue(followRepository.findFollowing(follower.getId()).isEmpty());
    }
}
