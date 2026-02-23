package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (is == null) {
                throw new RuntimeException("No file test.properties in resources!");
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Config read error", e);
        }
    }

    public static String getBaseUrl() {
        return PROPERTIES.getProperty("base.url");
    }

    public static String getSupervisorLogin() {
        return PROPERTIES.getProperty("supervisor.login");
    }

    public static Long getSupervisorId() {
        return Long.parseLong(PROPERTIES.getProperty("supervisor.id"));
    }

    public static String getAdminLogin() {
        return PROPERTIES.getProperty("admin.login");
    }

    public static Long getAdminId() {
        return Long.parseLong(PROPERTIES.getProperty("admin.id"));
    }
}
