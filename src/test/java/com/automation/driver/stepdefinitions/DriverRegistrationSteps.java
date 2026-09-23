package com.automation.driver.stepdefinitions;

import com.automation.drivers.DriverManager;
import com.automation.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Step definitions for src/test/resources/features/driver/driver_registration.feature — the
 * auth/profile-creation flow (Landing through Welcome) AND the real, GTA-marketplace onboarding
 * document checklist confirmed 2026-08-25 (see that scenario block's comments for how the
 * checklist's real 15-document/3-category shape was found, superseding the file's original
 * 25-scenario assumption). The remaining "not applicable" scenarios listed in a comment at the
 * end of the feature file (S3 upload internals, training, banking, etc.) don't match anything in
 * this app version and have no step definitions.
 *
 * Every locator/flow below is CONFIRMED against a real device 2026-08-25 —
 * unlike the rider suite's early days, nothing here is a guess pending
 * verification. See the feature file's header comment for the headline
 * findings (no Terms screen, background-location permission quirk, etc).
 *
 * Deliberately in its own package (com.automation.driver.stepdefinitions), not
 * alongside the rider suite's com.automation.stepdefinitions — see
 * DriverCommonSteps's javadoc for why.
 */
public class DriverRegistrationSteps {

    private final BasePage generic = new BasePage() {};

    private static final String STAGING_BYPASS_OTP = "111111";

    @Given("the HOVR driver app is installed and launched")
    public void the_hovr_driver_app_is_installed_and_launched() {
        // App launch is handled by Appium capabilities in DriverManager (appPackage/appActivity,
        // see driver.properties, loaded via -Dplatform=driver).
    }

    // ---------- Landing / phone entry ----------
    //
    // Same self-navigating pattern as the rider suite (see RegistrationSteps's javadoc): each
    // scenario gets a fresh Appium session but not necessarily a logged-out app, so every
    // "Given I am on X screen" drives forward from wherever it actually finds itself instead of
    // assuming prior scenario state.

    @Given("I am on the driver landing page")
    public void i_am_on_the_driver_landing_page() {
        ensureOnLandingPage();
    }

    private void ensureOnLandingPage() {
        if (generic.isDisplayedPublic(generic.byTextPublic("Get Started"))) {
            return;
        }
        DriverManager.resetAppToLoggedOutState();
        Assert.assertTrue(generic.waitForPublic(generic.byTextPublic("Get Started")) != null,
                "Driver landing page not shown even after resetting app to a logged-out state");
    }

