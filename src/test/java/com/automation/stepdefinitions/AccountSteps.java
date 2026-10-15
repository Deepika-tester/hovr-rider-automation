package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

/**
 * Step definitions for src/test/resources/features/rider/rider_profile.feature (rider Account area:
 * profile, saved places, settings, wallet, inbox, help, referral, trips).
 *
 * Every locator was read off a real screen (rider dev v6.3.0+588, emulator, 2026-10-14). The app renders
 * labels as content-desc (often "Title\nSubtitle"), so text checks use "contains". Icon-only buttons
 * (profile pencil, Saved Places "+", saved-place trash) have no label at all: each is the only
 * unlabelled clickable View on its screen besides the labelled Back button, so it is found as the
 * last unlabelled clickable View. Generic steps ("I tap {string}", "I should see the text {string}") live
 * in CommonSteps / CommuteSteps.
 *
 * Deliberately NOT automated: "Delete Account" (irreversible) and changing the app language (it
 * re-localises every later screen). Their screens are only checked for presence.
 */
public class AccountSteps {

    private final BasePage ui = new BasePage() {};

    private static final By ACCOUNT_TAB = By.xpath("//*[contains(@content-desc,'Tab 3 of 3')]");
    private static final By TRIPS_TAB = By.xpath("//*[contains(@content-desc,'Tab 2 of 3')]");
    private static final By RIDE_TAB = By.xpath("//*[contains(@content-desc,'Tab 1 of 3')]");
    private static final By PROFILE_CARD = By.xpath("//*[@clickable='true' and contains(@content-desc,'View profile')]");
    // Top-right icon button on Profile (pencil), Saved Places ("+") and Edit saved place (trash).
    private static final By TOP_RIGHT_ACTION = By.xpath(
            "(//*[(self::android.view.View or self::android.widget.Button) and @clickable='true' "
                    + "and string-length(@content-desc)=0])[last()]");
    private static final By NAME_FIELD = By.xpath("(//android.widget.EditText)[1]");
    private static final By ADDRESS_FIELD = By.xpath("(//android.widget.EditText)[2]");
    private static final By EDIT_FIELDS = By.xpath("//android.widget.EditText");
    private static final By SWITCHES = By.xpath("//android.widget.Switch");

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

    /** Lands on the Account page whatever a previous scenario left open (sub-page, form, dialog). */
    private void ensureOnAccountPage() {
        if (shown("Earn by driving")) {
            return;
        }
        RegistrationSteps registration = new RegistrationSteps();
        // Registering a fresh rider is the flakiest part of the suite (OTP screen / ride home sometimes
        // does not come up on the first try), so give it one more go before failing the scenario.
        for (int attempt = 1; ; attempt++) {
            try {
                boolean loggedOut = shown("Get Started") || shown("Enter your phone number");
                if (loggedOut) {
                    registration.ensureOnRideHomeScreen(); // registers a fresh rider and reaches ride home
                } else {
                    registration.relaunchToRideHome();     // clean slate: closes forms, pickers, dialogs
                }
                break;
            } catch (AssertionError | RuntimeException e) {
                if (attempt >= 2) {
                    throw e;
                }
                registration.relaunchToRideHome();
            }
        }
        ui.tapPublic(ACCOUNT_TAB);
        ui.waitForPublic(ui.byTextContainsPublic("Earn by driving"));
    }

    // ---- navigation ----

    @Given("I am on the Account page")
    public void i_am_on_the_account_page() {
        ensureOnAccountPage();
    }

    @When("I open the {string} bottom tab")
    public void i_open_the_bottom_tab(String tab) {
        switch (tab) {
            case "Ride" -> ui.tapPublic(RIDE_TAB);
            case "Trips" -> ui.tapPublic(TRIPS_TAB);
            case "Account" -> ui.tapPublic(ACCOUNT_TAB);
            default -> throw new IllegalArgumentException("Unknown bottom tab: " + tab);
        }
        sleep(1500);
    }

    @When("I open my profile")
    public void i_open_my_profile() {
        ui.tapPublic(PROFILE_CARD);
        // The profile page paints its header first and the details a moment later.
        ui.waitForPublic(ui.byTextContainsPublic("Legal Name"));
    }

    @When("I go back")
    public void i_go_back() {
        ui.driverPublic().navigate().back();
        sleep(1500);
    }

    @When("I open the Saved Places list")
    public void i_open_the_saved_places_list() {
        ui.tapTextPublic("Settings");
        ui.waitForPublic(ui.byTextContainsPublic("Shortcuts"));
        ui.tapTextPublic("Home");
        ui.waitForPublic(ui.byTextContainsPublic("Saved Places"));
        sleep(1500);
    }

    // Rows such as "Language\nEnglish (US)" carry a subtitle in the same label, so match on the title only.
    @When("I open the {string} setting")
    public void i_open_the_setting(String title) {
        ui.tapPublic(ui.byTextContainsPublic(title));
        sleep(1500);
    }

