package com.automation.spec.rider;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/** Steps for features/rider/06_account_profile_settings.feature. */
public class AccountSpecSteps {

    private final SpecUi ui = new SpecUi();

    private static final By NAME_FIELD = By.xpath("(//android.widget.EditText)[1]");
    private static final By ADDRESS_FIELD = By.xpath("(//android.widget.EditText)[2]");
    private static final By FIRST_SAVED_PLACE = By.xpath("//android.widget.Button[contains(@content-desc,'\n')]");
    private static final By PROFILE_CARD = By.xpath("//*[@clickable='true' and contains(@content-desc,'View profile')]");

    // ---- profile ----

    @Given("I am on the edit profile screen")
    public void i_am_on_the_edit_profile_screen() {
        SpecSession.goAccount(ui);
        ui.tapPublic(PROFILE_CARD);
        Assert.assertTrue(ui.waitSeen("Legal Name", 20), "Profile page not shown");
        ui.tapPublic(SpecAliases.TOP_RIGHT_ACTION); // the pencil
        Assert.assertTrue(ui.waitSeen("Edit profile", 15), "Edit profile screen not shown");
    }

    @When("I change preferred name to {string}")
    public void i_change_preferred_name_to(String name) {
        ui.typeIntoPublic(NAME_FIELD, name);
    }

    @When("I choose a new profile photo")
    public void i_choose_a_new_profile_photo() {
        throw new io.cucumber.java.PendingException("needs a photo in the gallery / camera; see @blocked note");
    }

    // ---- saved places ----

    private void typeAddressAndPickFirstSuggestion() {
        ui.typeIntoPublic(ADDRESS_FIELD, SpecMarket.savedAddress());
        By suggestion = By.xpath("(//android.widget.Button[string-length(@content-desc)>12 "
                + "and not(@content-desc='Save')])[1]");
        ui.waitForPublic(suggestion);
        ui.tapPublic(suggestion);
        ui.sleep(800);
    }

    @When("I enter name {string} and select an address suggestion")
    public void i_enter_name_and_select_an_address_suggestion(String name) {
        ui.typeIntoPublic(NAME_FIELD, name);
        typeAddressAndPickFirstSuggestion();
    }

    @Given("I am on the add new place screen")
    public void i_am_on_the_add_new_place_screen() {
        SpecSession.open(ui, "Saved Places");
        ui.tapPublic(SpecAliases.TOP_RIGHT_ACTION);
        Assert.assertTrue(ui.waitSeen("Add new place", 15), "Add new place screen not shown");
    }

    @When("I tap {string} with empty fields")
    public void i_tap_with_empty_fields(String label) {
        ui.tapLabel(label);
        ui.sleep(1200);
    }

    @Given("I have a saved place {string}")
    public void i_have_a_saved_place(String name) {
        SpecSession.open(ui, "Saved Places");
        if (ui.seen(name) && !ui.seen("No saved places")) {
            return;
        }
        ui.tapPublic(SpecAliases.TOP_RIGHT_ACTION);
        ui.waitSeen("Add new place", 15);
        ui.typeIntoPublic(NAME_FIELD, name);
        typeAddressAndPickFirstSuggestion();
        ui.tapLabel("Save");
        Assert.assertTrue(ui.waitSeen(name, 15), "Could not create the saved place " + name);
    }

    @Then("{string} is removed from saved places")
    public void is_removed_from_saved_places(String name) {
        // The list keeps showing the deleted row until it reloads, so look at a freshly opened list.
        SpecSession.open(ui, "Saved Places");
        Assert.assertTrue(ui.waitSeen("No saved places", 15) || ui.waitGone(name, 10),
                name + " is still listed under Saved Places after reopening the list");
    }

    // ---- notification preferences ----

    @Given("I open notification preferences for {string}")
    public void i_open_notification_preferences_for(String category) {
        SpecSession.open(ui, "Settings");
        ui.tapLabel("Notifications");
        ui.waitSeen("Promotions and Tips", 15);
        ui.tapLabel(category);
        ui.sleep(1500);
    }

    @When("I turn off all of {string}, {string} and {string}")
    public void i_turn_off_all_of(String a, String b, String c) {
        for (String label : new String[] {a, b, c}) {
            By sw = By.xpath("//android.widget.Switch[contains(@content-desc,'" + label + "')]");
            WebElement el = ui.visible(sw, 8);
            Assert.assertNotNull(el, "No \"" + label + "\" switch on this screen: " + "this category does not have channel switches");
            if ("true".equals(el.getAttribute("checked"))) {
                el.click();
                ui.sleep(1200);
            }
        }
    }

    // ---- app update / logout ----

    @Given("I open the app update screen")
    public void i_open_the_app_update_screen() {
        SpecSession.goAccount(ui);
        ui.tapLabel("Update the app");
        Assert.assertTrue(ui.waitSeen("Check for update", 15), "App update screen not shown");
    }

    @Then("I am returned to the landing screen")
    public void i_am_returned_to_the_landing_screen() {
        Assert.assertTrue(ui.waitSeen("Get Started", 20), "Landing screen not shown after logging out");
    }

    @When("I confirm")
    public void i_confirm() {
        throw new io.cucumber.java.PendingException("irreversible (account deletion); see @blocked note");
    }
}
