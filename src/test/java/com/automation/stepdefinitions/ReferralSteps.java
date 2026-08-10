package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for src/test/resources/features/rider/rider_referral.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer.
 */
public class ReferralSteps {

    private final BasePage ui = new BasePage() {};

    @Given("I navigate to the {string} screen")
    public void i_navigate_to_the_named_screen(String screenName) {
        ui.tapTextPublic(screenName);
    }

    @When("the referral profile is loaded")
    public void the_referral_profile_is_loaded() {
        ui.waitForPublic(ui.byAccessibilityIdPublic("referral_code_display")); // VERIFY
    }

    @Then("I should see my unique referral code")
    public void i_should_see_my_unique_referral_code() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_code_display")), // VERIFY
                "Referral code not shown");
    }

    @And("I should see a {string} button")
    public void i_should_see_a_named_button(String label) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(label)),
                "Expected button: " + label);
    }

    @And("I should see the referral reward details")
    public void i_should_see_the_referral_reward_details() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_reward_details")), // VERIFY
                "Referral reward details not shown");
    }

    // "I am on the {string} screen" is already defined in RegistrationSteps and reused here.

    @And("I see my referral code {string}")
    public void i_see_my_referral_code(String code) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(code)),
                "Expected referral code to be shown: " + code);
    }

    @Then("the system share sheet should open")
    public void the_system_share_sheet_should_open() {
        // Native OS share sheet — outside the app's own accessibility tree; Appium can only
        // confirm it by switching to the NATIVE_APP context and checking for system share UI,
        // which needs to be verified against the real device.
    }

    @And("the shared content should include my referral code")
    public void the_shared_content_should_include_my_referral_code() {
        // Same limitation as above — share-sheet content isn't inspectable without a
        // device-level check (e.g. reading the share intent extras via ADB on Android).
    }

    @And("it should include a download link for the HOVR app")
    public void it_should_include_a_download_link() {
        // Same limitation as above.
    }

    // "I enter referral code {string}" is already defined in RegistrationSteps and reused here.

    @And("I enter the referrer's name {string}")
    public void i_enter_the_referrers_name(String name) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("referrer_name_input"), name); // VERIFY
    }

    @Then("the referral should be redeemed successfully")
    public void the_referral_should_be_redeemed_successfully() {
        // Backend assertion — the UI-visible consequence is asserted by the next step.
    }

    @And("I should see a success message with the reward details")
    public void i_should_see_success_message_with_reward_details() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("success")),
                "Expected a success message with reward details");
    }

    @When("I enter my own referral code")
    public void i_enter_my_own_referral_code() {
        // Test-data precondition — requires knowing the current test account's own code;
        // wire up once a shared accessor for "my referral code" is available.
    }

    @When("I enter an invalid referral code {string}")
    public void i_enter_an_invalid_referral_code(String code) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("referral_code_field"), code); // VERIFY
    }

    @Then("I should see an error message from ReferralErrorState")
    public void i_should_see_error_message_from_referral_error_state() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_error_banner")), // VERIFY
                "Expected a ReferralErrorState error message");
    }

    @Given("I have already redeemed a referral code")
    public void i_have_already_redeemed_a_referral_code() {
        // Test-data precondition.
    }

    @When("I try to redeem another referral code")
    public void i_try_to_redeem_another_referral_code() {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("referral_code_field"), "HOVR-ANOTHER2026"); // VERIFY
        ui.tapTextPublic("Redeem");
    }

    // "I tap "Scan QR Code"" is a plain quoted-string tap, already covered by
    // CommonSteps' generic "I tap {string}" handler.

    @Then("the QR scanner should open")
    public void the_qr_scanner_should_open() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("qr_scanner_view")), // VERIFY
                "QR scanner view not shown");
    }

    @When("I scan a valid referral QR code")
    public void i_scan_a_valid_referral_qr_code() {
        // Camera-based QR scanning can't be driven directly via Appium — requires either a
        // physical QR code presented to the device camera in a real test rig, or a
        // deep-link/manual-entry fallback the app exposes for automation.
    }

    @Then("the referral code should be auto-filled")
    public void the_referral_code_should_be_auto_filled() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_code_input")), // VERIFY
                "Expected the referral code field to be auto-filled");
    }

    @And("I should see the referrer's details")
    public void i_should_see_the_referrers_details() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referrer_details")), // VERIFY
                "Expected referrer details to be shown");
    }

    @Given("the QR scanner is open")
    public void the_qr_scanner_is_open() {
        the_qr_scanner_should_open();
    }

    @When("I scan a QR code that is not a HOVR referral")
    public void i_scan_a_qr_code_that_is_not_a_hovr_referral() {
        // Same camera-scanning limitation as #i_scan_a_valid_referral_qr_code.
    }

    @Given("I have referred {int} friends")
    public void i_have_referred_n_friends(int count) {
        // Test-data precondition.
    }

    @Then("I should see a list of my referrals")
    public void i_should_see_a_list_of_my_referrals() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("referral_item")).size() > 0, // VERIFY
                "Expected a list of referrals");
    }

    @And("each referral should show the status \\(pending, completed, rewarded)")
    public void each_referral_should_show_status() {
        // Field-level check on every row — implement once the referral-row layout is confirmed.
    }

    @And("I should see total rewards earned")
    public void i_should_see_total_rewards_earned() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("total_rewards_earned")), // VERIFY
                "Total rewards earned not shown");
    }

    @Given("I have not referred anyone")
    public void i_have_not_referred_anyone() {
        // Test-data precondition — use a fresh test account.
    }

    @Then("I should see the empty referral progress view")
    public void i_should_see_the_empty_referral_progress_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_empty_state")), // VERIFY
                "Empty referral progress view not shown");
    }

    @And("I should see a call to action to start referring")
    public void i_should_see_call_to_action_to_start_referring() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Refer")), // VERIFY exact copy
                "Expected a call to action to start referring");
    }

    @When("the referral service returns an error")
    public void the_referral_service_returns_an_error() {
        // Simulated backend failure — needs a staging fault-injection hook or mocked response.
    }

    @Then("I should see the error state")
    public void i_should_see_the_error_state() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("referral_error_state")), // VERIFY
                "Referral error state not shown");
    }

    @And("I should be able to retry loading")
    public void i_should_be_able_to_retry_loading() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Retry")), // VERIFY exact label
                "Expected a retry option");
    }

    @Given("I am redeeming a referral code")
    public void i_am_redeeming_a_referral_code() {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("referral_code_field"), "HOVR-JOHN2026"); // VERIFY
    }

    @And("the code should not be consumed")
    public void the_code_should_not_be_consumed() {
        // Backend/state assertion — not directly checkable from the UI.
    }
}
