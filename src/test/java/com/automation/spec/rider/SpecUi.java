package com.automation.spec.rider;

import com.automation.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Locator/wait helpers for the spec steps, on top of BasePage. Matching is "contains" on text OR content-desc
 * (the app puts most labels in content-desc, often with a second line) and tolerant of curly vs straight
 * apostrophes -- the app renders "What’s your name?" while feature files are written either way.
 */
public class SpecUi extends BasePage {

    private static String lit(String s) {
        if (!s.contains("'")) {
            return "'" + s + "'";
        }
        if (!s.contains("\"")) {
            return "\"" + s + "\"";
        }
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = s.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) {
                sb.append(", \"'\", ");
            }
        }
        return sb.append(")").toString();
    }

    private static String norm(String s) {
        return s.replace('’', '\'').replace('‘', '\'');
    }

    private static String attr(String name) {
        return "translate(" + name + ",\"’‘\",\"''\")";
    }

    public static By contains(String text) {
        String l = lit(norm(text));
        // @hint matters for input fields whose placeholder is the text under test ("Add stop 1", "Where to?")
        return By.xpath("//*[contains(" + attr("@text") + "," + l + ") or contains(" + attr("@content-desc") + "," + l
                + ") or contains(" + attr("@hint") + "," + l + ")]");
    }

    public static By exact(String text) {
        String l = lit(norm(text));
        return By.xpath("//*[" + attr("@text") + "=" + l + " or " + attr("@content-desc") + "=" + l + "]");
    }

    public boolean popup() {
        return dismissUpdatePopupIfShown();
    }

    public boolean seen(String text) {
        return !findAll(contains(text)).isEmpty();
    }

    public boolean seenExact(String text) {
        return !findAll(exact(text)).isEmpty();
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Polls (dismissing the update / location popups on the way) until the text is on screen. */
    public boolean waitSeen(String text, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        do {
            dismissUpdatePopupIfShown();
            if (seen(text)) {
                return true;
            }
            sleep(500);
        } while (System.currentTimeMillis() < end);
        return false;
    }

    public boolean waitGone(String text, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        do {
            if (!seen(text)) {
                return true;
            }
            sleep(500);
        } while (System.currentTimeMillis() < end);
        return false;
    }

    /** Hides the on-screen keyboard with a plain BACK press (Appium's hideKeyboard() closes some dialogs outright). */
    public void hideKeyboard() {
        try {
            if (driver instanceof io.appium.java_client.HasOnScreenKeyboard k && k.isKeyboardShown()
                    && driver instanceof io.appium.java_client.android.AndroidDriver a) {
                a.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
                sleep(500);
            }
        } catch (RuntimeException ignored) {
            // best effort
        }
    }

    /** Taps the element labelled `text`: an exact match if there is one, otherwise the first that contains it. */
    public void tapLabel(String text) {
        if (!tryTapLabel(text, 20)) {
            throw new AssertionError("Could not find anything to tap labelled: " + text);
        }
    }

    public boolean tryTapLabel(String text, int seconds) {
        hideKeyboard(); // buttons such as "Continue" are not even in the tree while the keyboard covers them
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            dismissUpdatePopupIfShown();
            for (By by : new By[] {exact(text), contains(text)}) {
                for (WebElement el : findAll(by)) {
                    try {
                        if (el.isDisplayed()) {
                            el.click();
                            return true;
                        }
                    } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                        // re-find on the next pass
                    }
                }
            }
            sleep(500);
        }
        return false;
    }

    public List<WebElement> all(By by) {
        return findAll(by);
    }

    public void back() {
        driver.navigate().back();
        sleep(1200);
    }

    public org.openqa.selenium.WebElement visible(By by, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        do {
            dismissUpdatePopupIfShown();
            for (WebElement el : findAll(by)) {
                try {
                    if (el.isDisplayed()) {
                        return el;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    // next pass
                }
            }
            sleep(400);
        } while (System.currentTimeMillis() < end);
        return null;
    }

    /** Taps a screen point (for controls that have no accessibility node of their own, such as icon buttons in a row). */
    public void tapAt(int x, int y) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("mobile: clickGesture",
                java.util.Map.of("x", x, "y", y));
        sleep(800);
    }

    /** Taps the right-hand end of an element's row, where such icon buttons sit (inset px from the right edge). */
    public void tapRightEdgeOf(WebElement row, int inset) {
        org.openqa.selenium.Rectangle r = row.getRect();
        tapAt(r.x + r.width - inset, r.y + r.height / 2);
    }

    /** Swipes the page up until `text` is on screen (at most `max` swipes). */
    public boolean scrollTo(String text, int max) {
        for (int i = 0; i <= max; i++) {
            if (seen(text)) {
                return true;
            }
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("mobile: swipeGesture", java.util.Map.of(
                    "left", size.width / 4, "top", size.height / 4, "width", size.width / 2, "height", size.height / 2,
                    "direction", "up", "percent", 0.6));
            sleep(700);
        }
        return seen(text);
    }
}
