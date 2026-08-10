package com.automation.pages;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Shared helpers for all page objects. Platform-agnostic: driver is the common
 * AppiumDriver type, so the same page objects/step defs run against either
 * AndroidDriver or IOSDriver depending on -Dplatform (see DriverManager).
 *
 * IMPORTANT — Flutter app note:
 * HOVR's rider app is built with Flutter. Native Appium does not see Flutter
 * widgets as individual platform views the way it does for native screens — it
 * can only find elements exposed as accessibility nodes (Flutter's Semantics
 * layer). In practice this means:
 *   - byAccessibilityId(...) matches a widget's Semantics label (tooltip/label
 *     text on Android, accessibility label on iOS).
 *   - byText(...) / byTextContains(...) fall back to matching visible on-screen
 *     text — the underlying XML attribute differs by platform (Android:
 *     @text/@content-desc, iOS/XCUITest: @label/@name/@value), handled below.
 *   - Resource IDs (byId) generally do NOT work on Flutter screens.
 *
 * The exact accessibility labels used in step defs/page objects are best-effort
 * guesses based on the feature file wording and typical Flutter widget
 * conventions. Before running for real, open the app with Appium Inspector (or
 * `adb shell uiautomator dump` / Xcode Accessibility Inspector) and
 * confirm/replace each label — search for "VERIFY:" to find every one that
 * needs checking against the live app.
 */
public class BasePage {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    protected By byAccessibilityId(String label) {
        return AppiumBy.accessibilityId(label);
    }

    protected By byText(String visibleText) {
        if (driver instanceof AndroidDriver) {
            return AppiumBy.xpath("//*[@text='" + visibleText + "' or @content-desc='" + visibleText + "']");
        }
        return AppiumBy.xpath("//*[@label='" + visibleText + "' or @name='" + visibleText + "' or @value='" + visibleText + "']");
    }

    protected By byTextContains(String partialText) {
        if (driver instanceof AndroidDriver) {
            return AppiumBy.xpath("//*[contains(@text,'" + partialText + "') or contains(@content-desc,'" + partialText + "')]");
        }
        return AppiumBy.xpath("//*[contains(@label,'" + partialText + "') or contains(@name,'" + partialText + "') or contains(@value,'" + partialText + "')]");
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void tap(By locator) {
        waitForClickable(locator).click();
    }

    protected void tapText(String visibleText) {
        tap(byText(visibleText));
    }

    protected void typeInto(By locator, String value) {
        WebElement el = waitFor(locator);
        el.clear();
        el.sendKeys(value);
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitFor(locator).isDisplayed();
        } catch (NoSuchElementException | org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    // ---------------------------------------------------------------
    // Public wrappers so generic step-definition classes (a different
    // package from com.automation.pages) can reuse these helpers
    // without needing a dedicated page object for every ad-hoc step.
    // ---------------------------------------------------------------

    public By byAccessibilityIdPublic(String label) {
        return byAccessibilityId(label);
    }

    public By byTextPublic(String visibleText) {
        return byText(visibleText);
    }

    public By byTextContainsPublic(String partialText) {
        return byTextContains(partialText);
    }

    public WebElement waitForPublic(By locator) {
        return waitFor(locator);
    }

    public void tapPublic(By locator) {
        tap(locator);
    }

    public void tapTextPublic(String visibleText) {
        tapText(visibleText);
    }

    public void typeIntoPublic(By locator, String value) {
        typeInto(locator, value);
    }

    public boolean isDisplayedPublic(By locator) {
        return isDisplayed(locator);
    }

    public AppiumDriver driverPublic() {
        return driver;
    }
}