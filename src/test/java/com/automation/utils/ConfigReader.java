package com.automation.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads the platform-specific config.properties once and exposes typed getters.
 *
 * Platform is selected via -Dplatform=android|ios (or the PLATFORM env var as a
 * fallback), defaulting to "android" so existing invocations keep working
 * unchanged. Each platform's file lives at src/test/resources/config/<file>.
 */
public class ConfigReader {

    private static final String CONFIG_DIR = "src/test/resources/config/";

    private static final Properties properties = new Properties();

    static {
        String platform = resolvePlatform();
        String fileName = "ios".equals(platform) ? "ios.properties" : "config.properties";
        String path = CONFIG_DIR + fileName;

        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config from " + path, e);
        }
    }

    private static String resolvePlatform() {
        String platform = System.getProperty("platform");
        if (platform == null || platform.isBlank()) {
            platform = System.getenv("PLATFORM");
        }
        return platform == null ? "android" : platform.trim().toLowerCase();
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing key in config.properties: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
