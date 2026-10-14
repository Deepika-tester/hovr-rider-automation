package com.automation.driver.stepdefinitions;

import com.automation.pages.BasePage;
import com.automation.stepdefinitions.RegistrationSteps;
import com.automation.utils.ConfigReader;
import io.appium.java_client.InteractsWithApps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

/**
 * Step definitions for src/test/resources/features/driver/driver_profile.feature (driver dashboard,
 * menu, profile, help, vehicle documents and the Co-Commute tab).
 *
 * Read off a real screen (driver dev v6.2.4+592, emulator, India marketplace, 2026-10-14). Like the rider
 * app, labels are content-desc and several carry a second line ("Vehicle\nRequired · 1/1"), so cards are
 * opened with a contains() match. Account-area scenarios sign in as the existing test driver (see
 * driver-emulator.properties: testDriverPhone) with the 111111 staging OTP instead of registering a new
 * driver every time; registration itself is covered by DriverRegistrationSteps.
 *
 * Lives in the driver glue package so its generic wording ("I should see the text ...") cannot collide
 * with the rider suite's steps.
 */
public class DriverAccountSteps {

    private final BasePage ui = new BasePage() {};

    private static final String STAGING_BYPASS_OTP = "111111";
    private static final String APP_PACKAGE = "com.example.hovr_driver";

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

    private boolean onDashboard() {
        return shown("set up your account") && shown("Show menu");
    }

    private void waitForAny(int seconds, String... texts) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            ui.dismissUpdatePopupPublic();
            for (String t : texts) {
                if (shown(t)) {
                    return;
                }
            }
            sleep(500);
        }
    }

    /** Lands on the driver dashboard whatever a previous scenario left open, signing in if needed. */
    private void ensureOnDashboard() {
        if (onDashboard()) {
            return;
        }
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp(APP_PACKAGE);
        apps.activateApp(APP_PACKAGE);
        waitForAny(25, "set up your account", "Get Started");
        if (onDashboard()) {
            return;
        }
        Assert.assertTrue(shown("Get Started"), "Expected the dashboard or the landing page after a relaunch");
        signInAsTestDriver();
        waitForAny(40, "set up your account");
        Assert.assertTrue(onDashboard(), "Driver dashboard not shown after signing in");
    }

    private void signInAsTestDriver() {
        String phone = ConfigReader.get("testDriverPhone", "");
        Assert.assertFalse(phone.isBlank(), "testDriverPhone is not configured for this platform");
        ui.tapTextPublic("Get Started");
        ui.waitForPublic(ui.byTextPublic("Enter your phone number"));
        new RegistrationSteps().selectIndiaCountryCode();
        WebElement field = ui.waitForPublic(By.className("android.widget.EditText"));
        field.click();
        field.clear();
        field.sendKeys(phone);
        ui.tapTextPublic("Continue");
        ui.waitForPublic(ui.byTextContainsPublic("digit code sent"));
        WebElement otp = ui.waitForPublic(By.className("android.widget.EditText"));
        otp.click();
        otp.clear();
        otp.sendKeys(STAGING_BYPASS_OTP); // the 6th digit submits the code
    }

    // ---- navigation ----

    @Given("I am signed in as the test driver")
    public void i_am_signed_in_as_the_test_driver() {
        ensureOnDashboard();
    }

    @When("I open the {string} card")
    public void i_open_the_card(String title) {
        ui.tapPublic(ui.byTextContainsPublic(title));
        sleep(1500);
    }

    @When("I switch to the {string} dashboard tab")
    public void i_switch_to_the_dashboard_tab(String tab) {
        ui.tapPublic(By.xpath("//*[contains(@content-desc,'" + tab + "') and contains(@content-desc,'Tab ')]"));
        sleep(1500);
    }

    @When("I open the driver menu")
    public void i_open_the_driver_menu() {
        ui.tapPublic(By.xpath("//*[@content-desc='Show menu']"));
        sleep(1000);
    }

    @When("I go back one screen")
    public void i_go_back_one_screen() {
        ui.driverPublic().navigate().back();
        sleep(1500);
    }

    @When("I tap the driver {string} button")
    public void i_tap_the_driver_button(String label) {
        ui.tapPublic(By.xpath("//*[@content-desc='" + label + "' or @text='" + label + "']"));
        sleep(1500);
    }

    // ---- assertions ----

    @Then("I should see the driver text {string}")
    public void i_should_see_the_driver_text(String text) {
        ui.waitForPublic(ui.byTextContainsPublic(text));
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(text)), "Expected to see: " + text);
    }

    @Then("I should not see the driver text {string}")
    public void i_should_not_see_the_driver_text(String text) {
        sleep(1000);
        Assert.assertFalse(shown(text), "Did not expect to see: " + text);
    }

    @Then("I should see all of these driver texts:")
    public void i_should_see_all_of_these_driver_texts(DataTable table) {
        List<String> texts = table.asList();
        for (String text : texts) {
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(text)), "Expected to see: " + text);
        }
    }
}
