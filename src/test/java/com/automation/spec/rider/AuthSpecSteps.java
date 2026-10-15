package com.automation.spec.rider;

import com.automation.drivers.DriverManager;
import com.automation.stepdefinitions.RegistrationSteps;
import io.appium.java_client.InteractsWithApps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Steps for features/rider/01_authentication.feature (landing, phone, OTP, name, email, terms, referral, region and
 * notification-permission screens). Sign-up navigation reuses the self-navigating helpers in RegistrationSteps.
 *
 * The spec's phone examples are Canadian; the dev backend only accepts the 111111 staging OTP for +91, so steps that
 * need a code that really verifies register a fresh +91 number and tell SpecContext about it (see SpecContext).
 */
public class AuthSpecSteps {

    private static final String APP_PACKAGE = "production.hovr.customer";
    private static final String BYPASS_OTP = "111111";

    private final SpecUi ui = new SpecUi();
    private final RegistrationSteps registration = new RegistrationSteps();

    private WebElement field() {
        return ui.waitForPublic(By.className("android.widget.EditText"));
    }

    private void typeCode(String code) {
        ui.popup();
        WebElement f = field();
        f.clear();
        f.sendKeys(code);
        ui.sleep(1500);
    }

    // ---- Background ----

    @Given("the app is freshly installed")
    public void the_app_is_freshly_installed() {
        // The wipe itself happens in SpecHooks before the Appium session starts; just make sure it took.
        if (!ui.seen("Get Started")) {
            DriverManager.resetAppToLoggedOutState();
        }
    }

    @Given("I launch the HOVR Rider app")
    public void i_launch_the_hovr_rider_app() {
        ((InteractsWithApps) ui.driverPublic()).activateApp(APP_PACKAGE);
    }

    // ---- landing ----

    @When("the splash screen finishes loading")
    public void the_splash_screen_finishes_loading() {
        Assert.assertTrue(ui.waitSeen("Get Started", 40), "Splash never gave way to the landing screen");
    }

    @Then("I should see the landing screen")
    public void i_should_see_the_landing_screen() {
        Assert.assertTrue(ui.waitSeen("Get Started", 15), "Landing screen (Get Started) not shown");
    }

    @Then("I should see the {string} button")
    public void i_should_see_the_button(String label) {
        Assert.assertTrue(ui.waitSeen(label, 15), "Expected the " + label + " button");
    }

    @Given("I am on the landing screen")
    public void i_am_on_the_landing_screen() {
        registration.i_am_on_the_landing_page();
    }

    // ---- phone number ----

    @Given("I am on the phone number screen")
    public void i_am_on_the_phone_number_screen() {
        registration.i_am_on_the_phone_number_entry_screen();
    }

    @When("I enter phone number {string}")
    public void i_enter_phone_number(String phone) {
        ui.popup();
        WebElement f = field();
        f.click();
        f.clear();
        if (!phone.isEmpty()) {
            f.sendKeys(phone);
        }
        ui.sleep(800);
    }

    @Given("I submitted a valid phone number {string}")
    public void i_submitted_a_valid_phone_number(String specPhone) {
        registration.i_am_on_the_phone_number_entry_screen();
        registration.selectIndiaCountryCode();
        String actual = RegistrationSteps.uniqueTestPhoneNumber();
        SpecContext.phoneUsed(specPhone, actual);
        i_enter_phone_number(actual);
        ui.tapLabel("Continue");
        Assert.assertTrue(ui.waitSeen("digit code sent", 25), "OTP screen not shown after submitting the phone number");
    }

    // ---- OTP ----

    @Given("I am on the OTP verification page")
    public void i_am_on_the_otp_verification_page() {
        if (!ui.seen("digit code sent")) {
            registration.i_am_on_the_otp_verification_screen();
        }
    }

    @When("I enter the valid 6-digit code")
    public void i_enter_the_valid_6_digit_code() {
        typeCode(BYPASS_OTP);
    }

    @When("I enter the code {string}")
    public void i_enter_the_code(String code) {
        typeCode(code);
    }

