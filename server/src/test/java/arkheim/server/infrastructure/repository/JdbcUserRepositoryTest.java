// AI GENERATED TEST

package arkheim.server.infrastructure.repository;

import arkheim.server.domain.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Automatically rolls back database modifications after each test case
public class JdbcUserRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserRepository userRepository;
    private User testUser;
    private UUID testId;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
        testId = UUID.randomUUID();

        // Initialize a clean domain entity for testing
        testUser = new User(
                testId,
                "arkheim_dev",
                "hashed_password_123",
                "Arkheim Architect",
                "dev@arkheim.com",
                "Building a clean architecture clone",
                LocalDateTime.now(),
                "https://pfp.url/1",
                0,
                0,
                null,
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );
    }

    @Test
    void saveAndFindById_ShouldPersistAndReturnUser() {
        // Arrange & Act
        userRepository.save(testUser);
        User foundUser = userRepository.findById(testId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {
        // Act
        User foundUser = userRepository.findById(UUID.randomUUID());

        // Assert
        assertNull(foundUser); // Ensures our .stream().findFirst().orElse(null) bugfix works!
    }

    @Test
    void findByUsername_ShouldReturnCorrectUser() {
        // Arrange
        userRepository.save(testUser);

        // Act
        User foundUser = userRepository.findByUsername("arkheim_dev");

        // Assert
        assertNotNull(foundUser);
        assertEquals(testId, foundUser.getId());
    }

    @Test
    void updateProfile_ShouldModifyAllowedFieldsOnly() {
        // Arrange
        userRepository.save(testUser);

        // Create an updated user object modifying profile fields
        User updatedUser = new User(
                testId,
                testUser.getUsername(), // Keeping same
                testUser.getPasswordHash(),
                "New Name",
                testUser.getEmail(),
                "Updated Biography",
                testUser.getCreatedAt(),
                "https://pfp.url/new",
                testUser.getFollowerCount(),
                testUser.getFollowingCount(),
                testUser.getPinnedPostId(),
                testUser.getDateOfBirth()
        );

        // Act
        userRepository.updateProfile(updatedUser);
        User result = userRepository.findById(testId);

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals("Updated Biography", result.getBiography());
        assertEquals("https://pfp.url/new", result.getPfpUrl());
        assertEquals("arkheim_dev", result.getUsername()); // Verifies username was unaffected
    }

    @Test
    void incrementFollowerCount_ShouldAddOne() {
        // Arrange
        userRepository.save(testUser);

        // Act
        userRepository.incrementFollowerCount(testId);
        User result = userRepository.findById(testId);

        // Assert
        assertEquals(1, result.getFollowerCount());
    }

    @Test
    void delete_ShouldRemoveUserFromDatabase() {
        // Arrange
        userRepository.save(testUser);
        assertNotNull(userRepository.findById(testId));

        // Act
        userRepository.delete(testId);
        User result = userRepository.findById(testId);

        // Assert
        assertNull(result);
    }
}