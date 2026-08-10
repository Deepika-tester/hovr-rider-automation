package com.automation.stepdefinitions;

import com.automation.pages.*;
import com.automation.utils.ConfigReader;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.InteractsWithApps;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.testng.Assert;

/**
 * Step definitions for src/test/resources/features/rider_registration.feature.
 *
 * LOCATOR DISCLAIMER: this app is Flutter-based. Every By.accessibilityId(...) /
 * text lookup below is a best-effort guess at the widget's Semantics label based on
 * the feature wording. Before this suite is trustworthy, connect the real device,
 * open Appium Inspector (or `adb shell uiautomator dump` + view hierarchy), and
 * correct any locator that doesn't match — search "VERIFY" across src/test/java for
 * every spot flagged as a guess.
 *
 * Steps that assert on backend behavior directly (e.g. "an OTP should be requested
 * via the IAM service") cannot be verified from the UI layer alone; here they are
 * treated as no-ops/log statements with the UI-visible consequence (screen
 * transition) asserted instead. For true backend verification, pair this with the
 * api_auth.feature suite running against a REST/gRPC client, not Appium.
 */
public class RegistrationSteps {

    private final BasePage generic = new BasePage() {};

    // ---------- Background ----------

    @Given("the HOVR rider app is installed and launched")
    public void the_hovr_rider_app_is_installed_and_launched() {
        // App launch is handled by Appium capabilities in DriverManager (appPackage/appActivity).
    }

    @Given("the app has loaded the splash screen")
    public void the_app_has_loaded_the_splash_screen() {
        // Optional: wait for a known landing-page element to confirm splash has cleared.
    }

    // ---------- Landing / phone entry ----------

    @Given("I am on the landing page")
    public void i_am_on_the_landing_page() {
        // No-op: Background already puts us here after launch.
    }

    @When("I tap the {string} button")
    public void i_tap_the_named_button(String label) {
        generic.tapTextPublic(label);
    }