    // The wallet sometimes sits on its loading spinner for a long time on the dev backend; reopen it once.
    @When("I open my wallet")
    public void i_open_my_wallet() {
        for (int attempt = 1; ; attempt++) {
            ui.tapTextPublic("Wallet");
            try {
                new org.openqa.selenium.support.ui.WebDriverWait(ui.driverPublic(), java.time.Duration.ofSeconds(40))
                        .until(d -> shown("Payment Methods"));
                return;
            } catch (org.openqa.selenium.TimeoutException e) {
                if (attempt >= 2) {
                    throw e;
                }
                ui.driverPublic().navigate().back();
                sleep(2000);
            }
        }
    }

    // ---- profile ----

    @When("I tap the edit pencil")
    public void i_tap_the_edit_pencil() {
        ui.tapPublic(TOP_RIGHT_ACTION);
        ui.waitForPublic(ui.byTextContainsPublic("Edit profile"));
    }

    @When("I set my preferred name to {string}")
    public void i_set_my_preferred_name_to(String name) {
        ui.typeIntoPublic(By.xpath("(//android.widget.EditText)[1]"), name);
    }

    @When("I clear the first name on the edit form")
    public void i_clear_the_first_name_on_the_edit_form() {
        // Second editable field (after Preferred name). Compose ignores clear() on a prefilled field,
        // so select-all + delete through the keyboard instead.
        WebElement first = ui.waitForPublic(By.xpath("(//android.widget.EditText)[2]"));
        first.click();
        first.clear();
        if (first.getText() != null && !first.getText().isEmpty()) {
            for (int i = 0; i < 40; i++) {
                ((io.appium.java_client.android.AndroidDriver) ui.driverPublic())
                        .pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                                io.appium.java_client.android.nativekey.AndroidKey.DEL));
            }
        }
    }

    @Then("the edit form should have exactly {int} editable fields")
    public void the_edit_form_should_have_exactly_editable_fields(int count) {
        Assert.assertEquals(ui.findAllPublic(EDIT_FIELDS).size(), count,
                "Phone and email are read-only, so only the name fields should be editable");
    }

    // ---- saved places ----

    @When("I tap the add place button")
    public void i_tap_the_add_place_button() {
        ui.tapPublic(TOP_RIGHT_ACTION);
        ui.waitForPublic(ui.byTextContainsPublic("Add new place"));
    }

    @When("I enter the place name {string}")
    public void i_enter_the_place_name(String name) {
        ui.typeIntoPublic(NAME_FIELD, name);
    }

    @When("I enter the place address {string}")
    public void i_enter_the_place_address(String address) {
        ui.typeIntoPublic(ADDRESS_FIELD, address);
    }

    @When("I choose the first address suggestion")
    public void i_choose_the_first_address_suggestion() {
        By suggestion = By.xpath("(//android.widget.Button[string-length(@content-desc)>12 "
                + "and not(@content-desc='Save')])[1]");
        ui.waitForPublic(suggestion);
        ui.tapPublic(suggestion);
        sleep(1000);
    }

    @When("I open the saved place {string}")
    public void i_open_the_saved_place(String name) {
        ui.tapPublic(By.xpath("//android.widget.Button[contains(@content-desc,'" + name + "')]"));
        ui.waitForPublic(ui.byTextContainsPublic("Edit saved place"));
    }

    @When("I tap the delete icon")
    public void i_tap_the_delete_icon() {
        ui.tapPublic(TOP_RIGHT_ACTION);
        sleep(2500);
    }

    // ---- settings toggles ----

    private int switchIndex(String label) {
        return switch (label) {
            case "Wheelchair Assistance" -> 0;
            case "Verify Your Ride" -> 1;
            default -> throw new IllegalArgumentException("Unknown settings switch: " + label);
        };
    }

    private WebElement settingsSwitch(String label) {
        ui.waitForPublic(SWITCHES);
        return ui.findAllPublic(SWITCHES).get(switchIndex(label));
    }

    @When("I toggle the {string} switch")
    public void i_toggle_the_switch(String label) {
        settingsSwitch(label).click();
        sleep(2000);
    }

    @Then("the {string} switch should be {word}")
    public void the_switch_should_be(String label, String state) {
        boolean on = "true".equals(settingsSwitch(label).getAttribute("checked"));
        Assert.assertEquals(on, "on".equals(state), label + " switch should be " + state);
    }

    // ---- generic multi-text assertion ----

    @Then("I should see all of these texts:")
    public void i_should_see_all_of_these_texts(DataTable table) {
        List<String> texts = table.asList();
        for (String text : texts) {
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(text)), "Expected to see: " + text);
        }
    }

    @And("I should see a {string} switch")
    public void i_should_see_a_switch(String label) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(label)), "Expected setting: " + label);
        Assert.assertFalse(ui.findAllPublic(SWITCHES).isEmpty(), "Expected a switch on the screen");
    }
}
