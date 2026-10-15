package com.automation.spec.rider;

import com.automation.stepdefinitions.RegistrationSteps;
import org.openqa.selenium.By;

/**
 * Session/navigation helpers shared by every spec step class: get a signed-in rider onto a screen with the bottom
 * tabs, and walk Account-area routes. A signed-in rider is reused between scenarios (noReset), so most scenarios
 * only need a few BACK presses to get back to the tabs; a relaunch or a fresh registration happens only when the
 * app is logged out, stuck, or on the "not in your area" page.
 */
public final class SpecSession {

    private SpecSession() {}

    public static final By TAB_RIDE = By.xpath("//*[contains(@content-desc,'Tab 1 of 3')]");
    public static final By TAB_TRIPS = By.xpath("//*[contains(@content-desc,'Tab 2 of 3')]");
    public static final By TAB_ACCOUNT = By.xpath("//*[contains(@content-desc,'Tab 3 of 3')]");

    public static boolean tabsVisible(SpecUi ui) {
        return !ui.all(TAB_ACCOUNT).isEmpty();
    }

    private static boolean loggedOut(SpecUi ui) {
        return ui.seen("Get Started") || ui.seen("Enter your phone number") || ui.seen("digit code sent")
                || ui.seen("your name?") || ui.seen("your email?") || ui.seen("Terms of Service")
                || ui.seen("referral code?") || ui.seen("Welcome to HOVR")
                // the sign-up payment step has a Skip button; the wallet's "Payment Methods" list does not
                || (ui.seen("Payment Methods") && ui.seen("Skip") && !ui.seen("balance"));
    }

    /** Leaves the app on a screen that shows the bottom tabs, signed in as a rider. */
    public static void ensureSignedIn(SpecUi ui) {
        RegistrationSteps registration = new RegistrationSteps();
        for (int i = 0; i < 6; i++) {
            ui.popup();
            if (tabsVisible(ui)) {
                return;
            }
            if (loggedOut(ui) && !ui.seen("Earn by driving")) {
                if (SpecContext.keepGuide) {
                    registration.i_am_on_the_welcome_screen();
                    ui.tapLabel("Ride now");
                    ui.waitSeen("Where to?", 40); // leave the first-visit swipe guide up
                    return;
                }
                registration.ensureOnRideHomeScreen(); // registers a fresh rider when logged out
                if (tabsVisible(ui)) {
                    return;
                }
            }
            ui.back();
        }
        registration.relaunchToRideHome();
        if (!tabsVisible(ui)) {
            registration.ensureOnRideHomeScreen();
        }
    }

    public static void goRide(SpecUi ui) {
        ensureSignedIn(ui);
        if (!ui.seen("Where to?")) {
            ui.tapPublic(TAB_RIDE);
            ui.waitSeen("Where to?", 15);
        }
    }

    public static void goAccount(SpecUi ui) {
        ensureSignedIn(ui);
        if (!ui.seen("Earn by driving")) {
            ui.tapPublic(TAB_ACCOUNT);
            ui.waitSeen("Earn by driving", 15);
        }
    }

    public static void goTrips(SpecUi ui) {
        ensureSignedIn(ui);
        ui.tapPublic(TAB_TRIPS);
        ui.waitSeen("Upcoming", 15);
    }

    /** Makes sure a payment method is the default (Google Pay, which every rider has), then returns to ride home. */
    public static void ensureDefaultPayment(SpecUi ui) {
        open(ui, "Payment Methods");
        boolean hasDefault = ui.all(By.xpath("//*[contains(@content-desc,'Default')]")).size() > 0;
        if (!hasDefault) {
            ui.tapLabel("Google Pay");
            ui.waitSeen("Default", 15);
        }
        goRide(ui);
    }

    /** Opens a named screen the way the feature files refer to it ("Help", "Wallet", "Saved Places", ...). */
    public static void open(SpecUi ui, String screen) {
        switch (screen) {
            case "Help", "Support tickets" -> {
                goAccount(ui);
                ui.tapLabel("Help");
                ui.waitSeen("Support tickets", 15);
            }
            case "Wallet" -> {
                goAccount(ui);
                ui.tapLabel("Wallet");
                ui.waitSeen("balance", 40); // "No balance yet" / "HOVR balance"
            }
            case "Payment Methods" -> {
                open(ui, "Wallet");
                // The wallet opens on a balance page with a "Payment Methods" button; the list is one tap further.
                if (!ui.seen("Add payment method")) {
                    ui.tapLabel("Payment Methods");
                }
                ui.waitSeen("Add payment method", 40);
            }
            case "Edit saved place" -> {
                // The first saved place row: a Button whose label is "<name>\n<address>".
                org.openqa.selenium.WebElement row = ui.visible(
                        org.openqa.selenium.By.xpath("//android.widget.Button[contains(@content-desc,'\n')]"), 15);
                if (row == null) {
                    throw new AssertionError("There is no saved place to open");
                }
                row.click();
                ui.waitSeen("Edit saved place", 15);
            }
            case "Referral progress" -> {
                open(ui, "Refer and earn");
                ui.scrollTo("View progress", 4);
                ui.tapLabel("View progress");
                ui.waitSeen("Referral progress", 15);
            }
            case "Inbox" -> {
                goAccount(ui);
                ui.tapLabel("Inbox");
                ui.waitSeen("Broadcast", 15);
            }
            case "Refer and earn" -> {
                goAccount(ui);
                ui.tapLabel("Refer a friend");
                ui.waitSeen("Refer and earn", 15);
            }
            case "Settings" -> {
                goAccount(ui);
                ui.tapLabel("Settings");
                ui.waitSeen("Shortcuts", 15);
            }
            case "Saved Places" -> {
                open(ui, "Settings");
                ui.tapLabel("Home");
                ui.waitSeen("Saved Places", 15);
                ui.sleep(1500);
            }
            case "Co-Commute" -> {
                goRide(ui);
                ui.tapLabel("Co-Commute");
                ui.waitSeen("Commutes near you", 20);
                ui.tapLabel("Nearby journeys"); // the page reopens on the last-used tab
            }
            case "Account" -> goAccount(ui);
            case "Trips" -> goTrips(ui);
            default -> ui.tapLabel(screen);
        }
    }
}
