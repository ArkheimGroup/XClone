package arkheim.server.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Automatically ensures new database columns (banner_url, is_verified) exist on MySQL tables
 * during application startup.
 */
@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initDatabaseSchema() {
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN banner_url VARCHAR(255) DEFAULT 'uploads/banners/default_banner.png'");
        } catch (Exception ignored) {
            // Column banner_url already exists
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN is_verified BOOLEAN DEFAULT false");
        } catch (Exception ignored) {
            // Column is_verified already exists
        }
    }
}
