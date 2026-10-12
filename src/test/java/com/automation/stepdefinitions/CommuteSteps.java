package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.testng.Assert;

/**
 * Step definitions for src/test/resources/features/rider/rider_commute.feature (rider Co-Commute).
 *
 * Every locator here was read off a real screen (rider dev v6.3.0+588, emulator, 2026-10-12): the app is
 * Jetpack Compose, so rows/buttons expose their label as content-desc, and several carry a newline
 * ("My requests\n0", "Name\nAddress") -- so text checks use "contains", not exact match.
 * Generic steps ("I tap {string}", "I am a registered rider", ...) come from CommonSteps/ProfileSteps/
 * BookingSteps; only the commute-specific ones live here.
 */
public class CommuteSteps {

    private final BasePage ui = new BasePage() {};

    // Same shape as BookingSteps' suggestion rows: a clickable row whose content-desc is "<place>\n<address>".
    private static final By SUGGESTION_ROW = By.xpath(
            "//android.view.View[@clickable='true' and contains(@content-desc,'\n') "
                    + "and not(contains(@content-desc,'Tab '))]");
    // The commute form has two inputs: origin (pre-filled with the current address) and destination.
    private static final By DESTINATION_FIELD = By.xpath("(//android.widget.EditText)[2]");

    private boolean shown(String text) {
        return !ui.findAllPublic(ui.byTextContainsPublic(text)).isEmpty();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Lands on the Co-Commute page whatever a previous scenario left open (form, time picker, dialog). */
    private void ensureOnCommutePage() {
        if (shown("Commutes near you")) {
            return;
        }
        RegistrationSteps registration = new RegistrationSteps();
        boolean loggedOut = shown("Get Started") || shown("Enter your phone number");
        if (loggedOut) {
            registration.ensureOnRideHomeScreen(); // registers a fresh rider and reaches ride home
        } else {
            registration.relaunchToRideHome();     // clean slate: closes forms, pickers and dialogs
        }
        ui.tapTextPublic("Co-Commute");
        ui.waitForPublic(ui.byTextContainsPublic("Commutes near you"));
    }

    private void ensureOnPlanForm() {
        ensureOnCommutePage();
        ui.tapTextPublic("Post your own commute");
        ui.waitForPublic(ui.byTextContainsPublic("Plan a Co-Commute"));
    }

    // ---- navigation ----

    @When("I open the Co-Commute tab")
    public void i_open_the_co_commute_tab() {
        ui.tapTextPublic("Co-Commute");
        ui.waitForPublic(ui.byTextContainsPublic("Commutes near you"));
    }

    @Given("I am on the Co-Commute page")
    public void i_am_on_the_co_commute_page() {
        ensureOnCommutePage();
    }

    @Given("I am on the Plan a Co-Commute form")
    public void i_am_on_the_plan_a_co_commute_form() {
        ensureOnPlanForm();
    }

    // The middle tab is "My trips", but the app restores the last-used tab and has come up with that same
    // tab labelled "My requests" -- so it's located as the tab right after "Nearby journeys".
    private static final By MIDDLE_TAB = By.xpath(
            "(//android.widget.Button[@content-desc='Nearby journeys']"
                    + "/following-sibling::android.widget.Button)[1]");

    @When("I open the {string} tab")
    public void i_open_the_tab(String tab) {
        if ("My trips".equals(tab)) {
            ui.tapPublic(MIDDLE_TAB);
        } else {
            ui.tapTextPublic(tab);
        }
        sleep(1500);
    }

    @When("I tap the filter chip {string}")
    public void i_tap_the_filter_chip(String chip) {
        ui.tapTextPublic(chip);
        sleep(1500);
    }

    // ---- assertions ----

    @Then("I should see the Co-Commute page")
    public void i_should_see_the_co_commute_page() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Commutes near you")),
                "Expected the Co-Commute page ('Commutes near you')");
    }

    @And("I should see the tabs {string}, {string} and {string}")
    public void i_should_see_the_tabs(String a, String b, String c) {
        for (String tab : new String[] {a, b, c}) {
            boolean ok = "My trips".equals(tab)
                    ? ui.isDisplayedPublic(MIDDLE_TAB)
                    : ui.isDisplayedPublic(ui.byTextContainsPublic(tab));
            Assert.assertTrue(ok, "Expected tab: " + tab);
        }
    }

    @And("I should see the {string} button")
    public void i_should_see_the_button(String label) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(label)), "Expected button: " + label);
    }

    @And("I should see the filter chips {string}, {string} and {string}")
    public void i_should_see_the_filter_chips(String a, String b, String c) {
        for (String chip : new String[] {a, b, c}) {
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(chip)), "Expected filter chip: " + chip);
        }
    }

    // Apostrophe-free fragments are used in the feature on purpose: the app renders curly apostrophes.
    @Then("I should see the text {string}")
    public void i_should_see_the_text(String text) {
        // Search results / empty states settle asynchronously, so give them a moment before failing.
        ui.waitForPublic(ui.byTextContainsPublic(text));
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(text)), "Expected to see: " + text);
    }

    @Then("I should not see the text {string}")
    public void i_should_not_see_the_text(String text) {
        sleep(1000);
        Assert.assertFalse(shown(text), "Did not expect to see: " + text);
    }

    // ---- destination search in the commute form ----

    @When("I type the commute destination {string}")
    public void i_type_the_commute_destination(String address) {
        ui.typeIntoPublic(DESTINATION_FIELD, address);
    }

    @Then("I should see commute address suggestions")
    public void i_should_see_commute_address_suggestions() {
        Assert.assertTrue(ui.isDisplayedPublic(SUGGESTION_ROW), "Expected address suggestions");
    }

    @Then("I should not see commute address suggestions")
    public void i_should_not_see_commute_address_suggestions() {
        sleep(3000); // the search is asynchronous; give it time to answer before asserting nothing came back
        Assert.assertTrue(ui.findAllPublic(SUGGESTION_ROW).isEmpty(), "Did not expect any address suggestions");
    }

    @And("I pick the first commute suggestion")
    public void i_pick_the_first_commute_suggestion() {
        ui.waitForPublic(SUGGESTION_ROW);
        ui.tapPublic(SUGGESTION_ROW);
    }
}