    @Then("I should be signed in")
    public void i_should_be_signed_in() {
        // A brand-new rider is signed in but still has to finish the profile; a returning one lands on ride home.
        long end = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < end) {
            if (ui.seen("your name?") || ui.seen("Where to?") || SpecSession.tabsVisible(ui)) {
                return;
            }
            ui.sleep(500);
        }
        Assert.fail("Still not signed in 30s after the valid code; screen shows OTP: " + ui.seen("digit code sent"));
    }

    @Then("the {string} button shows a countdown timer")
    public void the_button_shows_a_countdown_timer(String label) {
        Assert.assertTrue(ui.waitSeen(label + " (", 10), "Expected a countdown on the " + label + " button");
    }

    @When("the countdown reaches zero")
    public void the_countdown_reaches_zero() {
        long end = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < end) {
            if (!ui.seen("Resend code (")) {
                return;
            }
            ui.sleep(1000);
        }
        Assert.fail("The Resend code countdown never reached zero");
    }

    @Then("a new code is sent")
    public void a_new_code_is_sent() {
        // Observable proof: the countdown starts again and no error is shown.
        Assert.assertTrue(ui.waitSeen("Resend code (", 10), "Resending did not restart the countdown");
        Assert.assertFalse(ui.seen("Something went wrong"), "Resending showed an error");
    }

    @Then("the countdown restarts")
    public void the_countdown_restarts() {
        Assert.assertTrue(ui.waitSeen("Resend code (", 10), "Countdown did not restart");
    }

    @Then("I should return to the phone number screen")
    public void i_should_return_to_the_phone_number_screen() {
        Assert.assertTrue(ui.waitSeen("Enter your phone number", 10), "Phone number screen not shown");
    }

    // ---- profile ----

    @Given("I verified OTP as a new rider")
    public void i_verified_otp_as_a_new_rider() {
        registration.i_am_on_the_rider_name_entry_screen();
    }

    @Given("I am on the name entry screen")
    public void i_am_on_the_name_entry_screen() {
        registration.i_am_on_the_rider_name_entry_screen();
    }

    @When("I enter first name {string} and last name {string}")
    public void i_enter_first_name_and_last_name(String first, String last) {
        ui.typeIntoPublic(By.xpath("(//android.widget.EditText)[1]"), first);
        ui.typeIntoPublic(By.xpath("(//android.widget.EditText)[2]"), last);
    }

    @When("I enter email {string}")
    public void i_enter_email(String email) {
        registration.i_enter_email(email);
    }

    @Given("I am on the email entry screen")
    public void i_am_on_the_email_entry_screen() {
        registration.i_am_on_the_rider_email_entry_screen();
    }

    @When("I tap {string} without entering an email")
    public void i_tap_without_entering_an_email(String label) {
        ui.tapLabel(label);
        ui.sleep(1200);
    }

    @Then("I should see the Terms of Service screen")
    public void i_should_see_the_terms_of_service_screen() {
        Assert.assertTrue(ui.waitSeen("Terms of Service", 20), "Terms of Service screen not shown");
    }

    @Given("I am on the Terms of Service screen")
    public void i_am_on_the_terms_of_service_screen() {
        registration.i_am_on_the_terms_and_policy_screen();
    }

    @When("I tap {string} without checking the agreement box")
    public void i_tap_without_checking_the_agreement_box(String label) {
        ui.tapLabel(label);
        ui.sleep(1200);
    }

    @When("I check the agreement box")
    public void i_check_the_agreement_box() {
        By box = By.xpath("//android.widget.CheckBox | //*[@checkable='true']");
        WebElement el = ui.visible(box, 8);
        if (el == null) {
            el = ui.visible(SpecUi.contains("I agree"), 5);
        }
        Assert.assertNotNull(el, "Could not find the agreement checkbox");
        el.click();
        ui.sleep(800);
    }

    // ---- referral / welcome ----

    @Given("I am on the referral code onboarding screen")
    public void i_am_on_the_referral_code_onboarding_screen() {
        registration.i_am_on_the_named_screen("Do you have a referral code?");
    }

    @When("I enter referral code {string}")
    public void i_enter_referral_code(String code) {
        ui.typeIntoPublic(By.className("android.widget.EditText"), code);
    }

    // ---- region / permission ----

    @Given("my current location is outside the HOVR service area")
    public void my_current_location_is_outside_the_hovr_service_area() {
        SpecAdb.geoFix(0.0, 0.0); // mid-Atlantic: certainly not a HOVR marketplace
        // The app decides the supported area when it starts, so restart it for the new location to count.
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp(APP_PACKAGE);
        apps.activateApp(APP_PACKAGE);
        ui.sleep(4000);
    }

    @When("I finish onboarding")
    public void i_finish_onboarding() {
        for (int attempt = 1; ; attempt++) {
            try {
                registration.i_am_on_the_welcome_screen();
                if (ui.seen("Ride now")) {
                    ui.tapLabel("Ride now");
                }
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                if (attempt >= 3) {
                    throw e;
                }
                ui.sleep(1500); // the screen was still changing under the helper; look again
            }
        }
    }

    @Then("I am subscribed to launch notifications")
    public void i_am_subscribed_to_launch_notifications() {
        Assert.assertTrue(ui.waitSeen("subscribed", 10) || ui.waitSeen("notify you", 5) || ui.waitSeen("Thanks", 5),
                "No confirmation that the launch-notification subscription was recorded");
    }

    @Given("I completed onboarding")
    public void i_completed_onboarding() {
        registration.i_am_on_the_welcome_screen();
        if (ui.seen("Ride now")) {
            ui.tapLabel("Ride now");
        }
    }

    @Given("notification permission has not been granted")
    public void notification_permission_has_not_been_granted() {
        // Revoked before sign-up by SpecHooks (@no-notifications); revoking now would not make the app ask again.
    }

    @Then("I should see the notification permission screen")
    public void i_should_see_the_notification_permission_screen() {
        // Not the status bar: it always carries "notification" texts of its own.
        By screen = By.xpath("//*[not(@package='com.android.systemui') and "
                + "(contains(@text,'otification') or contains(@content-desc,'otification'))]");
        Assert.assertNotNull(ui.visible(screen, 15), "No notification permission screen or prompt was shown after onboarding");
    }

    @When("I deny the permission")
    public void i_deny_the_permission() {
        ui.tapLabel("Don");
    }
}
