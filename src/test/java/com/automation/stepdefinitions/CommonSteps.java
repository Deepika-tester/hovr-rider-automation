package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
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

    @Then("I should see a validation error {string}")
    public void i_should_see_validation_error(String message) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(message)),
                "Expected validation error: " + message);
    }
}