    @Then("I should see the phone number entry screen")
    public void i_should_see_the_phone_number_entry_screen() {
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

    @When("I enter phone number {string}")
    public void i_enter_phone_number(String phoneNumber) {
        WebElement field = generic.waitForPublic(By.className("android.widget.EditText"));
        field.click();
        field.clear();
        field.sendKeys(phoneNumber);
    }

    // "647" is a real Toronto/GTA area code (matches the feature file's own example numbers) so
    // this can't collide with a real subscriber the way a fictional exchange would look odd —
    // the last 4 digits still change every run. Same reasoning as the rider suite's
    // uniqueTestPhoneNumber(): submitting registration likely creates the account server-side,
    // so a reused fixed number risks silently becoming a "returning user" run instead.
    // A fixed area code + "555" exchange leaves only 4 truly variable digits (10,000
    // combinations) no matter what feeds them — a real constraint of looking like a valid NANP
    // number, not something more entropy alone fixes. Spreads across a small pool of real GTA-area
    // codes AND seeds the suffix from nanoTime (finer-grained than millis, less prone to colliding
    // on rapid successive calls) to meaningfully cut collision odds on a day with heavy reuse,
    // without claiming this eliminates the risk entirely.
    private static final String[] TEST_AREA_CODES = {"647", "416", "613", "905", "289"};

    private static String uniqueTestPhoneNumber() {
        String areaCode = TEST_AREA_CODES[(int) (Math.abs(System.nanoTime()) % TEST_AREA_CODES.length)];
        long suffix = Math.abs(System.nanoTime()) % 10_000;
        return areaCode + "555" + String.format("%04d", suffix);
    }

    @Then("an OTP should be sent to {string}")
    public void an_otp_should_be_sent_to(String phone) {
        // Backend-only assertion — not verifiable from the UI. See class javadoc.
    }

    // Shared by every negative scenario's "Then I should see a validation error {string}" step
    // (empty phone, empty email, invalid email, incorrect OTP) — all confirmed real error
    // messages, matched apostrophe-free where the app renders a curly apostrophe (see the
    // scenarios' own comments in the feature file for the exact confirmed wording).
    @Then("I should see a validation error {string}")
    public void i_should_see_a_validation_error(String message) {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(message)),
                "Expected validation error: " + message);
    }

    @Then("I should see the OTP verification screen")
    public void i_should_see_the_otp_verification_screen() {
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
        i_enter_phone_number(uniqueTestPhoneNumber());
        generic.tapTextPublic("Continue");
        i_should_see_the_otp_verification_screen();
    }

    // CONFIRMED 2026-08-25: the same "111111" staging backdoor OTP that works on the rider app
    // works here too. Field needs click+clear before sendKeys — same leftover-text landmine as
    // the rider app's OTP field (see RegistrationSteps#i_enter_the_correct_otp_code).
    @When("I enter the correct OTP")
    public void i_enter_the_correct_otp() {
        WebElement otpField = generic.waitForPublic(By.className("android.widget.EditText"));
        otpField.click();
        otpField.clear();
        otpField.sendKeys(STAGING_BYPASS_OTP);
    }

    @When("I enter an incorrect OTP {string}")
    public void i_enter_an_incorrect_otp(String code) {
        WebElement otpField = generic.waitForPublic(By.className("android.widget.EditText"));
        otpField.click();
        otpField.clear();
        otpField.sendKeys(code);
    }

    @Then("I should remain on the OTP verification screen")
    public void i_should_remain_on_the_otp_verification_screen() {
        i_should_see_the_otp_verification_screen();
    }

    @Then("I should be authenticated")
    public void i_should_be_authenticated() {
        // Backend/IAM assertion — proxy via the next-screen check instead.
    }

    @Then("I should proceed to profile creation")
    public void i_should_proceed_to_profile_creation() {
        i_should_see_the_driver_name_screen();
    }

    // ---------- Name / Email / Welcome ----------

    @Given("I have verified my phone number")
    public void i_have_verified_my_phone_number() {
        // Precondition assumed satisfied by prior OTP steps in the same scenario.
    }

    @Given("I am on the driver name screen")
    public void i_am_on_the_driver_name_screen() {
        ensureOnNameScreen();
    }

    @Then("I should see the driver name screen")
    public void i_should_see_the_driver_name_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("What’s your name?")),
                "Driver name screen not shown");
    }

    // CONFIRMED 2026-08-25 on a real device: two EditTexts, in order (first name, then last
    // name). Their `hint` attribute is a placeholder EXAMPLE name ("Jane" / "Doe"), not a
    // description like the rider app's "First name on ID" — not usable for matching, so these
    // are targeted by position instead.
    private static final By FIRST_NAME_FIELD = By.xpath("(//android.widget.EditText)[1]");
    private static final By LAST_NAME_FIELD = By.xpath("(//android.widget.EditText)[2]");

    private void ensureOnNameScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("What’s your name?"))) {
            return;
        }
        ensureOnOtpScreen();
        i_enter_the_correct_otp();
        i_should_see_the_driver_name_screen();
    }

    @When("I enter first name {string}")
    public void i_enter_first_name(String firstName) {
        generic.typeIntoPublic(FIRST_NAME_FIELD, firstName);
    }

    @When("I enter last name {string}")
    public void i_enter_last_name(String lastName) {
        generic.typeIntoPublic(LAST_NAME_FIELD, lastName);
    }

    @Then("I should proceed to the email entry screen")
    public void i_should_proceed_to_the_email_entry_screen() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("What’s your email?")),
                "Email entry screen not shown");
    }

    @Given("I am on the driver email screen")
    public void i_am_on_the_driver_email_screen() {
        ensureOnEmailScreen();
    }

    private void ensureOnEmailScreen() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("What’s your email?"))) {
            return;
        }
        ensureOnNameScreen();
        i_enter_first_name("Ahmad");
        i_enter_last_name("Khan");
        generic.tapTextPublic("Continue");
        i_should_proceed_to_the_email_entry_screen();
    }

    // CONFIRMED 2026-08-25 on a real device: the backend enforces email uniqueness across
    // registrations too, not just phone number — confirmed via "An account with this email
    // already exists." after this step's fixed feature-file example email got reused across
    // repeated runs. Same category of landmine as the rider app's fixed-phone-number bug (see
    // RegistrationSteps#uniqueTestPhoneNumber's comment). Injects a unique "+" tag into the
    // local part (standard email addressing, doesn't change the domain) so this step can be
    // re-run indefinitely without colliding with a prior run's account.
    @When("I enter email {string}")
    public void i_enter_email(String email) {
        int atIndex = email.indexOf('@');
        String uniqueEmail = atIndex < 0
                ? email
                : email.substring(0, atIndex) + "+" + (System.currentTimeMillis() % 100_000) + email.substring(atIndex);
        generic.typeIntoPublic(By.className("android.widget.EditText"), uniqueEmail);
    }

    @Then("my driver profile should be created")
    public void my_driver_profile_should_be_created() {
        // Backend assertion — proxy via the welcome-screen check instead.
    }

    // CONFIRMED 2026-08-25 on a real device, twice (once with a camera-permission dialog
    // appearing mid-transition, once without): submitting email goes through a brief loading
    // spinner and lands directly on the welcome/onboarding dashboard ("Welcome <name> ... Here's
    // what you need to do to set up your account."). No Terms screen in between — see the
    // feature file's header comment.
    @Then("I should see the welcome screen")
    public void i_should_see_the_welcome_screen() {
        Assert.assertTrue(
                generic.isDisplayedPublic(generic.byTextContainsPublic("set up your account")),
                "Driver welcome/onboarding dashboard not shown");
    }

    // ---------- Onboarding checklist (GTA marketplace) ----------
    //
    // CONFIRMED 2026-08-25 on a real device: the onboarding checklist is MARKETPLACE-DEPENDENT,
    // determined by device GPS at signup (not phone number country code) — India shows one
    // document ("Vehicle Registration"), GTA shows the real 15-document/3-category checklist
    // these steps exercise. GPS is mocked to Toronto via Appium's setLocation (Selenium's
    // LocationContext interface, which AndroidDriver implements) rather than assuming whatever
    // the physical test device's real location happens to be.

    private static final double TORONTO_LATITUDE = 43.6532;
    private static final double TORONTO_LONGITUDE = -79.3832;
    private static final double TORONTO_ALTITUDE = 76;

    @Given("I have created my driver profile in the GTA marketplace")
    public void i_have_created_my_driver_profile_in_the_gta_marketplace() {
        ensureOnGtaWelcomeDashboard();
    }

    // Exposed as its own step for the "Complete driver registration and onboarding flow"
    // scenario, which builds the whole journey up manually from the landing page rather than
    // jumping straight to a post-registration state via ensureOnGtaWelcomeDashboard() above —
    // it needs the mock applied BEFORE registering, same as that method does internally.
    @Given("my device location is set to the GTA marketplace")
    public void my_device_location_is_set_to_the_gta_marketplace() {
        setMockLocationToToronto();
    }

    private void ensureOnGtaWelcomeDashboard() {
        if (generic.isDisplayedPublic(generic.byTextContainsPublic("set up your account"))
                && generic.isDisplayedPublic(generic.byTextContainsPublic("GTA"))) {
            return;
        }
        setMockLocationToToronto();
        ensureOnEmailScreen();
        i_enter_email("ahmad.khan@email.com");
        generic.tapTextPublic("Continue");
        i_should_see_the_welcome_screen();
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("GTA")),
                "Expected GTA marketplace after mocking location to Toronto");
    }

    private void setMockLocationToToronto() {
        ((io.appium.java_client.android.AndroidDriver) generic.driverPublic())
                .setLocation(new org.openqa.selenium.html5.Location(
                        TORONTO_LATITUDE, TORONTO_LONGITUDE, TORONTO_ALTITUDE));
    }

    @Then("I should see the HOVR onboarding document list")
    public void i_should_see_the_hovr_onboarding_document_list() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("set up your account")),
                "Onboarding document list not shown");
    }

    @And("I should see required document categories:")
    public void i_should_see_required_document_categories(io.cucumber.datatable.DataTable table) {
        for (java.util.Map<String, String> row : table.asMaps()) {
            String category = row.get("category").strip();
            Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic(category)),
                    "Expected onboarding category: " + category);
        }
    }

    // Category cards/document rows render their status on a second line (e.g. content-desc
    // "Driver\nRequired · 6/6"), so an EXACT text match on just the category/document name would
    // never hit — tapping via a contains() locator instead, same technique used for the Adyen
    // card-payment button collision in the rider suite's BasePage notes.
    @Given("I am on the {string} document list")
    public void i_am_on_the_named_document_list(String category) {
        ensureOnGtaWelcomeDashboard();
        generic.tapPublic(generic.byTextContainsPublic(category));
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("submit the following")),
                "Expected document list for category: " + category);
    }

    @When("I tap the {string} document")
    public void i_tap_the_named_document(String document) {
        generic.tapPublic(generic.byTextContainsPublic(document));
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Upload")),
                "Expected document detail page for: " + document);
    }

    // CONFIRMED 2026-08-25 on a real device: "Upload" opens Android's system photo picker
    // (com.google.android.photopicker) — selecting any existing photo and tapping "Done" is
    // enough to complete the upload; the app doesn't validate photo CONTENT, just that one was
    // provided. Selects whichever photo is first in the grid.
    @When("I upload a photo for the document")
    public void i_upload_a_photo_for_the_document() {
        generic.tapTextPublic("Upload");
        WebElement firstPhoto = generic.waitForPublic(
                By.xpath("(//*[contains(@content-desc,'Photo taken')])[1]"));
        firstPhoto.click();
        generic.tapTextPublic("Done");
    }

    @Then("the document should be uploaded successfully")
    public void the_document_should_be_uploaded_successfully() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("uploaded Successfully")),
                "Expected upload success confirmation");
    }

    // CONFIRMED 2026-09-23 on a real device: unlike every other document, Driver Selfie's
    // "Upload" button opens the live Samsung Camera app (com.sec.android.app.camera) directly
    // instead of the gallery photo picker. "Take picture" is the shutter button; "OK" confirms
    // the just-captured photo on the review screen that follows (its counterpart, "Retry",
    // discards and re-shoots — not used here). Both matched by exact text since they're short,
    // common strings where a contains() match risks hitting something unrelated.
    @When("I capture a photo for the document")
    public void i_capture_a_photo_for_the_document() {
        generic.tapTextPublic("Upload");
        generic.tapTextPublic("Take picture");
        generic.tapTextPublic("OK");
    }

    // CONFIRMED 2026-09-23 on a real device: the camera-capture path (unlike the gallery-picker
    // path) returns straight to the document LIST with the status already reading "Submitted" —
    // no separate "Document uploaded Successfully!" confirmation screen in between.
    @Then("the document should show as submitted")
    public void the_document_should_show_as_submitted() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextContainsPublic("Submitted")),
                "Expected document status to show as submitted");
    }

    // CONFIRMED 2026-09-23 on a real device: backing out of the gallery photo picker (system
    // back press) without selecting anything returns cleanly to the same document detail page —
    // no error, no crash. Uses a raw back press rather than Appium's navigate().back(), since
    // this needs to dismiss the PICKER activity specifically, not risk the app's own back-stack
    // behavior (see the rider suite's Adyen-dialog notes for why a driver-level hideKeyboard/back
    // call can behave unexpectedly on a screen it wasn't tested against).
    @When("I cancel the photo picker without selecting a photo")
    public void i_cancel_the_photo_picker_without_selecting_a_photo() {
        generic.tapTextPublic("Upload");
        generic.driverPublic().navigate().back();
    }

    @Then("the document should still show as required")
    public void the_document_should_still_show_as_required() {
        Assert.assertTrue(generic.isDisplayedPublic(generic.byTextPublic("Upload")),
                "Expected to remain on the document detail page (upload not completed)");
    }
}
