package com.automation.pages;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
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

    // CONFIRMED 2026-08-13 on a real device: naively wrapping search text in single quotes broke
    // on the very first apostrophe we hit ("couldn't use this number") — XPathParserException,
    // since the apostrophe closed the string literal early. English UI copy is full of these
    // ("don't", "isn't", "What's your name?"), so this needed a real fix, not a one-off escape.
    // Standard XPath 1.0 trick: no concat()-free way to embed both quote types in one literal, so
    // build via concat() when the value contains a single quote.
    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) {
                sb.append(", \"'\", ");
            }
        }
        return sb.append(")").toString();
    }

    protected By byText(String visibleText) {
        String literal = xpathLiteral(visibleText);
        if (driver instanceof AndroidDriver) {
            return AppiumBy.xpath("//*[@text=" + literal + " or @content-desc=" + literal + "]");
        }
        return AppiumBy.xpath("//*[@label=" + literal + " or @name=" + literal + " or @value=" + literal + "]");
    }

    protected By byTextContains(String partialText) {
        String literal = xpathLiteral(partialText);
        if (driver instanceof AndroidDriver) {
            return AppiumBy.xpath("//*[contains(@text," + literal + ") or contains(@content-desc," + literal + ")]");
        }
        return AppiumBy.xpath("//*[contains(@label," + literal + ") or contains(@name," + literal + ") or contains(@value," + literal + ")]");
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void tap(By locator) {
        // CONFIRMED 2026-08-10: the on-screen keyboard covers/intercepts taps on buttons below
        // a text field (e.g. "Continue" on the name/email entry screens) until it's dismissed —
        // a real element.click() still lands on the keyboard, not the button underneath, since
        // it's a real touch at the element's screen coordinates. Defensively hide the keyboard
        // (if shown) before every tap so callers don't each need to remember this.
        hideKeyboardIfShown();
        waitForClickable(locator).click();
    }

    // CONFIRMED 2026-08-13: some buttons are disabled behind a countdown longer than the default
    // 20s wait (e.g. OTP screen's "Resend code" — genuinely disabled for ~29s). A one-off longer
    // wait for those specific taps, rather than raising the default for everything.
    protected void tap(By locator, java.time.Duration timeout) {
        hideKeyboardIfShown();
        new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                .until(ExpectedConditions.elementToBeClickable(locator))
                .click();
    }

    protected void tapText(String visibleText, java.time.Duration timeout) {
        tap(byText(visibleText), timeout);
    }

    private void hideKeyboardIfShown() {
        try {
            if (driver instanceof HasOnScreenKeyboard hasKeyboard && hasKeyboard.isKeyboardShown()) {
                ((HidesKeyboard) driver).hideKeyboard();
            }
        } catch (Exception e) {
            // Best-effort — never fail a tap over keyboard-visibility detection not being
            // supported/erroring on a given platform/driver version.
        }
    }

    protected void tapText(String visibleText) {
        tap(byText(visibleText));
    }

    protected void typeInto(By locator, String value) {
        // CONFIRMED 2026-08-10 on a real device: sendKeys silently no-ops on a Compose EditText
        // that isn't already focused (e.g. the "Last name on ID" field on the name-entry screen —
        // typing into it right after the "First name" field, which WAS pre-focused by the app,
        // did nothing until the element was tapped first). Fields that happen to be pre-focused
        // on screen load (phone number, OTP) worked without this; anything else silently failed.
        // Tapping first makes this reliable everywhere, not just where we happened to get lucky.
        WebElement el = waitFor(locator);
        el.click();
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

    public void tapTextPublic(String visibleText, java.time.Duration timeout) {
        tapText(visibleText, timeout);
    }

    public void typeIntoPublic(By locator, String value) {
        typeInto(locator, value);
    }

    public boolean isDisplayedPublic(By locator) {
        return isDisplayed(locator);
    }

    public List<WebElement> findAllPublic(By locator) {
        return findAll(locator);
    }

    public AppiumDriver driverPublic() {
        return driver;
    }
}