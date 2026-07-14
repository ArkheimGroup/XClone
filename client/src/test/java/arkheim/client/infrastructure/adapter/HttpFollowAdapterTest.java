package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.dtos.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HttpFollowAdapterTest {
    private HttpFollowAdapter followAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private List<UUID> createdUserIds;

    private UserDto userA;
    private UserDto userB;

    @BeforeEach
    public void setUp() {
        followAdapter = new HttpFollowAdapter();
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        createdUserIds = new ArrayList<>();

        String suffixA = UUID.randomUUID().toString().substring(0, 8);
        userA = authAdapter.register("user_a_" + suffixA, "User A", "password", "usera_" + suffixA + "@test.com", LocalDateTime.now());
        createdUserIds.add(userA.id());

        String suffixB = UUID.randomUUID().toString().substring(0, 8);
        userB = authAdapter.register("user_b_" + suffixB, "User B", "password", "userb_" + suffixB + "@test.com", LocalDateTime.now());
        createdUserIds.add(userB.id());
    }

    @AfterEach
    public void tearDown() {
        // Clean up follows (in case test left them)
        try {
            followAdapter.unfollowUser(userA.id(), userB.id());
        } catch (Exception e) {}

        for (UUID id : createdUserIds) {
            try {
                userAdapter.deleteUser(id);
            } catch (Exception e) {}
        }
    }

    @Test
    public void testFollowAndUnfollowLifecycle() {
        // Initially, user A should not follow user B
        assertFalse(followAdapter.isFollowing(userA.id(), userB.id()));

        // User A follows user B
        followAdapter.followUser(userA.id(), userB.id());
        assertTrue(followAdapter.isFollowing(userA.id(), userB.id()));

        // Check following of A contains B
        List<UserDto> followingA = followAdapter.getFollowing(userA.id());
        assertTrue(followingA.stream().anyMatch(u -> u.id().equals(userB.id())));

        // Check followers of B contains A
        List<UserDto> followersB = followAdapter.getFollowers(userB.id());
        assertTrue(followersB.stream().anyMatch(u -> u.id().equals(userA.id())));

        // User A unfollows user B
        followAdapter.unfollowUser(userA.id(), userB.id());
        assertFalse(followAdapter.isFollowing(userA.id(), userB.id()));

        // Check following of A does not contain B anymore
        followingA = followAdapter.getFollowing(userA.id());
        assertFalse(followingA.stream().anyMatch(u -> u.id().equals(userB.id())));
    }
}
