package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.Media.response.MediaDto;
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

public class HttpMediaAdapterTest {
    private HttpMediaAdapter mediaAdapter;
    private HttpPostAdapter postAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private List<UUID> createdUserIds;
    private List<UUID> createdPostIds;
    private List<UUID> createdMediaIds;

    private UserDto testUser;

    @BeforeEach
    public void setUp() {
        mediaAdapter = new HttpMediaAdapter();
        postAdapter = new HttpPostAdapter();
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        createdUserIds = new ArrayList<>();
        createdPostIds = new ArrayList<>();
        createdMediaIds = new ArrayList<>();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testUser = authAdapter.register("user_" + suffix, "Test User", "password", "user_" + suffix + "@test.com", LocalDateTime.now());
        createdUserIds.add(testUser.id());
    }

    @AfterEach
    public void tearDown() {
        // Unlink any leftover media and delete media first
        for (UUID mediaId : createdMediaIds) {
            for (UUID postId : createdPostIds) {
                try {
                    mediaAdapter.unlinkMediaFromPost(mediaId, postId);
                } catch (Exception e) {}
            }
            try {
                mediaAdapter.deleteMedia(mediaId);
            } catch (Exception e) {}
        }
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
    public void testMediaLifecycle() {
        // Register Media
        String mediaUrl = "uploads/media/test_image.png";
        MediaDto media = mediaAdapter.registerMedia(mediaUrl, 800, 600, 102400L, testUser.id());
        assertNotNull(media);
        assertNotNull(media.id());
        assertEquals(mediaUrl, media.url());
        assertEquals(testUser.id(), media.uploadedBy());
        createdMediaIds.add(media.id());

        // Create a Post
        PostDetailDto post = postAdapter.createPost(testUser.id(), "A post that will have media linked", null, null);
        assertNotNull(post);
        createdPostIds.add(post.id());

        // Initially no media for post
        List<MediaDto> initialMediaList = mediaAdapter.getMediaForPost(post.id());
        assertTrue(initialMediaList.isEmpty());

        // Link Media to Post
        mediaAdapter.linkMediaToPost(media.id(), post.id());
        
        List<MediaDto> linkedMediaList = mediaAdapter.getMediaForPost(post.id());
        assertEquals(1, linkedMediaList.size());
        assertEquals(media.id(), linkedMediaList.get(0).id());

        // Unlink Media
        mediaAdapter.unlinkMediaFromPost(media.id(), post.id());
        List<MediaDto> unlinkedMediaList = mediaAdapter.getMediaForPost(post.id());
        assertTrue(unlinkedMediaList.isEmpty());

        // Delete Media
        mediaAdapter.deleteMedia(media.id());
        createdMediaIds.remove(media.id()); // Already deleted
    }
}
