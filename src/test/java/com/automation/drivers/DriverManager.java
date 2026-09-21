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

            if (!"iOS".equalsIgnoreCase(platformName)) {
                grantAndroidRuntimePermissions();
            }

            AppiumDriver driver = createSessionWithRetry(appiumUrl, platformName);

            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(ConfigReader.getInt("implicitWaitSeconds")));

            driverThreadLocal.set(driver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL in config.properties", e);
        }
    }

    // CONFIRMED 2026-09-18 on this same physical device, again: mid-run USB/adb dropouts are a
    // real, recurring hardware issue here, not a code problem (adb devices shows the device gone
    // then back healthy on its own, transport_id changing each time). Without this retry, one
    // blip during ANY scenario's session creation cascades into every remaining scenario in the
    // batch failing with SessionNotCreatedException, since each scenario's @Before tries to
    // create a brand new session (see Hooks) and none of them recovered on their own. Three
    // attempts with an `adb reconnect` and a pause between each gives the device roughly the
    // window it takes to come back on its own, without letting a truly-disconnected device
    // (unplugged, etc.) hang a scenario forever.
    private static AppiumDriver createSessionWithRetry(URL appiumUrl, String platformName) {
        final int maxAttempts = 3;
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return "iOS".equalsIgnoreCase(platformName)
                        ? new IOSDriver(appiumUrl, buildIosOptions())
                        : new AndroidDriver(appiumUrl, buildAndroidOptions());
            } catch (RuntimeException e) {
                lastFailure = e;
                if (attempt == maxAttempts) {
                    break;
                }
                String udid = ConfigReader.get("udid", "");
                if (!udid.isBlank()) {
                    try {
                        new ProcessBuilder("adb", "-s", udid, "reconnect")
                                .redirectErrorStream(true)
                                .start()
                                .waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (Exception ignored) {
                        // Best-effort — fall through to the sleep/retry regardless.
                    }
                }
                try {
                    Thread.sleep(20_000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw lastFailure;
    }

    // Belt-and-suspenders alongside UiAutomator2Options#setAutoGrantPermissions: that capability
    // only re-grants permissions at APP INSTALL time. With noReset=true (the common case — app
    // already installed) plus a `pm clear` between runs (common when testing registration from a
    // logged-out state), permissions get reset by the clear but nothing re-grants them, since no
    // install happens. Confirmed as a real blocker on a real device 2026-08-10 — the OS
    // "Allow location?" dialog blocked the very first screen. Calling `adb shell pm grant`
    // directly, every time, works regardless of install/reset state.
    private static void grantAndroidRuntimePermissions() {
        String[] permissions = {
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.POST_NOTIFICATIONS"
        };
        String appPackage = ConfigReader.get("appPackage", "");
        for (String permission : permissions) {
            runAdbShell("pm", "grant", appPackage, permission);
        }
    }

    // Registration scenarios need a genuinely logged-out app to test navigation from the landing
    // page — bringing an already-running session to the foreground (activateApp) is NOT enough,
    // since a previously-completed registration/login in this same app install would just resume
    // on the ride home screen. Only a real data clear + relaunch reliably lands back on the
    // landing page. Step defs call this from their "Given I am on X screen" methods when the
    // expected screen isn't already showing (see RegistrationSteps#ensureOnLandingPage).
    public static void resetAppToLoggedOutState() {
        String appPackage = ConfigReader.get("appPackage", "");
        if (appPackage.isBlank()) {
            return;
        }
        runAdbShell("pm", "clear", appPackage);
        grantAndroidRuntimePermissions(); // pm clear wipes previously-granted permissions too
        ((io.appium.java_client.InteractsWithApps) getDriver()).activateApp(appPackage);
    }

    // Backs the "Registration with network timeout during OTP request" scenario — Appium has no
    // capability for this, so it's plain adb like resetAppToLoggedOutState above. Toggles both
    // radios since the device may be on wifi or mobile data depending on how it's provisioned;
    // `svc wifi`/`svc data` need no root and survive app restarts, unlike airplane-mode content
    // settings which need a broadcast to actually take effect.
    public static void setNetworkEnabled(boolean enabled) {
        String state = enabled ? "enable" : "disable";
        runAdbShell("svc", "wifi", state);
        runAdbShell("svc", "data", state);
    }

    private static void runAdbShell(String... shellArgs) {
        String udid = ConfigReader.get("udid", "");
        if (udid.isBlank()) {
            return;
        }
        try {
            String[] fullCommand = new String[shellArgs.length + 4];
            fullCommand[0] = "adb";
            fullCommand[1] = "-s";
            fullCommand[2] = udid;
            fullCommand[3] = "shell";
            System.arraycopy(shellArgs, 0, fullCommand, 4, shellArgs.length);
            new ProcessBuilder(fullCommand)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            // Non-fatal — adb may be unavailable in this environment (e.g. a cloud device farm
            // reached over a non-adb protocol). Don't fail driver init/reset over it.
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

        // Auto-grant every runtime permission declared in the manifest (location,
        // notifications, etc.) at session start — without this, a fresh install/data-clear
        // blocks the very first screen behind an OS "Allow location?" dialog our step defs
        // don't know how to dismiss. Confirmed necessary on a real device 2026-08-10.
        options.setAutoGrantPermissions(true);

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
