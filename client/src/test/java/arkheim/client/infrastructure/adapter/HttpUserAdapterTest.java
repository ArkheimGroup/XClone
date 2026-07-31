package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.dtos.User.response.UserDto;
import arkheim.client.domain.dtos.User.response.UserProfileDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HttpUserAdapterTest {
    private HttpUserAdapter userAdapter;
    private HttpAuthAdapter authAdapter;
    private HttpPostAdapter postAdapter;
    private List<UUID> createdUserIds;
    private List<UUID> createdPostIds;

    private UserDto testUser;

    @BeforeEach
    public void setUp() {
        userAdapter = new HttpUserAdapter();
        authAdapter = new HttpAuthAdapter();
        postAdapter = new HttpPostAdapter();
        createdUserIds = new ArrayList<>();
        createdPostIds = new ArrayList<>();

        // Register a test user for these tests
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        testUser = authAdapter.register(
                "user_" + uniqueSuffix,
                "User Display Name",
                "password123",
                "user_" + uniqueSuffix + "@example.com",
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );
        createdUserIds.add(testUser.id());
    }

    @AfterEach
    public void tearDown() {
        // Delete posts first due to foreign keys (in reverse order)
        for (int i = createdPostIds.size() - 1; i >= 0; i--) {
            UUID postId = createdPostIds.get(i);
            try {
                postAdapter.deletePost(postId, testUser.id());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        for (UUID userId : createdUserIds) {
            try {
                userAdapter.deleteUser(userId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testGetUserProfile() {
        // By ID
        UserProfileDto profileById = userAdapter.getUserProfileById(testUser.id());
        assertNotNull(profileById);
        assertEquals(testUser.id(), profileById.id());
        assertEquals(testUser.username(), profileById.username());

        // By Username
        UserProfileDto profileByUsername = userAdapter.getUserProfileByUsername(testUser.username());
        assertNotNull(profileByUsername);
        assertEquals(testUser.id(), profileByUsername.id());
        assertEquals(testUser.username(), profileByUsername.username());
    }

    @Test
    public void testUpdateProfile() {
        String newName = "Updated Display Name";
        String bio = "This is my new biography.";
        String pfp = "uploads/profile_pictures/new_pfp.png";
        String banner = "uploads/banners/new_banner.png";
        boolean isVerified = true;
        LocalDateTime dob = LocalDateTime.of(1990, 10, 10, 0, 0);

        UserProfileDto updatedProfile = userAdapter.updateProfile(testUser.id(), newName, bio, pfp, banner, isVerified, dob);
        assertNotNull(updatedProfile);
        assertEquals(newName, updatedProfile.name());
        assertEquals(bio, updatedProfile.biography());
        assertEquals(pfp, updatedProfile.pfpUrl());
        assertEquals(banner, updatedProfile.bannerUrl());
        assertTrue(updatedProfile.isVerified());
        assertEquals(dob, updatedProfile.dateOfBirth());
    }

    @Test
    public void testPinAndUnpinPost() {
        // Create a post first
        PostDetailDto post = postAdapter.createPost(testUser.id(), "Post to be pinned #test", null, null);
        assertNotNull(post);
        createdPostIds.add(post.id());

        // Pin the post
        userAdapter.pinPost(testUser.id(), post.id());
        UserProfileDto profileWithPin = userAdapter.getUserProfileById(testUser.id());
        assertEquals(post.id(), profileWithPin.pinnedPostId());

        // Unpin the post
        userAdapter.unpinPost(testUser.id());
        UserProfileDto profileWithoutPin = userAdapter.getUserProfileById(testUser.id());
        assertNull(profileWithoutPin.pinnedPostId());
    }
}
