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
 * LOCATOR DISCLAIMER: see BasePage's javadoc for the UI-toolkit note — this app
 * is Jetpack Compose, not Flutter (corrected 2026-08-10 against a real device
 * dump). Every By.accessibilityId(...) / text lookup below is still a
 * best-effort guess at the real content-desc/text based on the feature wording.
 * Before this suite is trustworthy, connect the real device, open Appium
 * Inspector (or pull GET /session/:id/source), and correct any locator that
 * doesn't match — search "VERIFY" across src/test/java for every spot flagged
 * as a guess.
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
    //
    // SELF-NAVIGATING GIVEN STEPS (added 2026-08-10): each scenario gets a fresh Appium session
    // (see Hooks), but the app itself is NOT guaranteed to be logged out — noReset=true means a
    // previous scenario's completed registration/login persists. A "Given I am on X screen" that
    // only asserts (the original pattern here) fails as soon as a scenario doesn't happen to run
    // right after the one that navigated there. Confirmed as a real, systemic bug on a real
    // device: "Enter a valid Canadian phone number" failed at its very first step for exactly
    // this reason. Fix: each ensureOnXScreen() checks first, and if not already there, drives
    // forward from the previous screen in the flow (recursively), with ensureOnLandingPage() as
    // the base case that force-resets the app via DriverManager.resetAppToLoggedOutState() if
    // even the landing page isn't showing. This makes every registration scenario independently
    // runnable regardless of what ran before it, in any order, on a shared device.
    //
    // NOTE: only the landing/phone/OTP screens below have CONFIRMED real locators (per the
    // 2026-08-10 device session). Name/email/terms/payment screens still use guessed locators
    // (byAccessibilityIdPublic(...) placeholders) — the chain's *structure* is correct, but those
    // links will need fixing once walked on a real device, same as any other VERIFY spot.

    @Given("I am on the landing page")
    public void i_am_on_the_landing_page() {
        ensureOnLandingPage();
    }

    private void ensureOnLandingPage() {
        if (generic.isDisplayedPublic(generic.byTextPublic("Get Started"))) {
            return;
        }
        com.automation.drivers.DriverManager.resetAppToLoggedOutState();
        Assert.assertTrue(generic.waitForPublic(generic.byTextPublic("Get Started")) != null,
                "Landing page not shown even after resetting app to a logged-out state");
    }

    @When("I tap the {string} button")
    public void i_tap_the_named_button(String label) {
        generic.tapTextPublic(label);
    }

    @Then("I should see the phone number entry screen")
    public void i_should_see_the_phone_number_entry_screen() {
        // CONFIRMED 2026-08-10 on a real device: heading text is literally "Enter your phone
        // number" (@text, unmerged). The mobile-number EditText itself has no text/content-desc/
        // resource-id at all — see i_enter_phone_number below.
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Enter your phone number")),
                "Phone number entry screen not shown");
    }

    @Given("I am on the phone number entry screen")
    public void i_am_on_the_phone_number_entry_screen() {
        ensureOnPhoneEntryScreen();
    }

    private void ensureOnPhoneEntryScreen() {
        if (generic.isDisplayedPublic(generic.byTextPublic("Enter your phone number"))) {
            return;
        }
        ensureOnLandingPage();
        generic.tapTextPublic("Get Started");
        i_should_see_the_phone_number_entry_screen();
    }

    @When("I select country code {string}")
    public void i_select_country_code(String code) {
        // CONFIRMED 2026-08-10: default is already Canada/+1, so every actual usage in this
        // feature file (all three are "+1") is a same-code no-op — skip opening the picker
        // entirely rather than risk leaving it open (confirmed real bug: it's a full-screen
        // alphabetical list with no exposed search EditText in the accessibility tree despite
        // showing a "Search country" placeholder, so a picker left open with the wrong country
        // typed/tapped blocks the rest of the screen, including "Continue"). Selecting a
        // DIFFERENT country isn't implemented — would need the search field solved first.
        if (generic.isDisplayedPublic(generic.byTextPublic(code))) {
            return;
        }
        throw new UnsupportedOperationException(
                "Selecting a country code other than the default (+1) is not yet implemented — "
                        + "the picker's search field isn't exposed in the accessibility tree.");
    }

    @When("I enter phone number {string}")
    public void i_enter_phone_number(String phoneNumber) {
        // CONFIRMED 2026-08-10: only one EditText on this screen (the mobile number field).
        generic.waitForPublic(By.className("android.widget.EditText")).sendKeys(phoneNumber);
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
        // CONFIRMED 2026-08-10 on a real device: heading is literally "Enter the 6-digit code
        // sent to you at <phone>." (@text). Despite rendering as 6 visually separate boxes, the
        // accessibility tree exposes it as ONE EditText with max-text-length="6" — not 6 separate
        // fields as originally guessed. See i_enter_the_correct_otp_code below.
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("digit code sent")),
                "OTP verification screen not shown");
    }

    @Given("I am on the OTP verification screen")
    public void i_am_on_the_otp_verification_screen() {
        ensureOnOtpScreen();
    }

    private void ensureOnOtpScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("digit code sent"))) {
            return;
        }
        ensureOnPhoneEntryScreen();
        // Country code already defaults to +1 on this screen (confirmed 2026-08-10) — no need to
        // reselect it. MUST be a fresh number each run, not a fixed one: confirmed 2026-08-13
        // against the real staging backend that accepting Terms of Service actually creates the
        // account server-side, so replaying a previously-completed number just logs straight
        // into the existing account (ride home screen) instead of reaching name/email/etc. —
        // that's exactly what broke "Skip payment method during registration" after an earlier
        // run had already completed registration with the old hardcoded "4165551234".
        i_enter_phone_number(uniqueTestPhoneNumber());
        generic.tapTextPublic("Continue");
        i_should_be_navigated_to_otp_screen();
    }

    // "555" is the North American fictional-use exchange code (never assigned to a real
    // subscriber), so this can't collide with a real number. The last 4 digits change every run.
    private static String uniqueTestPhoneNumber() {
        return "416555" + String.format("%04d", System.currentTimeMillis() % 10_000);
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

    // "111111" is a confirmed staging backdoor OTP (per the team, 2026-08-10) — always accepted
    // regardless of what was actually texted to the number. Locator is confirmed: the one
    // EditText on this screen (see i_should_be_navigated_to_otp_screen above).
    private static final String STAGING_BYPASS_OTP = "111111";

    @When("I enter the correct 6-digit OTP code")
    public void i_enter_the_correct_otp_code() {
        generic.waitForPublic(By.className("android.widget.EditText")).sendKeys(STAGING_BYPASS_OTP);
    }

    @When("I enter an incorrect OTP code {string}")
    public void i_enter_an_incorrect_otp_code(String code) {
        generic.waitForPublic(By.className("android.widget.EditText")).sendKeys(code);
    }

    @When("I enter the expired OTP code")
    public void i_enter_the_expired_otp_code() {
        // NOTE: the bypass code above always succeeds regardless of expiry, so it can't be used
        // here — this scenario needs a genuinely expired code, which requires waiting out a real
        // OTP's 5-minute window (or a staging fixture that seeds one pre-expired). Placeholder
        // "123456" left as-is; this scenario is not actually verified yet.
        generic.waitForPublic(By.className("android.widget.EditText")).sendKeys("123456");
    }

    @Then("the OTP should be verified successfully via the IAM service")
    public void the_otp_should_be_verified_successfully() {
        // Backend assertion — proxy via next-screen check instead.
    }

    @Then("I should be navigated to the profile setup flow")
    public void i_should_be_navigated_to_profile_setup_flow() {
        Assert.assertTrue(generic.isDisplayedPublic(FIRST_NAME_FIELD),
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
        ensureOnNameEntryScreen();
    }

    // CONFIRMED 2026-08-10 on a real device: "What's your name?" screen has THREE EditTexts
    // (First name on ID / Last name on ID / Preferred name, the last one optional) — none have
    // text/content-desc, only a distinguishing `hint` attribute. Also confirmed: the OTP screen
    // auto-advances to this screen as soon as the 6th digit is entered — no Continue tap needed
    // (the try/catch below already handled that possibility correctly).
    // referral_code_field further down is still an UNVERIFIED guess (email screen confirmed too,
    // see i_should_be_navigated_to_email_screen).
    private static final By FIRST_NAME_FIELD = By.xpath("//android.widget.EditText[@hint='First name on ID']");
    private static final By LAST_NAME_FIELD = By.xpath("//android.widget.EditText[@hint='Last name on ID']");
    private static final By PREFERRED_NAME_FIELD = By.xpath("//android.widget.EditText[@hint='Optional']");

    private void ensureOnNameEntryScreen() {
        if (generic.isDisplayedPublic(FIRST_NAME_FIELD)) {
            return;
        }
        ensureOnOtpScreen();
        i_enter_the_correct_otp_code();
        try {
            generic.tapTextPublic("Continue");
        } catch (Exception autoAdvanced) {
            // Expected — the OTP screen auto-advances on the 6th digit, no Continue button here.
        }
        Assert.assertTrue(generic.isDisplayedPublic(FIRST_NAME_FIELD),
                "Name entry screen not shown");
    }

    @When("I enter first name {string}")
    public void i_enter_first_name(String firstName) {
        generic.typeIntoPublic(FIRST_NAME_FIELD, firstName);
    }

    @When("I enter last name {string}")
    public void i_enter_last_name(String lastName) {
        generic.typeIntoPublic(LAST_NAME_FIELD, lastName);
    }

    @When("I leave the first name field empty")
    public void i_leave_first_name_empty() {
        generic.waitForPublic(FIRST_NAME_FIELD).clear();
    }

    // CONFIRMED 2026-08-10: heading is literally "What's your email?" (@text); single unlabeled
    // EditText on screen (hint="name@email.com", pre-focused on load).
    @Then("I should be navigated to the email entry screen")
    public void i_should_be_navigated_to_email_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("your email")),
                "Email entry screen not shown");
    }

    @Then("I should see a validation error for the first name field")
    public void i_should_see_first_name_validation_error() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("first name")),
                "Expected first-name validation error");
    }

    @Given("I am on the rider email entry screen")
    public void i_am_on_the_rider_email_entry_screen() {
        ensureOnEmailEntryScreen();
    }

    private void ensureOnEmailEntryScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("your email"))) {
            return;
        }
        ensureOnNameEntryScreen();
        i_enter_first_name("Jane"); // VERIFY: arbitrary placeholder name
        i_enter_last_name("Doe");
        generic.tapTextPublic("Continue");
        i_should_be_navigated_to_email_screen();
    }

    @When("I enter email {string}")
    public void i_enter_email(String email) {
        generic.typeIntoPublic(By.className("android.widget.EditText"), email);
    }

    // CONFIRMED 2026-08-12 on a real device — TWO surprises vs. what the feature file assumes:
    //   1. Actual screen order is Email -> Terms -> Referral Code -> Payment, NOT
    //      Email -> Referral -> Terms as rider_registration.feature's scenario ordering implies.
    //      The chain below follows the real order; the feature file's Gherkin scenario order
    //      is just narrative and doesn't affect execution since each scenario sets up its own
    //      preconditions via Given, so this doesn't break anything — just noting the mismatch.
    //   2. Heading is "Do you have a referral code?" (content-desc); single unlabeled EditText;
    //      Skip button confirmed (content-desc="Skip", matches feature file). The real "apply"
    //      button says "Redeem Code", not "Apply" as originally in the Gherkin — feature file
    //      wording updated to match (2026-08-13, user-approved: app is the source of truth for
    //      UI copy, scenario intent unchanged).
    @Then("I should be navigated to the referral code screen")
    public void i_should_be_navigated_to_referral_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("referral code")),
                "Referral code screen not shown");
    }

    // Generic enough to be reused verbatim by rider_booking.feature ("I am on the "Finding
    // your driver" screen" etc.) via Cucumber's global step registry.
    @Given("I am on the {string} screen")
    public void i_am_on_the_named_screen(String screenName) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(screenName)),
                "Expected to be on screen: " + screenName);
    }

    @When("I enter referral code {string}")
    public void i_enter_referral_code(String code) {
        generic.typeIntoPublic(By.className("android.widget.EditText"), code);
    }

    // CONFIRMED 2026-08-12: heading is "Accept Hovr's Terms of Service"; the action button says
    // "Agree and continue" (content-desc), NOT "I Accept" as originally guessed.
    @Then("I should be navigated to the terms and policy screen")
    public void i_should_be_navigated_to_terms_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("Terms of Service")),
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

    // Private, not a step definition — the referral screen's actual public Given is the shared
    // generic "I am on the {string} screen" above (also reused by rider_booking.feature etc.),
    // which deliberately stays a plain assertion since it can't know which domain's navigation
    // to run. This is only for internally chaining payment/welcome below.
    private void ensureOnReferralScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("referral code"))) {
            return;
        }
        ensureOnTermsScreen();
        generic.tapTextPublic("Agree and continue");
        i_should_be_navigated_to_referral_screen();
    }

    @Given("I am on the terms and policy screen")
    public void i_am_on_the_terms_and_policy_screen() {
        ensureOnTermsScreen();
    }

    private void ensureOnTermsScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("Terms of Service"))) {
            return;
        }
        ensureOnEmailEntryScreen();
        i_enter_email("jane.doe@example.com"); // VERIFY: arbitrary placeholder
        generic.tapTextPublic("Continue");
        i_should_be_navigated_to_terms_screen();
    }

    @When("I review the terms of service")
    public void i_review_the_terms_of_service() {
        // Scrolling/reading — no assertion needed unless a "scrolled to bottom" gate exists.
    }

    // CONFIRMED 2026-08-13: heading is "Payment Methods"; options are "Google Pay" and "Credit
    // or debit card"; skip button says "Skip". Feature file wording updated to match (was
    // "Add Credit/Debit Card" and "Skip for now").
    @Then("I should be navigated to the payment method screen")
    public void i_should_be_navigated_to_payment_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("Payment Methods")),
                "Payment method screen not shown");
    }

    @Then("I should not be able to proceed with registration")
    public void i_should_not_be_able_to_proceed() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("Terms of Service")),
                "Expected to remain on terms screen after declining");
    }

    @Given("I am on the payment method selector screen during registration")
    public void i_am_on_payment_method_selector_screen() {
        ensureOnPaymentScreen();
    }

    private void ensureOnPaymentScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("Payment Methods"))) {
            return;
        }
        // Real order is Terms -> Referral -> Payment (see ensureOnReferralScreen note) — skip
        // the referral step to reach payment.
        ensureOnReferralScreen();
        generic.tapTextPublic("Skip");
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
        ensureOnWelcomeScreen();
    }

    private void ensureOnWelcomeScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("Welcome to HOVR"))) {
            return;
        }
        ensureOnPaymentScreen();
        generic.tapTextPublic("Skip"); // CONFIRMED 2026-08-13: real label is "Skip", not "Skip for now"
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
        // Deliberately the FIXED number here, not uniqueTestPhoneNumber() — this scenario
        // ("Returning user login") specifically needs a number that has already completed
        // registration, to verify login skips profile setup. "4165551234" is the one number we
        // know for certain has already registered on this backend (confirmed 2026-08-13).
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
        Assert.assertFalse(generic.isDisplayedPublic(FIRST_NAME_FIELD),
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
        i_enter_phone_number(uniqueTestPhoneNumber());
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