package com.automation.spec.rider;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Wording that every rider feature file shares: Backgrounds ("I am signed in as a rider"), tab/screen navigation and
 * the generic "I should see ..." / "I tap ..." steps.
 *
 * Assertions wait a few seconds (screens and toasts settle asynchronously) and, when they fail, say what the screen
 * actually shows -- the real texts are what the report needs to compare with the spec.
 */
public class CommonSpecSteps {

    private final SpecUi ui = new SpecUi();

    // Must run AFTER Hooks.setUp (default order 10000): BasePage grabs the driver when this class is constructed.
    @Before(order = 20000)
    public void resetContext() {
        SpecContext.reset();
        ApiNote.clear();
    }

    /** Undo what a scenario changed on the device so the next one starts at the Mohali test location with permissions. */
    @io.cucumber.java.After
    public void restoreDevice() {
        if (SpecAdb.moved()) {
            SpecAdb.geoHome();
        }
        SpecAdb.shell("pm", "grant", "production.hovr.customer", "android.permission.POST_NOTIFICATIONS");
        SpecAdb.shell("pm", "grant", "production.hovr.customer", "android.permission.ACCESS_FINE_LOCATION");
        SpecAdb.shell("pm", "grant", "production.hovr.customer", "android.permission.ACCESS_COARSE_LOCATION");
        if (SpecAdb.locationOff()) {
            SpecAdb.setLocationMode(3);
            SpecAdb.geoHome();
        }
    }

    // ---- Backgrounds / session ----

    @Given("I am signed in as a rider")
    public void i_am_signed_in_as_a_rider() {
        SpecSession.ensureSignedIn(ui);
    }

    @Given("location permission is granted")
    public void location_permission_is_granted() {
        // Granted for the whole run (DriverManager grants it with adb and the emulator location is fixed to Mohali).
    }

    @Given("the device has an internet connection")
    public void the_device_has_an_internet_connection() {
        com.automation.drivers.DriverManager.setNetworkEnabled(true);
    }

    @Given("I am on the home screen")
    public void i_am_on_the_home_screen() {
        SpecSession.goRide(ui);
    }

    @Given("I am on the {string} tab")
    public void i_am_on_the_tab(String tab) {
        SpecSession.open(ui, tab);
    }

    @Given("I open the {string} tab")
    public void i_open_the_tab(String tab) {
        SpecSession.open(ui, tab);
    }

    @Given("I open {string}")
    public void i_open(String screen) {
        SpecSession.open(ui, screen);
    }

    // ---- taps ----

    @When("I tap {string}")
    public void i_tap(String label) {
        String l = SpecContext.expand(label);
        if (!ui.tryTapLabel(l, 6) && !SpecAliases.tap(ui, l)) {
            ui.tapLabel(l); // not an alias: keep waiting (slow screens) and fail with the usual message
        }
        ui.sleep(1200);
    }

    @When("I select {string}")
    public void i_select(String option) {
        ui.tapLabel(SpecContext.expand(option));
        ui.sleep(1200);
    }

    @When("I enter {string}")
    public void i_enter(String value) {
        // "FRIEND5" is an example code that does not exist on the dev backend ("Failed to redeem referral code!"), so a
        // genuine code is fetched from a throw-away rider created through the API.
        String text = "FRIEND5".equals(value) ? com.automation.spec.api.RiderSession.freshReferralCode() : value;
        ui.typeIntoPublic(org.openqa.selenium.By.xpath("(//android.widget.EditText)[last()]"), text);
    }

    @Then("I should see {string} in results")
    public void i_should_see_in_results(String text) {
        assertSeen(text);
    }

    // ---- generic assertions ----

    private String screenSummary() {
        try {
            String src = ui.driverPublic().getPageSource();
            java.util.LinkedHashSet<String> texts = new java.util.LinkedHashSet<>();
            java.util.regex.Matcher node = java.util.regex.Pattern.compile("<[^>]*package=\"(?!com\\.android\\.systemui)[^\"]+\"[^>]*>")
                    .matcher(src);
            java.util.regex.Pattern label = java.util.regex.Pattern.compile("(?:text|content-desc)=\"([^\"]+)\"");
            while (node.find() && texts.size() < 16) {
                java.util.regex.Matcher m = label.matcher(node.group());
                while (m.find() && texts.size() < 16) {
                    texts.add(m.group(1).replace("&#10;", " | "));
                }
            }
            return texts.isEmpty() ? "(no app text visible -- app not in the foreground?)" : String.join(" // ", texts);
        } catch (RuntimeException e) {
            return "(screen unreadable)";
        }
    }

    public void assertSeen(String text) {
        String expected = SpecAliases.text(SpecContext.expand(text));
        if (SpecContext.sawTransientText(expected)) {
            return;
        }
        Assert.assertTrue(ui.waitSeen(expected, 12),
                "Expected to see \"" + expected + "\" but the screen shows: " + screenSummary());
    }

    @Then("I should see {string}")
    public void i_should_see(String text) {
        assertSeen(text);
    }

    @Then("I should see {string} and {string}")
    public void i_should_see_two(String a, String b) {
        assertSeen(a);
        assertSeen(b);
    }

    @Then("I should see {string}, {string} and {string}")
    public void i_should_see_three(String a, String b, String c) {
        assertSeen(a);
        assertSeen(b);
        assertSeen(c);
    }

    @Then("I should see {string} or {string}")
    public void i_should_see_either(String a, String b) {
        String x = SpecContext.expand(a);
        String y = SpecContext.expand(b);
        long end = System.currentTimeMillis() + 12_000;
        while (System.currentTimeMillis() < end) {
            if (ui.seen(x) || ui.seen(y)) {
                return;
            }
            ui.sleep(500);
        }
        Assert.fail("Expected to see \"" + x + "\" or \"" + y + "\" but the screen shows: " + screenSummary());
    }

    @Then("I should not see {string}")
    public void i_should_not_see(String text) {
        ui.sleep(1000);
        Assert.assertFalse(ui.seen(SpecContext.expand(text)), "Did not expect to see: " + text);
    }
}
