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

public class HttpAuthAdapterTest {
    private HttpAuthAdapter authAdapter;
    private HttpUserAdapter userAdapter;
    private List<UUID> createdUserIds;

    @BeforeEach
    public void setUp() {
        authAdapter = new HttpAuthAdapter();
        userAdapter = new HttpUserAdapter();
        createdUserIds = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        for (UUID userId : createdUserIds) {
            try {
                userAdapter.deleteUser(userId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testRegisterAndLogin() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "test_" + uniqueSuffix;
        String name = "Test User " + uniqueSuffix;
        String email = "test_" + uniqueSuffix + "@example.com";
        String password = "password123";
        LocalDateTime dob = LocalDateTime.of(1995, 5, 5, 0, 0);

        // Register user
        UserDto registeredUser = authAdapter.register(username, name, password, email, dob);
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.id());
        assertEquals(username, registeredUser.username());
        assertEquals(name, registeredUser.name());
        assertEquals(email, registeredUser.email());

        createdUserIds.add(registeredUser.id());

        // Login user
        UserDto loggedInUser = authAdapter.login(email, password);
        assertNotNull(loggedInUser);
        assertEquals(registeredUser.id(), loggedInUser.id());
        assertEquals(username, loggedInUser.username());
        assertEquals(name, loggedInUser.name());
        assertEquals(email, loggedInUser.email());
    }

    @Test
    public void testLoginWithInvalidCredentialsThrowsException() {
        String nonExistentEmail = "invalid_" + UUID.randomUUID() + "@example.com";
        assertThrows(RuntimeException.class, () -> {
            authAdapter.login(nonExistentEmail, "wrongpassword");
        });
    }
}
