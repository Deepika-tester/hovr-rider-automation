package com.automation.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads src/test/resources/config/api.properties — kept separate from
 * com.automation.utils.ConfigReader (Appium mobile capabilities) since the API
 * suite is a different concern (plain HTTP client config) and runs independently
 * of platform (android/ios) selection.
 */
public class ApiConfigReader {

    private static final String CONFIG_PATH = "src/test/resources/config/api.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config from " + CONFIG_PATH, e);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing key in api.properties: " + key);
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
}
