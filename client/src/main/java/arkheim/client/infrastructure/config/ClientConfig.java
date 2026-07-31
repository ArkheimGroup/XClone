package arkheim.client.infrastructure.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration properties for the client (such as server baseUrl and feed socket parameters).
 */
public class ClientConfig {
    private static final Properties properties = new Properties();
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080";
    private static final String DEFAULT_SOCKET_HOST = "localhost";
    private static final int DEFAULT_SOCKET_PORT = 8082;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Try loading config file from resources
        try (InputStream input = ClientConfig.class.getResourceAsStream("/config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception ignored) {
        }

        // get the config.properties from current working directory
        File[] potentialFiles = new File[] {
                new File("config.properties"),
                new File("client/config.properties"),
        };

        for (File file : potentialFiles) {
            if (file.exists() && file.isFile()) {
                try (InputStream input = new FileInputStream(file)) {
                    properties.load(input);
                    break;
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Gets the configured base URL for HTTP API requests.
     * Priority: Environment variable (SERVER_BASE_URL) > config.properties file > Default
     */
    public static String getBaseUrl() {
        String envProp = System.getenv("SERVER_BASE_URL");
        if (envProp != null && !envProp.isBlank()) {
            return envProp.trim();
        }

        String prop = properties.getProperty("server.baseUrl");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }

        return DEFAULT_BASE_URL;
    }

    /**
     * Gets the configured host for TCP feed socket connection.
     */
    public static String getSocketHost() {
        String envProp = System.getenv("SOCKET_URL");
        if (envProp != null && !envProp.isBlank()){
            return envProp.trim();
        }
        String prop = properties.getProperty("feed.socket.host");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return DEFAULT_SOCKET_HOST;
    }

    /**
     * Gets the configured port for TCP feed socket connection.
     */
    public static int getSocketPort() {
        String envProp = System.getenv("SOCKET_PORT");
        if (envProp != null && !envProp.isBlank()) {
            try {
                return Integer.parseInt(envProp.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String propStr = properties.getProperty("feed.socket.port");
        if (propStr != null && !propStr.isBlank()) {
            try {
                return Integer.parseInt(propStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_SOCKET_PORT;
    }
}
