package com.automation.pages;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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

    // Same semantics as ExpectedConditions.visibilityOfElementLocated / elementToBeClickable (first
    // match that is displayed / displayed+enabled), but each poll first clears the "Update Required"
    // popup: once it is up it hides the whole screen behind it from the accessibility tree, so a plain
    // wait would just time out on a screen that is really there.
    protected WebElement waitFor(By locator) {
        return wait.until(d -> {
            dismissUpdatePopupIfShown();
            for (WebElement el : findAll(locator)) {
                try {
                    if (el.isDisplayed()) {
                        return el;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    // went away between find and check; the next poll will re-find it
                }
            }
            return null;
        });
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(d -> {
            dismissUpdatePopupIfShown();
            for (WebElement el : findAll(locator)) {
                try {
                    if (el.isDisplayed() && el.isEnabled()) {
                        return el;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    // see waitFor
                }
            }
            return null;
        });
    }

    // CONFIRMED 2026-10-12 (rider dev build +588): the "Update Required" popup (Skip / Update) is no
    // longer confined to the ride-home screen -- it also lands on top of the phone-entry screen and
    // steals focus mid-typing ("Cached elements ... do not exist in DOM anymore"). It arrives a few
    // seconds after launch, on any screen, so every tap and text entry first clears it.
    //
    // Also handles Google Play services' "Location Accuracy" prompt ("Turn on" / "No thanks"), which a
    // FRESH emulator shows on top of the app the first time it asks for location (CONFIRMED 2026-10-12:
    // it blocked every screen behind it, so the app looked stuck on "not in your area"). Accepting it
    // just enables the emulator's own location accuracy; real devices that already have it never show it.
    protected boolean dismissUpdatePopupIfShown() {
        if (!findAll(byTextContains("Location Accuracy")).isEmpty()) {
            List<WebElement> turnOn = findAll(byText("Turn on"));
            if (!turnOn.isEmpty()) {
                turnOn.get(0).click();
                return true;
            }
        }
        if (findAll(byText("Update Required")).isEmpty()) {
            return false;
        }
        List<WebElement> skip = findAll(byText("Skip"));
        if (skip.isEmpty()) {
            return false;
        }
        skip.get(0).click();
        return true;
    }

    protected void tap(By locator) {
        dismissUpdatePopupIfShown();
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

    // CONFIRMED 2026-09-23 on a real device via a clean A/B test (same screen, same filled-in
    // state, only the dismiss method differed): Appium's own hideKeyboard() command does
    // something more aggressive than a real user's keyboard-dismiss action on the Adyen
    // card-entry dialog specifically — it closes the WHOLE dialog and discards every entered
    // field, not just the keyboard. What a real user actually gets from a single system BACK
    // press (equivalent to the nav bar's keyboard-dismiss chevron) hides ONLY the keyboard and
    // leaves the dialog and its data intact. This had been misreported as an app bug before this
    // was root-caused — it's specific to Appium's hideKeyboard() implementation, not real user
    // interaction, so a raw back-key press is used here instead to avoid the false positive
    // entirely (and matches real usage more closely everywhere else too, not just this dialog).
    private void hideKeyboardIfShown() {
        try {
            if (driver instanceof HasOnScreenKeyboard hasKeyboard && hasKeyboard.isKeyboardShown()) {
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                } else {
                    ((HidesKeyboard) driver).hideKeyboard();
                }
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
        dismissUpdatePopupIfShown();
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

    // Existence checks must answer immediately: with the driver's implicit wait (10s) findElements
    // blocks that long every time nothing matches, which turned polling loops (e.g. waiting for a
    // screen to settle, or "is a popup showing?") into multi-minute waits. Drop the implicit wait for
    // the lookup and restore it afterwards.
    protected List<WebElement> findAll(By locator) {
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            return driver.findElements(locator);
        } finally {
            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(com.automation.utils.ConfigReader.getInt("implicitWaitSeconds")));
        }
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

    public boolean dismissUpdatePopupPublic() {
        return dismissUpdatePopupIfShown();
    }

    public List<WebElement> findAllPublic(By locator) {
        return findAll(locator);
    }

    public AppiumDriver driverPublic() {
        return driver;
    }
}