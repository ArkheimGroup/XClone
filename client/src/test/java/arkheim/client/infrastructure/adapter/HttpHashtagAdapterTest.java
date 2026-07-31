package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.Hashtag.response.HashtagDto;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.dtos.User.response.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HttpHashtagAdapterTest {
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private HttpPostAdapter postAdapter;
    private HttpHashtagAdapter hashtagAdapter;

    private UserDto testUser;
    private List<UUID> createdPostIds;
    private List<UUID> createdUserIds;

    @BeforeEach
    public void setUp() {
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        postAdapter = new HttpPostAdapter();
        hashtagAdapter = new HttpHashtagAdapter();

        createdPostIds = new ArrayList<>();
        createdUserIds = new ArrayList<>();

        String uniqueUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@test.com";

        testUser = authAdapter.register(
                uniqueUsername,
                "Test User",
                "Password123!",
                uniqueEmail,
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );
        assertNotNull(testUser);
        createdUserIds.add(testUser.id());
    }

    @AfterEach
    public void tearDown() {
        for (UUID id : createdPostIds) {
            try {
                postAdapter.deletePost(id, testUser.id());
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
        
        PostDetailDto post = postAdapter.createPost(testUser.id(), content, null, null);
        assertNotNull(post);
        createdPostIds.add(post.id());

        // Get hashtags for post
        List<HashtagDto> hashtags = hashtagAdapter.getHashtagsForPost(post.id());
        assertNotNull(hashtags);
        assertTrue(hashtags.stream().anyMatch(h -> h.name().equalsIgnoreCase(hashtagName)));

        // Get posts by hashtag
        List<PostDetailDto> posts = hashtagAdapter.getPostsByHashtag(hashtagName, testUser.id());
        assertNotNull(posts);
        assertTrue(posts.stream().anyMatch(p -> p.id().equals(post.id())));
    }
}
