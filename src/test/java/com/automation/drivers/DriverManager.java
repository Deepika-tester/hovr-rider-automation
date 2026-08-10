package com.automation.drivers;

import com.automation.utils.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.ios.options.wda.XcodeCertificate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Creates and tears down the Appium driver instance per test thread.
 * Uses a ThreadLocal so tests can run in parallel safely with TestNG.
 *
 * Platform (Android vs iOS) is decided by ConfigReader, which itself picks
 * config.properties or ios.properties based on -Dplatform=android|ios
 * (see ConfigReader). This class just builds whichever driver matches
 * platformName in the loaded config.
 */
public class DriverManager {

    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    public static AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void initDriver() {
        try {
            URL appiumUrl = new URL(ConfigReader.get("appiumServerUrl"));
            String platformName = ConfigReader.get("platformName");

            AppiumDriver driver = "iOS".equalsIgnoreCase(platformName)
                    ? new IOSDriver(appiumUrl, buildIosOptions())
                    : new AndroidDriver(appiumUrl, buildAndroidOptions());

            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(ConfigReader.getInt("implicitWaitSeconds")));

            driverThreadLocal.set(driver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL in config.properties", e);
        }
    }

    private static UiAutomator2Options buildAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setDeviceName(ConfigReader.get("deviceName"));
        options.setPlatformVersion(ConfigReader.get("platformVersion"));

        // Real device: set udid in config.properties (from `adb devices`) to target a specific phone.
        String udid = ConfigReader.get("udid", "");
        if (!udid.isBlank()) {
            options.setUdid(udid);
        }

        // App under test — installs/updates the APK on the device if present.
        // If the app is already installed and noReset=true, Appium will just launch it.
        File appFile = new File(ConfigReader.get("appPath"));
        if (appFile.exists()) {
            options.setApp(appFile.getAbsolutePath());
        }

        options.setAppPackage(ConfigReader.get("appPackage"));
        options.setAppActivity(ConfigReader.get("appActivity"));

        options.setNoReset(ConfigReader.getBoolean("noReset"));
        options.setFullReset(ConfigReader.getBoolean("fullReset"));
        options.setNewCommandTimeout(Duration.ofSeconds(120));

        return options;
    }

    private static XCUITestOptions buildIosOptions() {
        XCUITestOptions options = new XCUITestOptions();

        options.setDeviceName(ConfigReader.get("deviceName"));
        options.setPlatformVersion(ConfigReader.get("platformVersion"));

        // Real device: set udid in ios.properties (from `xcrun xctrace list devices`).
        // Simulator: leave blank and Appium boots/reuses a matching simulator by
        // deviceName + platformVersion.
        String udid = ConfigReader.get("udid", "");
        if (!udid.isBlank()) {
            options.setUdid(udid);
        }

        // .app (simulator) or .ipa (real device) — installs it if present.
        File appFile = new File(ConfigReader.get("appPath"));
        if (appFile.exists()) {
            options.setApp(appFile.getAbsolutePath());
        }

        options.setBundleId(ConfigReader.get("bundleId"));

        String xcodeOrgId = ConfigReader.get("xcodeOrgId", "");
        if (!xcodeOrgId.isBlank()) {
            options.setXcodeCertificate(new XcodeCertificate(
                    xcodeOrgId, ConfigReader.get("xcodeSigningId", "iPhone Developer")));
        }

        options.setWdaLocalPort(ConfigReader.getInt("wdaLocalPort"));

        options.setNoReset(ConfigReader.getBoolean("noReset"));
        options.setFullReset(ConfigReader.getBoolean("fullReset"));
        options.setNewCommandTimeout(Duration.ofSeconds(120));

        return options;
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}
