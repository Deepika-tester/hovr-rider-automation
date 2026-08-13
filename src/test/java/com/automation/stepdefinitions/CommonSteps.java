package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Steps whose Gherkin wording is shared verbatim across rider_registration.feature
 * and rider_profile.feature. Kept in ONE place — Cucumber throws an ambiguous-match
 * error at runtime if two glue classes both define the same step pattern, so do not
 * re-declare "I tap {string}", "I select {string}", etc. in the other step classes.
 */
public class CommonSteps {

    private final BasePage ui = new BasePage() {};

    @When("I tap {string}")
    public void i_tap(String label) {
        ui.tapTextPublic(label);
    }

    @When("I tap on {string}")
    public void i_tap_on(String label) {
        ui.tapTextPublic(label);
    }

    @When("I select {string}")
    public void i_select(String option) {
        ui.tapTextPublic(option);
    }

    @When("I save it")
    public void i_save_it() {
        ui.tapTextPublic("Save");
    }

    @Then("I should see a success confirmation")
    public void i_should_see_success_confirmation() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("success")),
                "Expected success confirmation");
    }

    // Contains-match, not exact — error/validation copy is exactly the kind of text that gets
    // tweaked without anyone updating every test. CONFIRMED 2026-08-13: the invalid-phone-format
    // message is actually "Sorry, we couldn't use this number. Please ensure it's correct for
    // your region." — the feature file's Gherkin now quotes a matching substring, not the
    // originally-guessed "Invalid phone number".
    @Then("I should see a validation error {string}")
    public void i_should_see_validation_error(String message) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(message)),
                "Expected validation error: " + message);
    }

    // ---------------------------------------------------------------
    // Shared preconditions/outcomes reused verbatim across multiple
    // feature files (rider_booking, rider_trip, rider_payment,
    // rider_promo). Kept here rather than duplicated per domain class.
    // ---------------------------------------------------------------

    @Given("I have a valid payment method")
    public void i_have_a_valid_payment_method() {
        // Test-data precondition — assumes the test account already has a card on file.
    }

    @And("I should be returned to the home screen")
    public void i_should_be_returned_to_the_home_screen() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Where to")), // VERIFY home-screen anchor text
                "Expected to return to the ride home screen");
    }

    // "there is no network connectivity" / "I should see a network error message" are already
    // defined in RegistrationSteps — reused verbatim by rider_booking/rider_trip/rider_payment
    // via Cucumber's global step registry, not re-declared here.
}