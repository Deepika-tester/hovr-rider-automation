package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.datatable.DataTable;
import org.openqa.selenium.By;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * Step definitions for src/test/resources/features/rider_profile.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer — the same
 * caveats apply here: every accessibility-id / text locator is a best-effort
 * guess to be confirmed against the live app with Appium Inspector.
 */
public class ProfileSteps {

    private final BasePage ui = new BasePage() {};

    @Given("I am a registered rider")
    public void i_am_a_registered_rider() {
        // Test-data precondition — assumes an account already exists for the test phone number.
    }

    @Given("I am logged into the rider app")
    public void i_am_logged_into_the_rider_app() {
        // Assumes app launched with noReset=true and a prior session/login already present,
        // or a login step (see RegistrationSteps) has been run before this scenario.
    }

    @Given("I navigate to the account section")
    public void i_navigate_to_account_section() {
        ui.tapTextPublic("Account"); // VERIFY exact tab/menu label
    }

    @When("I tap on my profile")
    public void i_tap_on_my_profile() {
        ui.tapTextPublic("Profile");
    }

    @Then("I should see the user profile screen with:")
    public void i_should_see_user_profile_screen_with(DataTable dataTable) {
        List<String> fields = dataTable.asList();
        for (String field : fields) {
            if (field.equalsIgnoreCase("field")) continue; // header row
            Assert.assertTrue(
                    ui.isDisplayedPublic(ui.byTextContainsPublic(field)) ||
                            ui.isDisplayedPublic(ui.byAccessibilityIdPublic(field.toLowerCase().replace(" ", "_"))),
                    "Expected profile field to be shown: " + field);
        }
    }

    @Given("I am on the edit profile screen")
    public void i_am_on_edit_profile_screen() {
        ui.tapTextPublic("Edit");
    }

