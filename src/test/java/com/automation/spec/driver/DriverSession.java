package com.automation.spec.driver;

import com.automation.spec.rider.SpecUi;
import com.automation.utils.ConfigReader;
import io.appium.java_client.InteractsWithApps;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Gets the driver app onto a known screen. Account-area scenarios sign in as the approved test driver (Virat, a GTA
 * driver supplied by the project owner; staging OTP 111111 works for +1 numbers while the emulator GPS is in Toronto)
 * instead of registering a new driver every time.
 */
final class DriverSession {

    static final String APP = "com.example.hovr_driver";

    private DriverSession() {}

    static boolean onHome(SpecUi ui) {
        return ui.seenExact("Show menu");
    }

    static void relaunch(SpecUi ui) {
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp(APP);
        apps.activateApp(APP);
    }

    static boolean waitFor(SpecUi ui, int seconds, String... texts) {
        return waitForAny(ui, seconds, texts);
    }

    private static boolean waitForAny(SpecUi ui, int seconds, String... texts) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            ui.popup();
            for (String t : texts) {
                if (ui.seen(t)) {
                    return true;
                }
            }
            ui.sleep(500);
        }
        return false;
    }

    /**
     * Types a national number on the phone screen and continues to the OTP screen. The backend refuses a second code for
     * the same number within a cooldown ("please wait 18 seconds before requesting another code"); the field is then
     * cleared by the app, so wait the stated time and type it again.
     */
    static void enterPhone(SpecUi ui, String national) {
        ui.waitSeen("Enter your phone number", 20);
        for (int attempt = 0; attempt < 2; attempt++) {
            WebElement field = ui.visible(By.className("android.widget.EditText"), 15);
            Assert.assertNotNull(field, "No phone field on the sign-in screen");
            field.click();
            field.clear();
            field.sendKeys(national);
            ui.tapLabel("Continue");
            long end = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < end) {
                if (ui.seen("digit code sent")) {
                    return;
                }
                ui.sleep(500);
            }
            // The cooldown toast is not exposed to Appium, so a screen that is still the phone screen with an emptied
            // field means "please wait N seconds": stay quiet for longer than the cooldown before asking again.
            if (attempt == 0) {
                ui.sleep(40_000);
            }
        }
        Assert.fail("The OTP request was refused (cooldown or the daily OTP limit for this number); not asking again so the "
                + "limit is not burned. Screen: " + ui.driverPublic().getPageSource().length() + " bytes");
    }

    static void enterOtp(SpecUi ui, String code) {
        Assert.assertTrue(ui.waitSeen("digit code sent", 25), "OTP screen not shown");
        WebElement otp = ui.visible(By.className("android.widget.EditText"), 10);
        otp.click();
        otp.clear();
        otp.sendKeys(code); // the 6th digit submits
    }

    /** From the landing page: Get Started and the phone screen. */
    static void toPhoneScreen(SpecUi ui) {
        if (!ui.seen("Enter your phone number")) {
            ui.tapLabel("Get Started");
        }
        Assert.assertTrue(ui.waitSeen("Enter your phone number", 15), "Phone screen not shown");
    }

    static String testPhone() {
        String p = ConfigReader.get("testDriverPhone", "");
        Assert.assertFalse(p.isBlank(), "testDriverPhone is not configured for this platform");
        return p;
    }

    static void signInAsTestDriver(SpecUi ui) {
        toPhoneScreen(ui);
        enterPhone(ui, testPhone());
        enterOtp(ui, "111111");
    }

    /** Lands on the driver home (the screen with the "Show menu" button), signing in when logged out. */
    static void ensureHome(SpecUi ui) {
        if (onHome(ui)) {
            return;
        }
        if (!ui.seen("Get Started") && !ui.seen("Enter your phone number")) {
            relaunch(ui);
        }
        waitForAny(ui, 30, "Show menu", "Get Started", "Enter your phone number");
        if (onHome(ui)) {
            return;
        }
        if (ui.seen("Get Started") || ui.seen("Enter your phone number")) {
            signInAsTestDriver(ui);
        }
        Assert.assertTrue(ui.waitSeen("Show menu", 45), "Driver home not shown after signing in");
    }

    static void openMenu(SpecUi ui) {
        ensureHome(ui);
        ui.tapLabel("Show menu");
        ui.sleep(1000);
    }

    /** Opens an item of the side drawer, from wherever the app is. */
    static void drawer(SpecUi ui, String item) {
        goHome(ui);
        openMenu(ui);
        ui.tapLabel(item);
        ui.sleep(1800);
    }

    /** Backs out of sub-screens until the home screen is showing again. */
    static void goHome(SpecUi ui) {
        for (int i = 0; i < 6 && !onHome(ui); i++) {
            ui.popup();
            if (ui.seen("Get Started")) {
                break;
            }
            ui.back();
        }
        ensureHome(ui);
    }
}
