package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.dtos.HashtagDto;
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

public class HttpHashtagAdapterTest {
    private HttpHashtagAdapter hashtagAdapter;
    private HttpPostAdapter postAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private List<UUID> createdUserIds;
    private List<UUID> createdPostIds;

    private UserDto testUser;

    @BeforeEach
    public void setUp() {
        hashtagAdapter = new HttpHashtagAdapter();
        postAdapter = new HttpPostAdapter();
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        createdUserIds = new ArrayList<>();
        createdPostIds = new ArrayList<>();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testUser = authAdapter.register("user_" + suffix, "Test User", "password", "user_" + suffix + "@test.com", LocalDateTime.now());
        createdUserIds.add(testUser.id());
    }

    @AfterEach
    public void tearDown() {
        for (UUID postId : createdPostIds) {
            try {
                postAdapter.deletePost(postId, testUser.id());
            } catch (Exception e) {}
        }
        for (UUID id : createdUserIds) {
            try {
                userAdapter.deleteUser(id);
            } catch (Exception e) {}
        }
    }

    @Test
    public void testHashtags() {
        String hashtagName = "testjunit_" + UUID.randomUUID().toString().substring(0, 8);
        String content = "This is a post with a unique hashtag #" + hashtagName;
        
        PostDto post = postAdapter.createPost(testUser.id(), content, null, null);
        assertNotNull(post);
        createdPostIds.add(post.id());

        // Get hashtags for post
        List<HashtagDto> hashtags = hashtagAdapter.getHashtagsForPost(post.id());
        assertNotNull(hashtags);
        assertTrue(hashtags.stream().anyMatch(h -> h.name().equalsIgnoreCase(hashtagName)));

        // Get posts by hashtag
        List<PostDto> posts = hashtagAdapter.getPostsByHashtag(hashtagName, testUser.id());
        assertNotNull(posts);
        assertTrue(posts.stream().anyMatch(p -> p.id().equals(post.id())));
    }
}
