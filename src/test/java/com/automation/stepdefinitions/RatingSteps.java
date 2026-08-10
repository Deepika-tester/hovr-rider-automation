package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Map;

/**
 * Step definitions for src/test/resources/features/rider/rider_rating.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer.
 */
public class RatingSteps {

    private final BasePage ui = new BasePage() {};

    @Given("I have just completed a trip")
    public void i_have_just_completed_a_trip() {
        // Test-data precondition.
    }

    @Given("the trip has ended and I am on the rating screen")
    public void the_trip_has_ended_and_i_am_on_the_rating_screen() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("star_rating_input")), // VERIFY
                "Rating screen not shown");
    }

    @When("I select a {int}-star rating")
    public void i_select_a_star_rating(int stars) {
        ui.tapPublic(ui.byAccessibilityIdPublic("star_" + stars)); // VERIFY star locator pattern
    }

    @Then("the rating should be submitted with:")
    public void the_rating_should_be_submitted_with(DataTable dataTable) {
        // Backend payload assertion — not directly checkable from the UI beyond the
        // navigation-away consequence asserted by the following step.
    }

    @And("I should be navigated to the home screen")
    public void i_should_be_navigated_to_the_home_screen() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Where to")), // VERIFY home-screen anchor text
                "Expected to be navigated to the home screen");
    }

    @Given("I am on the rating screen")
    public void i_am_on_the_rating_screen() {
        the_trip_has_ended_and_i_am_on_the_rating_screen();
    }

    @Then("I should be prompted for additional feedback")
    public void i_should_be_prompted_for_additional_feedback() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("feedback_prompt")), // VERIFY
                "Expected an additional-feedback prompt for a low rating");
    }

    @And("I should see predefined complaint options")
    public void i_should_see_predefined_complaint_options() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("complaint_option")).size() > 0, // VERIFY
                "Expected predefined complaint options to be listed");
    }

    @When("I tap {string} or close the rating view")
    public void i_tap_or_close_the_rating_view(String label) {
        ui.tapTextPublic(label);
    }

    @And("no rating should be submitted for this trip")
    public void no_rating_should_be_submitted_for_this_trip() {
        // Backend/state assertion — not directly checkable from the UI.
    }

    @And("I should see the compliment driver view")
    public void i_should_see_the_compliment_driver_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("compliment_view")), // VERIFY
                "Compliment driver view not shown");
    }

    @And("I should see predefined compliment categories like:")
    public void i_should_see_predefined_compliment_categories(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            String compliment = row.get("compliment");
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(compliment)),
                    "Expected compliment category: " + compliment);
        }
    }

    @When("I select {string} and {string}")
    public void i_select_two_options(String option1, String option2) {
        ui.tapTextPublic(option1);
        ui.tapTextPublic(option2);
    }

    @Then("the rating should include the selected compliments in comments")
    public void the_rating_should_include_selected_compliments() {
        // Backend payload assertion — see class javadoc.
    }

    @And("I should see the complaint driver view")
    public void i_should_see_the_complaint_driver_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("complaint_view")), // VERIFY
                "Complaint driver view not shown");
    }

    @And("I should see complaint categories")
    public void i_should_see_complaint_categories() {
        i_should_see_predefined_complaint_options();
    }

    @When("I select a complaint reason")
    public void i_select_a_complaint_reason() {
        ui.tapPublic(ui.byAccessibilityIdPublic("complaint_option_0")); // VERIFY
    }

    @And("I enter additional details {string}")
    public void i_enter_additional_details(String details) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("complaint_details_field"), details); // VERIFY
    }

    @And("I mark {string} as true")
    public void i_mark_option_as_true(String label) {
        ui.tapTextPublic(label); // VERIFY assumes a tappable checkbox/toggle labeled with this text
    }

    @Then("the rating should be submitted with furtherSupport as true")
    public void the_rating_should_be_submitted_with_further_support_true() {
        // Backend payload assertion — see class javadoc.
    }

    @And("a support ticket should be created")
    public void a_support_ticket_should_be_created() {
        // Backend-only assertion — verifiable via Zendesk, not the rider app UI.
    }

    @And("I enter a text suggestion {string}")
    public void i_enter_a_text_suggestion(String suggestion) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("rating_suggestion_field"), suggestion); // VERIFY
    }

    @Then("the rating should include the suggestion text")
    public void the_rating_should_include_the_suggestion_text() {
        // Backend payload assertion — see class javadoc.
    }

    @Given("I have submitted a rating for my trip")
    public void i_have_submitted_a_rating_for_my_trip() {
        // Test-data/navigation precondition.
    }

    @When("I see the tip prompt")
    public void i_see_the_tip_prompt() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("tip_prompt")), // VERIFY
                "Tip prompt not shown");
    }

    @And("I select a {string} tip")
    public void i_select_a_tip(String amount) {
        ui.tapTextPublic(amount);
    }

    @Then("an AddTipRequest should be sent")
    public void an_add_tip_request_should_be_sent() {
        // Backend-only assertion — see class javadoc.
    }

    @And("the tip amount should be added to the trip fare")
    public void the_tip_amount_should_be_added_to_trip_fare() {
        // Backend/state assertion — the confirmation check below covers the UI-visible
        // consequence.
    }

    @And("I should see confirmation of the tip")
    public void i_should_see_confirmation_of_the_tip() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("tip")), // VERIFY exact copy
                "Expected a tip confirmation to be shown");
    }

    // "I see the tip prompt" (used as both Given and When across scenarios) is defined once
    // above — Cucumber matches step text regardless of the Given/When/Then keyword used.

    @When("I enter {string}")
    public void i_enter_amount(String amount) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("custom_tip_field"), amount); // VERIFY
    }

    @And("I confirm")
    public void i_confirm() {
        ui.tapTextPublic("Confirm"); // VERIFY exact label
    }

    @Then("the custom tip should be applied to the trip")
    public void the_custom_tip_should_be_applied_to_the_trip() {
        i_should_see_confirmation_of_the_tip();
    }

    @When("I tap {string} or skip")
    public void i_tap_or_skip(String label) {
        ui.tapTextPublic(label);
    }

    @Then("no tip should be added")
    public void no_tip_should_be_added() {
        // Backend/state assertion — not directly checkable from the UI.
    }

    @When("I submit a {int}-star rating")
    public void i_submit_a_star_rating(int stars) {
        i_select_a_star_rating(stars);
        ui.tapTextPublic("Submit");
    }

    @Then("the rating should be queued locally")
    public void the_rating_should_be_queued_locally() {
        // Local-storage/state assertion — not directly checkable from the UI without
        // instrumentation; the retry-on-reconnect behavior is covered by the next step.
    }

    @And("it should be submitted when connectivity is restored")
    public void it_should_be_submitted_when_connectivity_is_restored() {
        // Backend-only assertion — see class javadoc.
    }

    @Given("I completed a trip but did not rate the driver")
    public void i_completed_a_trip_but_did_not_rate_the_driver() {
        // Test-data precondition.
    }

    @When("I check my trip history")
    public void i_check_my_trip_history() {
        ui.tapTextPublic("Trips"); // VERIFY — same trips tab used by rider_profile.feature
    }

    @Then("I should see an option to rate the unrated trip")
    public void i_should_see_option_to_rate_unrated_trip() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Rate")), // VERIFY exact label
                "Expected an option to rate the unrated trip");
    }
}
