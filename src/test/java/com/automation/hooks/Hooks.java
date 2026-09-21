package com.automation.hooks;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Hooks {

    @Before
    public void setUp() {
        DriverManager.initDriver();
        dismissKnownNagDialogs();
    }

    // Even with location permission granted (see DriverManager), a real device without an
    // immediate GPS fix shows the app's own "Location unavailable... Nevermind" dialog on first
    // launch — confirmed 2026-08-10. Best-effort, non-fatal: if it's not there, this is a no-op.
    private void dismissKnownNagDialogs() {
        try {
            List<WebElement> nevermind = DriverManager.getDriver()
                    .findElements(AppiumBy.accessibilityId("Nevermind"));
            if (!nevermind.isEmpty()) {
                nevermind.get(0).click();
            }
        } catch (Exception e) {
            // Best-effort only — never fail a scenario over a dialog that wasn't there.
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && DriverManager.getDriver() != null) {
            byte[] screenshot = DriverManager.getDriver().getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
        }
        // Defensive, unconditional: the "no network connectivity" scenario (see
        // RegistrationSteps) disables wifi/data via adb. If it fails partway — before its own
        // retry assertion re-enables them — the device would stay offline for every scenario
        // after it. Always restoring here is a no-op cost when nothing was ever disabled.
        DriverManager.setNetworkEnabled(true);
        DriverManager.quitDriver();
    }
}