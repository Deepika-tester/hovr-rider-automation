package com.automation.driver.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Generic step wording shared across the driver feature files (mirrors
 * com.automation.stepdefinitions.CommonSteps for the rider app). Kept in a
 * SEPARATE package/glue path from the rider suite deliberately: several driver
 * feature files use the exact same Gherkin phrasing as the rider ones ("I tap
 * {string}", "I should see the phone number entry screen", ...) but need
 * different real locators/assertions underneath. If both suites' step
 * definitions lived in the same glue package, Cucumber would see two methods
 * bound to the identical step text and fail with "Ambiguous step definitions"
 * the moment either suite ran — so DriverTestRunner's glue never includes the
 * rider's com.automation.stepdefinitions package, and this class exists so
 * driver features still get the same generic conveniences.
 */
public class DriverCommonSteps {

    private final BasePage ui = new BasePage() {};

    @When("I tap {string}")
    public void i_tap(String label) {
        ui.tapTextPublic(label);
    }

    @When("I tap {string} or {string}")
    public void i_tap_either(String primary, String fallback) {
        if (ui.isDisplayedPublic(ui.byTextPublic(primary))) {
            ui.tapTextPublic(primary);
        } else {
            ui.tapTextPublic(fallback);
        }
    }

    @When("I select country code {string}")
    public void i_select_country_code(String code) {
        // CONFIRMED 2026-08-25 on a real device: the phone entry screen already defaults to
        // "+1" with no picker interaction needed for this test's Canadian numbers — same
        // resolution as the rider app's equivalent step.
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(code)),
                "Expected country code " + code + " to already be selected");
    }
}