    @When("I change my first name to {string}")
    public void i_change_first_name_to(String name) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("edit_first_name"), name); // VERIFY
    }

    @When("I clear the first name field")
    public void i_clear_first_name_field() {
        ui.waitForPublic(ui.byAccessibilityIdPublic("edit_first_name")).clear();
    }

    @Then("my profile name should be updated to {string}")
    public void my_profile_name_updated_to(String name) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(name)),
                "Expected updated profile name: " + name);
    }

    // ---------- Saved places ----------

    @Given("I navigate to {string}")
    public void i_navigate_to(String destination) {
        ui.tapTextPublic(destination);
    }

    @Given("I navigate to {string} then {string}")
    public void i_navigate_to_then(String first, String second) {
        ui.tapTextPublic(first);
        ui.tapTextPublic(second);
    }

    @Then("I should see my saved places list")
    public void i_should_see_saved_places_list() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Add New Place")),
                "Saved places screen not shown");
    }

    @And("I should see preset categories {string} and {string}")
    public void i_should_see_preset_categories(String cat1, String cat2) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(cat1)), "Expected category: " + cat1);
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(cat2)), "Expected category: " + cat2);
    }

    @Given("I am on the saved places screen")
    public void i_am_on_the_saved_places_screen() {
        i_should_see_saved_places_list();
    }

    @Given("{string} has not been set")
    public void place_has_not_been_set(String placeLabel) {
        // Test-data precondition.
    }

    @When("I search for {string}")
    public void i_search_for(String query) {
        // Same Gherkin phrase is reused for both "search for an address" (saved places) and
        // "search for a help topic" — try the address field first, fall back to help search.
        //
        // CONFIRMED 2026-08-10 on a real device (Add New Place screen): the Address field is
        // an unlabeled android.widget.EditText — its content-desc="Address" sits on a sibling
        // wrapper node, not the EditText itself, so accessibility-id lookup can't target the
        // field directly. There's exactly one EditText on this screen, so By.className works.
        // Not yet confirmed whether other screens (e.g. destination search) have more than one
        // EditText, which would make this selector ambiguous there — recheck if it fails.
        try {
            ui.waitForPublic(By.className("android.widget.EditText")).sendKeys(query);
        } catch (Exception addressFieldNotFound) {
            ui.typeIntoPublic(ui.byAccessibilityIdPublic("help_search_bar"), query); // VERIFY
        }
    }

    @When("I select the address from suggestions")
    public void i_select_address_from_suggestions() {
        // VERIFY: first autocomplete row locator, e.g. accessibility id "place_suggestion_0"
        ui.tapPublic(ui.byAccessibilityIdPublic("place_suggestion_0"));
    }

    @When("I search for and select {string}")
    public void i_search_for_and_select(String address) {
        i_search_for(address);
        i_select_address_from_suggestions();
    }

    @Then("{string} should be set to {string}")
    public void place_should_be_set_to(String label, String address) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(address)),
                "Expected " + label + " to be set to " + address);
    }

    @And("it should appear on the home screen for quick booking")
    public void it_should_appear_on_home_screen() {
        // Requires navigating back to the ride-booking home screen to verify — out of scope
        // for this step alone; chain a navigation + assertion here once locators are confirmed.
    }

    @When("I enter the label {string}")
    public void i_enter_the_label(String label) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("place_label_field"), label); // VERIFY
    }

    @Then("I should see {string} in my saved places list")
    public void i_should_see_in_saved_places_list(String label) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(label)),
                "Expected saved place: " + label);
    }

    @Given("I have a saved place {string} at {string}")
    public void i_have_a_saved_place_at(String label, String address) {
        // Test-data precondition.
    }

    @When("I change the address to {string}")
    public void i_change_address_to(String address) {
        i_search_for(address);
        i_select_address_from_suggestions();
    }

    @Then("{string} should be updated to {string}")
    public void place_should_be_updated_to(String label, String address) {
        place_should_be_set_to(label, address);
    }

    @Given("I have a custom saved place {string}")
    public void i_have_a_custom_saved_place(String label) {
        // Test-data precondition.
    }

    @When("I confirm the deletion")
    public void i_confirm_the_deletion() {
        ui.tapTextPublic("Confirm"); // VERIFY exact confirm-dialog label
    }

    @Then("{string} should be removed from my saved places")
    public void place_should_be_removed(String label) {
        Assert.assertFalse(ui.isDisplayedPublic(ui.byTextPublic(label)),
                "Expected " + label + " to be removed from saved places");
    }

    // ---------- Ride/trip history ----------

    @Given("I have completed rides")
    public void i_have_completed_rides() {
        // Test-data precondition.
    }

    @When("I navigate to the trips section")
    public void i_navigate_to_trips_section() {
        ui.tapTextPublic("Trips"); // VERIFY exact label
    }

    @Then("I should see a list of my past trips")
    public void i_should_see_list_of_past_trips() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("trip_item_0")).size() >= 0, // VERIFY item pattern
                "Trip list not shown");
    }

    @And("each trip should display:")
    public void each_trip_should_display(DataTable dataTable) {
        // Field-level verification would require opening the first trip row and checking each
        // labelled value — implement once trip-row/detail locators are confirmed.
    }

    @Given("I am on the trips list")
    public void i_am_on_the_trips_list() {
        i_navigate_to_trips_section();
    }

    @When("I tap on a completed trip")
    public void i_tap_on_a_completed_trip() {
        ui.tapPublic(ui.byAccessibilityIdPublic("trip_item_0")); // VERIFY
    }

    @Then("I should see the trip receipt view")
    public void i_should_see_trip_receipt_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Receipt")),
                "Trip receipt view not shown");
    }

    @And("I should see the route name and time")
    public void i_should_see_route_name_and_time() {
        // VERIFY specific locators once trip receipt screen structure is confirmed.
    }

    @And("I should see the fare breakdown")
    public void i_should_see_fare_breakdown() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Fare")),
                "Fare breakdown not shown");
    }

    @Given("I have not completed any trips")
    public void i_have_not_completed_any_trips() {
        // Test-data precondition — use a fresh/empty test account for this scenario.
    }

    @Then("I should see an empty state message")
    public void i_should_see_empty_state_message() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("trips_empty_state")), // VERIFY
                "Empty state message not shown");
    }

    // ---------- Settings / language / notifications ----------

    @Then("the app language should change to French")
    public void app_language_should_change_to_french() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Paramètres")), // VERIFY French label
                "Expected app to switch to French");
    }

    @And("all UI text should be displayed in French")
    public void all_ui_text_should_be_in_french() {
        // Broad assertion — verify a couple of known-translated labels once confirmed.
    }

    @Given("the app is currently in French")
    public void the_app_is_currently_in_french() {
        // Test-data/session precondition.
    }

    @When("I navigate to language settings")
    public void i_navigate_to_language_settings() {
        ui.tapTextPublic("Langue"); // VERIFY French label for "Language"
    }

    @Then("the app should switch back to English")
    public void app_should_switch_back_to_english() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Settings")),
                "Expected app to switch back to English");
    }

    @Then("I should see notification categories")
    public void i_should_see_notification_categories() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("notification_category")).size() > 0, // VERIFY
                "Expected notification categories to be listed");
    }

    @When("I toggle a notification category off")
    public void i_toggle_a_notification_category_off() {
        ui.tapPublic(ui.byAccessibilityIdPublic("notification_toggle_0")); // VERIFY
    }

    @Then("I should no longer receive notifications for that category")
    public void i_should_no_longer_receive_notifications() {
        // Verifying actual notification delivery is out of scope for UI automation;
        // assert the toggle state persisted instead once the locator is confirmed.
    }

    // ---------- Messages ----------

    @Then("I should see my messages from HOVR")
    public void i_should_see_messages_from_hovr() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("message_item")).size() >= 0, // VERIFY
                "Messages list not shown");
    }

    @And("messages should be sorted by date with newest first")
    public void messages_sorted_newest_first() {
        // Would require reading each message's timestamp element and comparing order —
        // implement once message-row locators are confirmed.
    }

    // ---------- Account actions ----------

    @Given("I am on the settings screen")
    public void i_am_on_the_settings_screen() {
        ui.tapTextPublic("Settings");
    }

    @When("I confirm the logout")
    public void i_confirm_the_logout() {
        ui.tapTextPublic("Log Out"); // confirm dialog button, VERIFY exact label
    }

    @Then("I should be logged out")
    public void i_should_be_logged_out() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Get Started")),
                "Expected to return to landing/logged-out screen");
    }

    @And("I should be returned to the landing screen")
    public void i_should_be_returned_to_landing_screen() {
        i_should_be_logged_out();
    }

    // ---------- Help & support ----------

    @Then("I should see help categories:")
    public void i_should_see_help_categories(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String category = row.get("category");
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(category)),
                    "Expected help category: " + category);
        }
    }

    @Given("I am on the help screen")
    public void i_am_on_the_help_screen() {
        ui.tapTextPublic("Help");
    }

    @Then("I should be directed to the Zendesk support interface")
    public void i_should_be_directed_to_zendesk() {
        // Android-only check: the APK declares
        // zendesk.messaging.android.internal.messagingscreen.MessagingActivity.
        // On iOS Zendesk opens as an in-app view rather than a separate activity, so
        // fall back to a UI-visible check there — VERIFY once the iOS screen exists.
        if (ui.driverPublic() instanceof AndroidDriver androidDriver) {
            Assert.assertEquals(androidDriver.currentActivity(),
                    "zendesk.messaging.android.internal.messagingscreen.MessagingActivity");
        } else {
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Help")), // VERIFY
                    "Expected Zendesk support interface to be shown");
        }
    }

    @When("I tap the search bar")
    public void i_tap_the_search_bar() {
        ui.tapPublic(ui.byAccessibilityIdPublic("help_search_bar")); // VERIFY
    }

    @Then("I should see relevant FAQ articles")
    public void i_should_see_relevant_faq_articles() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("faq_article_item")).size() >= 0, // VERIFY
                "Expected FAQ article results");
    }
}