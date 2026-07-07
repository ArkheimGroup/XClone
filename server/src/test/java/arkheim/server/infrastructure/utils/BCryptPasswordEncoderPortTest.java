package arkheim.server.infrastructure.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordEncoderPortTest {

    private final BCryptPasswordEncoderPort encoder = new BCryptPasswordEncoderPort();

    @Test
    void encode_ShouldGenerateSecureHash() {
        String rawPassword = "mySecurePassword123";
        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"),
                "BCrypt hash should start with typical BCrypt prefixes ($2a$ or $2b$)");
    }

    @Test
    void matches_ShouldReturnTrueForCorrectPassword() {
        String rawPassword = "correctPassword";
        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    void matches_ShouldReturnFalseForIncorrectPassword() {
        String rawPassword = "correctPassword";
        String encoded = encoder.encode(rawPassword);

        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    void encode_ShouldGenerateDifferentHashesForSamePasswordDueToUniqueSalts() {
        String password = "samePassword";

        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        // Verify that unique salts are used per hashing operation
        assertNotEquals(hash1, hash2, "Hashes of the same password must be different due to unique salts");

        // Verify both hashes are valid and match the password
        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));
    }
}