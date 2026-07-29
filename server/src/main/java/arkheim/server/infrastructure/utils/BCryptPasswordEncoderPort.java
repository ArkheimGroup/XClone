package arkheim.server.infrastructure.utils;

import arkheim.server.application.ports.PasswordEncoderPort;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoderPort implements PasswordEncoderPort {
    private final int logRounds = 12;

    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String rawPassword, String hashPassword) {
        if (rawPassword == null || hashPassword == null || hashPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, hashPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
