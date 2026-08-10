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
 * IMPORTANT — UI toolkit note (corrected 2026-08-10 against a real device dump —
 * the app is NOT Flutter, despite earlier comments in this codebase claiming so):
 * HOVR's rider app is built with Jetpack Compose (native Android —
 * androidx.compose.ui.platform.ComposeView at the root of every screen we've
 * inspected so far). Compose's accessibility tree behaves differently from both
 * Flutter and classic Android View-based screens:
 *   - byAccessibilityId(...) (content-desc) DOES work — confirmed present on
 *     several elements (e.g. "Add new place", "Name", "Address", "Save" on the
 *     saved-places screen) — better than initially assumed.
 *   - byText(...) / byTextContains(...) (Android @text) also works — Compose
 *     Text composables render as TextView with a real text attribute.
 *   - resource-id is consistently EMPTY across every screen inspected so far —
 *     Compose only exposes it if the app opts in via
 *     `Modifier.semantics { testTagsAsResourceId = true }` + testTag(...), which
 *     this app does not appear to use. Don't rely on resource-id locators here.
 *   - Input fields (EditText) are frequently unlabeled themselves — the
 *     content-desc/text often sits on a sibling/wrapper "label" node instead of
 *     the actual editable element. When there's exactly one EditText on screen,
 *     targeting By.className("android.widget.EditText") is more reliable than
 *     guessing an accessibility id for the field itself.
 *
 * The exact locators used in step defs/page objects are still best-effort
 * guesses pending confirmation, marked "VERIFY:". Confirm each one against the
 * live app — pull the page source via GET /session/:id/source on a running
 * Appium session (or Appium Inspector) rather than assuming Flutter/native-View
 * conventions.
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