package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final Properties PROPERTIES = new Properties();
    private static final String BASE_URL_ENV_VAR_KEY = "BASE_URL";
    private static final String PROPERTIES_FILE_NAME = "test.properties";

    private enum ConfigKey {
        BASE_URL("base.url"),
        SUPERVISOR_LOGIN("supervisor.login"),
        SUPERVISOR_ID("supervisor.id"),
        ADMIN_LOGIN("admin.login"),
        ADMIN_ID("admin.id");

        private final String key;

        ConfigKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    static {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (is == null) {
                throw new RuntimeException("No file %s in resources!".formatted(PROPERTIES_FILE_NAME));
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Config read error", e);
        }
    }

    public static String getBaseUrl() {
        String baseUrl = System.getenv(BASE_URL_ENV_VAR_KEY);
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        return PROPERTIES.getProperty(ConfigKey.BASE_URL.getKey());
    }

    public static String getSupervisorLogin() {
        return PROPERTIES.getProperty(ConfigKey.SUPERVISOR_LOGIN.getKey());
    }

    public static Long getSupervisorId() {
        return Long.parseLong(PROPERTIES.getProperty(ConfigKey.SUPERVISOR_ID.getKey()));
    }

    public static String getAdminLogin() {
        return PROPERTIES.getProperty(ConfigKey.ADMIN_LOGIN.getKey());
    }

    public static Long getAdminId() {
        return Long.parseLong(PROPERTIES.getProperty(ConfigKey.ADMIN_ID.getKey()));
    }
}