    @Then("I should see the phone number entry screen")
    public void i_should_see_the_phone_number_entry_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("phone_number_field")),
                "Phone number entry screen not shown");
    }

    @Given("I am on the phone number entry screen")
    public void i_am_on_the_phone_number_entry_screen() {
        i_should_see_the_phone_number_entry_screen();
    }

    @When("I select country code {string}")
    public void i_select_country_code(String code) {
        generic.tapPublic(generic.byAccessibilityIdPublic("country_code_selector"));
        generic.tapTextPublic(code);
    }

    @When("I enter phone number {string}")
    public void i_enter_phone_number(String phoneNumber) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("phone_number_field"), phoneNumber);
    }

    @When("I enter phone number {string} without selecting a country code")
    public void i_enter_phone_number_without_country_code(String phoneNumber) {
        i_enter_phone_number(phoneNumber);
    }

    @When("I enter a phone number registered to an unsupported region")
    public void i_enter_unsupported_region_number() {
        // VERIFY: substitute a real test phone number known to map to an unsupported region.
        i_enter_phone_number("+447911123456");
    }

    @When("I tap {string} or {string}")
    public void i_tap_either(String option1, String option2) {
        if (generic.isDisplayedPublic(generic.byTextPublic(option1))) {
            generic.tapTextPublic(option1);
        } else {
            generic.tapTextPublic(option2);
        }
    }

    @When("I tap {string} or navigate back")
    public void i_tap_or_navigate_back(String label) {
        if (generic.isDisplayedPublic(generic.byTextPublic(label))) {
            generic.tapTextPublic(label);
        } else {
            generic.driverPublic().navigate().back();
        }
    }

    @Then("an OTP should be requested via the IAM service")
    public void an_otp_should_be_requested_via_iam() {
        // Backend-only assertion — not verifiable from UI. See class javadoc.
    }

    @Then("I should be navigated to the OTP verification screen")
    public void i_should_be_navigated_to_otp_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("otp_input_field")),
                "OTP verification screen not shown");
    }

    @Given("I am on the OTP verification screen")
    public void i_am_on_the_otp_verification_screen() {
        i_should_be_navigated_to_otp_screen();
    }

    @Then("I should see a validation error for missing country code")
    public void i_should_see_validation_error_missing_country_code() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("country code")),
                "Expected missing-country-code validation error");
    }

    @Then("I should be navigated to the {string} screen")
    public void i_should_be_navigated_to_named_screen(String screenName) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(screenName)),
                "Expected screen: " + screenName);
    }

    @Then("I should see a message indicating HOVR is not yet available in my area")
    public void i_should_see_unsupported_region_message() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("not yet available")),
                "Expected unsupported-region messaging");
    }

    // ---------- OTP ----------

    @Given("an OTP has been sent to my phone {string}")
    public void an_otp_has_been_sent_to_my_phone(String phone) {
        // Precondition assumed true after a real RequestOTP call triggered by prior steps.
    }

    @Given("the OTP was sent more than 5 minutes ago")
    public void the_otp_was_sent_more_than_5_minutes_ago() {
        // Requires test-data/backend control to simulate; out of scope for pure UI automation.
    }

    @When("I enter the correct 6-digit OTP code")
    public void i_enter_the_correct_otp_code() {
        // VERIFY: replace with a real test OTP retrieval mechanism (test backdoor, SMS API, etc.)
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("otp_input_field"), "123456");
    }

    @When("I enter an incorrect OTP code {string}")
    public void i_enter_an_incorrect_otp_code(String code) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("otp_input_field"), code);
    }

    @When("I enter the expired OTP code")
    public void i_enter_the_expired_otp_code() {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("otp_input_field"), "123456");
    }

    @Then("the OTP should be verified successfully via the IAM service")
    public void the_otp_should_be_verified_successfully() {
        // Backend assertion — proxy via next-screen check instead.
    }

    @Then("I should be navigated to the profile setup flow")
    public void i_should_be_navigated_to_profile_setup_flow() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("first_name_field")),
                "Profile setup (name entry) screen not shown");
    }

    @Then("I should see an error message {string}")
    public void i_should_see_an_error_message(String message) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic(message)),
                "Expected error message: " + message);
    }

    @Then("I should remain on the OTP verification screen")
    public void i_should_remain_on_otp_screen() {
        i_should_be_navigated_to_otp_screen();
    }

    @Then("a new OTP should be sent to my phone number")
    public void a_new_otp_should_be_sent() {
        // Backend assertion.
    }

    @Then("I should see a confirmation that the code was resent")
    public void i_should_see_resend_confirmation() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("resent")),
                "Expected resend confirmation");
    }

    @Then("I should see an error message indicating the code has expired")
    public void i_should_see_expired_code_message() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("expired")),
                "Expected expired-code messaging");
    }

    @Then("I should be prompted to request a new code")
    public void i_should_be_prompted_for_new_code() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Resend Code")),
                "Expected prompt to request a new code");
    }

    // ---------- Name / Email / Referral / Terms / Payment ----------

    @Given("I have verified my phone number")
    public void i_have_verified_my_phone_number() {
        // Precondition assumed satisfied by prior OTP steps in the same scenario.
    }

    @Given("I am on the rider name entry screen")
    public void i_am_on_the_rider_name_entry_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("first_name_field")),
                "Name entry screen not shown");
    }

    @When("I enter first name {string}")
    public void i_enter_first_name(String firstName) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("first_name_field"), firstName);
    }

    @When("I enter last name {string}")
    public void i_enter_last_name(String lastName) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("last_name_field"), lastName);
    }

    @When("I leave the first name field empty")
    public void i_leave_first_name_empty() {
        generic.waitForPublic(generic.byAccessibilityIdPublic("first_name_field")).clear();
    }

    @Then("I should be navigated to the email entry screen")
    public void i_should_be_navigated_to_email_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("email_field")),
                "Email entry screen not shown");
    }

    @Then("I should see a validation error for the first name field")
    public void i_should_see_first_name_validation_error() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("first name")),
                "Expected first-name validation error");
    }

    @Given("I am on the rider email entry screen")
    public void i_am_on_the_rider_email_entry_screen() {
        i_should_be_navigated_to_email_screen();
    }

    @When("I enter email {string}")
    public void i_enter_email(String email) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("email_field"), email);
    }

    @Then("I should be navigated to the referral code screen")
    public void i_should_be_navigated_to_referral_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("referral_code_field")),
                "Referral code screen not shown");
    }

    // Generic enough to be reused verbatim by rider_booking.feature ("I am on the "Finding
    // your driver" screen" etc.) via Cucumber's global step registry.
    @Given("I am on the {string} screen")
    public void i_am_on_the_named_screen(String screenName) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(screenName))
                        || generic.isDisplayedPublic(generic.byAccessibilityIdPublic("referral_code_field")),
                "Expected to be on screen: " + screenName);
    }

    @When("I enter referral code {string}")
    public void i_enter_referral_code(String code) {
        generic.typeIntoPublic(generic.byAccessibilityIdPublic("referral_code_field"), code);
    }

    @Then("I should be navigated to the terms and policy screen")
    public void i_should_be_navigated_to_terms_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("I Accept")),
                "Terms and policy screen not shown");
    }

    @Then("the referral code should be validated")
    public void the_referral_code_should_be_validated() {
        // Backend assertion.
    }

    @Then("I should see an error {string}")
    public void i_should_see_an_error(String message) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic(message)),
                "Expected error: " + message);
    }

    @Given("I am on the terms and policy screen")
    public void i_am_on_the_terms_and_policy_screen() {
        i_should_be_navigated_to_terms_screen();
    }

    @When("I review the terms of service")
    public void i_review_the_terms_of_service() {
        // Scrolling/reading — no assertion needed unless a "scrolled to bottom" gate exists.
    }

    @Then("I should be navigated to the payment method screen")
    public void i_should_be_navigated_to_payment_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Skip for now")),
                "Payment method screen not shown");
    }

    @Then("I should not be able to proceed with registration")
    public void i_should_not_be_able_to_proceed() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("I Accept")),
                "Expected to remain on terms screen after declining");
    }

    @Given("I am on the payment method selector screen during registration")
    public void i_am_on_payment_method_selector_screen() {
        i_should_be_navigated_to_payment_screen();
    }

    @When("I enter valid card details")
    public void i_enter_valid_card_details() {
        // Adyen drop-in is a separate embedded flow/activity — VERIFY exact card-field locators
        // in com.adyen.checkout.dropin.internal.ui.DropInActivity if this scenario is automated fully.
    }

    @Then("the payment method should be saved via Adyen")
    public void the_payment_method_should_be_saved_via_adyen() {
        // Backend/Adyen assertion.
    }

    @Then("I should be navigated to the welcome screen")
    public void i_should_be_navigated_to_welcome_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("Welcome to HOVR")),
                "Welcome screen not shown");
    }

    @Then("my account should be created without a payment method")
    public void my_account_created_without_payment_method() {
        // Backend assertion.
    }

    // ---------- Completion / returning user / edge cases ----------

    @Given("I am on the welcome screen")
    public void i_am_on_the_welcome_screen() {
        i_should_be_navigated_to_welcome_screen();
    }

    @Then("I should see {string}")
    public void i_should_see(String text) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(text)),
                "Expected to see: " + text);
    }

    @And("my rider profile should be created with:")
    public void my_rider_profile_created_with(io.cucumber.datatable.DataTable dataTable) {
        // Backend assertion — cross-check via api_auth.feature / a GetRiderProfile API call
        // if you need to verify these values server-side rather than just UI.
    }

    @Then("I should be navigated to the ride home page")
    public void i_should_be_navigated_to_ride_home_page() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("ride_home_map")), // VERIFY
                "Ride home page not shown");
    }

    @Then("I should be navigated directly to the ride home page")
    public void i_should_be_navigated_directly_to_ride_home_page() {
        i_should_be_navigated_to_ride_home_page();
    }

    @Given("I have an existing rider account with phone {string}")
    public void i_have_an_existing_rider_account(String phone) {
        // Test-data precondition — assumes backend/test fixtures already created this account.
    }

    @When("I enter my phone number and verify with OTP")
    public void i_enter_phone_and_verify_with_otp() {
        i_enter_phone_number("4165551234");
        generic.tapTextPublic("Continue");
        i_enter_the_correct_otp_code();
    }

    @Then("I should be authenticated")
    public void i_should_be_authenticated() {
        i_should_be_navigated_to_ride_home_page();
    }

    @Then("I should not see the profile setup flow")
    public void i_should_not_see_profile_setup_flow() {
        Assert.assertFalse(generic.isDisplayedPublic(generic.byAccessibilityIdPublic("first_name_field")),
                "Should not see profile setup after returning-user login");
    }

    @Given("there is no network connectivity")
    public void there_is_no_network_connectivity() {
        // Real-device network toggling requires adb (e.g. `adb shell svc wifi disable` +
        // `adb shell svc data disable`), not exposed via Appium options here — add as a
        // bash/adb pre-step if this scenario needs to run for real.
    }

    @When("I enter a valid phone number and tap {string}")
    public void i_enter_valid_phone_number_and_tap(String buttonLabel) {
        i_enter_phone_number("4165551234");
        generic.tapTextPublic(buttonLabel);
    }

    @Then("I should see a network error message")
    public void i_should_see_network_error_message() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("network")),
                "Expected network error message");
    }

    @Then("I should be able to retry when connectivity is restored")
    public void i_should_be_able_to_retry() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Continue")),
                "Expected retry option after connectivity restored");
    }

    @Given("I have completed OTP verification")
    public void i_have_completed_otp_verification() {
        // Precondition assumed satisfied by prior steps in the same scenario.
    }

    @Given("I am on the name entry screen")
    public void i_am_on_the_name_entry_screen() {
        i_am_on_the_rider_name_entry_screen();
    }

    @When("the app is killed and relaunched")
    public void the_app_is_killed_and_relaunched() {
        // appPackage on Android, bundleId on iOS — both drivers implement InteractsWithApps.
        String appId = "iOS".equalsIgnoreCase(ConfigReader.get("platformName"))
                ? ConfigReader.get("bundleId")
                : ConfigReader.get("appPackage");
        InteractsWithApps appsDriver = (InteractsWithApps) generic.driverPublic();
        appsDriver.terminateApp(appId);
        appsDriver.activateApp(appId);
    }

    @Then("I should be able to resume registration from where I left off")
    public void i_should_resume_registration() {
        i_am_on_the_rider_name_entry_screen();
    }

    @Given("a rider account already exists for phone {string}")
    public void a_rider_account_already_exists(String phone) {
        // Test-data precondition.
    }

    @When("a new user enters phone {string} and verifies OTP")
    public void a_new_user_enters_phone_and_verifies_otp(String phone) {
        i_enter_phone_number(phone.replace("+1", ""));
        generic.tapTextPublic("Continue");
        i_enter_the_correct_otp_code();
    }

    @Then("they should be logged into the existing account")
    public void they_should_be_logged_into_existing_account() {
        i_should_be_navigated_to_ride_home_page();
    }

    @Then("they should not see the registration flow")
    public void they_should_not_see_registration_flow() {
        i_should_not_see_profile_setup_flow();
    }
}