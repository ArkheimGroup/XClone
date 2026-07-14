package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.domain.ports.dtos.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TcpFeedAdapterTest {
    private TcpFeedAdapter feedAdapter;
    private HttpFollowAdapter followAdapter;
    private HttpPostAdapter postAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    
    private List<UUID> createdUserIds;
    private List<UUID> createdPostIds;

    private UserDto userA;
    private UserDto userB;

    @BeforeEach
    public void setUp() {
        feedAdapter = new TcpFeedAdapter("localhost", 8082);
        followAdapter = new HttpFollowAdapter();
        postAdapter = new HttpPostAdapter();
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        
        createdUserIds = new ArrayList<>();
        createdPostIds = new ArrayList<>();

        String suffixA = UUID.randomUUID().toString().substring(0, 8);
        userA = authAdapter.register("user_a_" + suffixA, "User A", "password", "usera_" + suffixA + "@test.com", LocalDateTime.now());
        createdUserIds.add(userA.id());

        String suffixB = UUID.randomUUID().toString().substring(0, 8);
        userB = authAdapter.register("user_b_" + suffixB, "User B", "password", "userb_" + suffixB + "@test.com", LocalDateTime.now());
        createdUserIds.add(userB.id());
    }

    @AfterEach
    public void tearDown() {
        try {
            followAdapter.unfollowUser(userA.id(), userB.id());
        } catch (Exception e) {}
        // Delete posts first due to foreign keys (in reverse order)
        for (int i = createdPostIds.size() - 1; i >= 0; i--) {
            UUID postId = createdPostIds.get(i);
            try {
                postAdapter.deletePost(postId, userB.id());
            } catch (Exception e) {}
        }
        for (UUID id : createdUserIds) {
            try {
                userAdapter.deleteUser(id);
            } catch (Exception e) {}
        }
    }

    @Test
    public void testGetHomeFeed() {
        // User A follows User B
        followAdapter.followUser(userA.id(), userB.id());

        // User B posts a message
        String content = "Hello followers! This is user B.";
        PostDto post = postAdapter.createPost(userB.id(), content, null, null);
        assertNotNull(post);
        createdPostIds.add(post.id());

        // Get feed of User A (should contain User B's post)
        List<PostDto> feed = feedAdapter.getHomeFeed(userA.id());
        assertNotNull(feed);
        assertTrue(feed.stream().anyMatch(p -> p.id().equals(post.id())));
    }
}
