package arkheim.client.infrastructure.adapter;

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

public class HttpPostAdapterTest {
    private HttpPostAdapter postAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private List<UUID> createdUserIds;
    private List<UUID> createdPostIds;

    private UserDto testUser;

    @BeforeEach
    public void setUp() {
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
        // Delete posts first due to foreign keys (in reverse order)
        for (int i = createdPostIds.size() - 1; i >= 0; i--) {
            UUID postId = createdPostIds.get(i);
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
    public void testPostOperations() {
        // Create Post
        String content = "Hello world! This is my first test post.";
        PostDetailDto post = postAdapter.createPost(testUser.id(), content, null, null);
        assertNotNull(post);
        assertNotNull(post.id());
        assertEquals(testUser.id(), post.authorId());
        assertEquals(content, post.content());
        createdPostIds.add(post.id());

        // Get Post Details
        PostDetailDto details = postAdapter.getPostDetails(post.id(), testUser.id());
        assertNotNull(details);
        assertEquals(post.id(), details.id());
        assertEquals(content, details.content());

        // Toggle Like (Like)
        assertFalse(details.isLikedByMe());
        postAdapter.toggleLike(post.id(), testUser.id());
        
        PostDetailDto detailsLiked = postAdapter.getPostDetails(post.id(), testUser.id());
        assertTrue(detailsLiked.isLikedByMe());
        assertEquals(1, detailsLiked.likeCount());

        // Toggle Like again (Unlike)
        postAdapter.toggleLike(post.id(), testUser.id());
        PostDetailDto detailsUnliked = postAdapter.getPostDetails(post.id(), testUser.id());
        assertFalse(detailsUnliked.isLikedByMe());
        assertEquals(0, detailsUnliked.likeCount());

        // Get User Timeline
        List<PostDetailDto> timeline = postAdapter.getUserTimeline(testUser.id());
        assertNotNull(timeline);
        assertTrue(timeline.stream().anyMatch(p -> p.id().equals(post.id())));

        // Create Reply Post
        String replyContent = "Replying to first post!";
        PostDetailDto reply = postAdapter.createPost(testUser.id(), replyContent, null, post.id());
        assertNotNull(reply);
        assertEquals(post.id(), reply.parentPostId());
        createdPostIds.add(reply.id());

        // Get Replies
        List<PostDetailDto> replies = postAdapter.getPostReplies(post.id(), testUser.id());
        assertNotNull(replies);
        assertTrue(replies.stream().anyMatch(r -> r.id().equals(reply.id())));
    }
}
