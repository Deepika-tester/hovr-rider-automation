package com.automation.spec.rider;

import io.appium.java_client.InteractsWithApps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.Map;

/**
 * Steps for features/rider/02_home_and_trip_planning.feature. Destinations in the feature files are Toronto examples
 * ("Union Station"); the riders here are in Mohali, so they are searched as their local equivalents (SpecContext places).
 * Nothing in this class confirms a ride: planning stops at the route/"Choose a ride" preview.
 */
public class HomeSpecSteps {

    private final SpecUi ui = new SpecUi();

    private static final By PICKUP_FIELD = By.xpath("(//android.widget.EditText)[1]");
    private static final By DROPOFF_FIELD = By.xpath("(//android.widget.EditText)[2]");
    // A suggestion row is a clickable node labelled "<place>\n<address>".
    private static final By SUGGESTION_ROW = By.xpath(
            "//*[@clickable='true' and contains(@content-desc,'\n') and not(contains(@content-desc,'Tab '))]");

    private void openPlanScreen() {
        if (SpecAdb.locationOff()) {
            // With location services off the app shows its own dialog; do not insist on a healthy ride home.
            ui.popup();
            if (ui.tryTapLabel("Where to?", 6)) {
                ui.waitSeen("Plan your trip", 8);
            }
            return;
        }
        SpecSession.goRide(ui);
        ui.tapLabel("Where to?");
        Assert.assertTrue(ui.waitSeen("Plan your trip", 15), "Plan your trip screen not shown");
    }

    private void typeDropoff(String place) {
        ui.typeIntoPublic(DROPOFF_FIELD, SpecMarket.place(place));
    }

    // ---- main navigation ----

    @Then("I should see the bottom navigation tabs {string}, {string} and {string}")
    public void i_should_see_the_bottom_navigation_tabs(String a, String b, String c) {
        for (String tab : new String[] {a, b, c}) {
            Assert.assertTrue(ui.waitSeen(tab + "\nTab", 10) || ui.waitSeen(tab, 5), "Missing bottom tab: " + tab);
        }
        Assert.assertTrue(SpecSession.tabsVisible(ui), "Bottom navigation tabs not shown");
    }

    @Then("I should see the {string} search field")
    public void i_should_see_the_search_field(String label) {
        Assert.assertTrue(ui.waitSeen(label, 10), "Search field not shown: " + label);
    }

    @Then("I should see the {string} and {string} tabs")
    public void i_should_see_the_two_tabs(String a, String b) {
        Assert.assertTrue(ui.waitSeen(a, 10) && ui.waitSeen(b, 5), "Expected the tabs " + a + " and " + b);
    }

    // ---- swipe guide (shown to a rider's first visit; scenarios are @fresh-install) ----

    @Given("this is my first visit to the home screen")
    public void this_is_my_first_visit_to_the_home_screen() {
        // Guaranteed by @fresh-install: the app is wiped before the session and the new rider's guide is left showing.
    }

    @When("I swipe up on the panel")
    public void i_swipe_up_on_the_panel() {
        SpecAdb.swipe(540, 2000, 540, 900, 400);
        ui.sleep(1500);
    }

    @When("I swipe down on the panel")
    public void i_swipe_down_on_the_panel() {
        SpecAdb.swipe(540, 1250, 540, 2000, 400);
        ui.sleep(1500);
    }

    @Given("the swipe guide is displayed")
    public void the_swipe_guide_is_displayed() {
        Assert.assertTrue(ui.waitSeen("Your turn", 10), "The swipe guide is not showing for this new rider");
    }

    @Then("the swipe guide is dismissed")
    public void the_swipe_guide_is_dismissed() {
        ui.sleep(1500);
        Assert.assertFalse(ui.seen("Your turn"), "The swipe guide is still showing");
    }

    @Then("it is not shown on the next launch")
    public void it_is_not_shown_on_the_next_launch() {
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp("production.hovr.customer");
        apps.activateApp("production.hovr.customer");
        Assert.assertTrue(ui.waitSeen("Where to?", 40), "Home did not come back after a relaunch");
        Assert.assertFalse(ui.seen("Your turn"), "The swipe guide came back after a relaunch");
    }

    // ---- plan your trip ----

    @Given("I am on the plan your trip screen")
    public void i_am_on_the_plan_your_trip_screen() {
        openPlanScreen();
    }

    @When("I open the plan your trip screen")
    public void i_open_the_plan_your_trip_screen() {
        openPlanScreen();
    }

    @Then("the pickup field is prefilled with {string}")
    public void the_pickup_field_is_prefilled_with(String text) {
        WebElement f = ui.visible(PICKUP_FIELD, 10);
        Assert.assertNotNull(f, "No pickup field");
        String shown = f.getText() == null ? "" : f.getText();
        String hint = f.getAttribute("hint") == null ? "" : f.getAttribute("hint");
        Assert.assertTrue(shown.contains(text) || hint.contains(text),
                "Pickup field shows \"" + shown + "\" (hint \"" + hint + "\") instead of \"" + text + "\"");
    }

    @When("I type {string} in the drop-off field")
    public void i_type_in_the_drop_off_field(String place) {
        typeDropoff(place);
    }

    @Then("I should see place suggestions")
    public void i_should_see_place_suggestions() {
        Assert.assertNotNull(ui.visible(SUGGESTION_ROW, 15), "No place suggestions appeared");
    }

    @When("I select the first suggestion")
    public void i_select_the_first_suggestion() {
        WebElement row = ui.visible(SUGGESTION_ROW, 15);
        Assert.assertNotNull(row, "No suggestion to select");
        row.click();
        ui.sleep(2500);
    }

    @Then("I should see the route preview")
    public void i_should_see_the_route_preview() {
        Assert.assertTrue(ui.waitSeen("Choose a ride", 30) || ui.waitSeen("Confirm", 5),
                "No route preview (\"Choose a ride\") after choosing a destination");
    }

    // ---- recent searches ----

    @Given("I have previous searches")
    public void i_have_previous_searches() {
        openPlanScreen();
        typeDropoff("Union Station");
        WebElement row = ui.visible(SUGGESTION_ROW, 15);
        Assert.assertNotNull(row, "Could not make a search to create a recent entry");
        row.click();
        ui.sleep(3000);
        SpecSession.ensureSignedIn(ui);
    }

    @Then("I should see the {string} section")
    public void i_should_see_the_section(String name) {
        Assert.assertTrue(ui.waitSeen(name, 10), "No \"" + name + "\" section on the plan your trip screen");
    }

    @Then("the recent searches list is empty")
    public void the_recent_searches_list_is_empty() {
        ui.sleep(1500);
        Assert.assertFalse(ui.seen("Clear all"), "Recent searches are still listed");
    }

    // ---- map pin ----

    @When("I drag the map to a new position")
    public void i_drag_the_map_to_a_new_position() {
        SpecAdb.swipe(540, 900, 300, 1300, 600);
        // "Fetching address..." only shows while the new pin position is being resolved: catch it if it appears.
        long end = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < end) {
            if (ui.seen("Fetching address")) {
                SpecContext.sawTransient("Fetching address...");
                break;
            }
            ui.sleep(150);
        }
    }

    @Then("the address field updates to the pinned location")
    public void the_address_field_updates_to_the_pinned_location() {
        ui.sleep(2500);
        Assert.assertFalse(ui.seen("Fetching address"), "Still fetching the address");
        Assert.assertTrue(ui.seen("Confirm") || ui.seen("Set location") || ui.seen("Mohali") || ui.seen("Punjab") || ui.seen("Toronto") || ui.seen("Ontario"),
                "No pinned address is shown");
    }

    // ---- stops ----

    @Given("I am on the plan your trip screen with a destination")
    public void i_am_on_the_plan_your_trip_screen_with_a_destination() {
        openPlanScreen();
        typeDropoff("Union Station");
        Assert.assertNotNull(ui.visible(SUGGESTION_ROW, 15), "No suggestions for the destination");
    }

    @When("I try to continue with an empty stop")
    public void i_try_to_continue_with_an_empty_stop() {
        // With an empty stop in the route the app cannot go on: choose the destination and watch for its message
        // (it is shown only for an instant, and the screen stays on "Plan your trip").
        By destination = By.xpath("(//android.widget.EditText)[last()]");
        ui.typeIntoPublic(destination, SpecMarket.destinationPrefix());
        WebElement row = ui.visible(SUGGESTION_ROW, 15);
        Assert.assertNotNull(row, "No destination suggestion to choose");
        row.click();
        long end = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < end) {
            if (ui.seen("Select a stop or remove it to continue")) {
                SpecContext.sawTransient("Select a stop or remove it to continue.");
                return;
            }
            ui.sleep(150);
        }
    }

    // ---- location unavailable ----

    @Given("location services are disabled")
    public void location_services_are_disabled() {
        SpecAdb.setLocationMode(0);
        ui.sleep(2000);
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp("production.hovr.customer");
        apps.activateApp("production.hovr.customer");
        ui.sleep(6000);
        ui.popup();
        ui.sleep(2000);
    }

    // ---- saved shortcuts ----

    @Given("I have saved a {string} place")
    public void i_have_saved_a_place(String name) {
        SpecSession.open(ui, "Saved Places");
        if (ui.seen(name)) {
            SpecSession.goRide(ui);
            return;
        }
        ui.tapPublic(SpecAliases.TOP_RIGHT_ACTION);
        ui.waitSeen("Add new place", 15);
        ui.typeIntoPublic(By.xpath("(//android.widget.EditText)[1]"), name);
        ui.typeIntoPublic(By.xpath("(//android.widget.EditText)[2]"), SpecMarket.savedAddress());
        By suggestion = By.xpath("(//android.widget.Button[string-length(@content-desc)>12 and not(@content-desc='Save')])[1]");
        ui.waitForPublic(suggestion);
        ui.tapPublic(suggestion);
        ui.tapLabel("Save");
        Assert.assertTrue(ui.waitSeen(name, 15), "Could not save the place " + name);
        SpecSession.goRide(ui);
    }

    @When("I tap the {string} shortcut")
    public void i_tap_the_shortcut(String name) {
        SpecSession.goRide(ui);
        ui.tapLabel(name);
        ui.sleep(2500);
    }

    @Then("the drop-off is set to my Home address")
    public void the_drop_off_is_set_to_my_home_address() {
        Assert.assertTrue(ui.waitSeen("Choose a ride", 30) || ui.waitSeen("CP67", 5),
                "Tapping the Home shortcut did not set the drop-off");
    }

    @Given("I have no saved Home place")
    public void i_have_no_saved_home_place() {
        SpecSession.open(ui, "Saved Places");
        for (int i = 0; i < 3 && ui.seen("Home") && !ui.seen("No saved places"); i++) {
            ui.tapLabel("Home");
            ui.waitSeen("Edit saved place", 10);
            ui.tapPublic(SpecAliases.TOP_RIGHT_ACTION);
            ui.sleep(3000);
            SpecSession.open(ui, "Saved Places");
        }
        SpecSession.goRide(ui);
    }

    @Then("I should see the add new place screen")
    public void i_should_see_the_add_new_place_screen() {
        Assert.assertTrue(ui.waitSeen("Add new place", 15), "The add new place screen did not open");
    }

    // ---- schedule for later ----

    private static final By HOUR_WHEEL = By.xpath("//android.widget.SeekBar[contains(@content-desc,\"o'clock\")]");

    private void openChooseTime() {
        openPlanScreen();
        ui.tapLabel("Schedule");
        Assert.assertTrue(ui.waitSeen("Choose a time", 15), "The Choose a time screen did not open");
    }

    private void scrollHour(String direction) {
        WebElement wheel = ui.visible(HOUR_WHEEL, 10);
        Assert.assertNotNull(wheel, "No hour wheel on the time picker");
        ((org.openqa.selenium.JavascriptExecutor) ui.driverPublic()).executeScript("mobile: swipeGesture", Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) wheel).getId(),
                "direction", direction, "percent", 0.12));
        ui.sleep(1200);
    }

    @When("I tap the time selector")
    public void i_tap_the_time_selector() {
        openPlanScreen();
        ui.tapLabel("Schedule");
    }

    @Given("I am on the choose time screen")
    public void i_am_on_the_choose_time_screen() {
        openChooseTime();
    }

    @When("I choose a pickup time {}")
    public void i_choose_a_pickup_time(String when) {
        // The picker opens on the earliest allowed time (about 90 minutes ahead). Later = scroll the hour wheel up one
        // notch; earlier than allowed = scroll it down.
        if (when.contains("2 hours")) {
            scrollHour("up");
        } else {
            scrollHour("down");
        }
    }

    @Then("the trip is planned for the selected time")
    public void the_trip_is_planned_for_the_selected_time() {
        ui.sleep(2000);
        Assert.assertFalse(ui.seen("Choose a time"), "Still on the time picker");
        Assert.assertTrue(ui.seen(" PM") || ui.seen(" AM"), "The chosen time is not shown on the plan screen");
    }

    // ---- unsupported pickup / no route ----

    @Given("my pickup point is outside the service area")
    public void my_pickup_point_is_outside_the_service_area() {
        ApiNote.set("pickup", "Mumbai Airport");
    }

    @Given("my destination has no drivable route")
    public void my_destination_has_no_drivable_route() {
        ApiNote.set("dropoff", "Lakshadweep");
    }

    @When("I request route options")
    public void i_request_route_options() {
        openPlanScreen();
        String pickup = ApiNote.get("pickup", null);
        if (pickup != null) {
            ui.typeIntoPublic(PICKUP_FIELD, pickup);
            WebElement row = ui.visible(SUGGESTION_ROW, 8);
            if (row == null) {
                return; // the app offers no such place ("Hmm, we couldn't find that location"); the assertion reports it
            }
            row.click();
            ui.sleep(1500);
        }
        typeDropoff(ApiNote.get("dropoff", SpecMarket.savedAddress()));
        WebElement row = ui.visible(SUGGESTION_ROW, 8);
        if (row == null) {
            return;
        }
        row.click();
        ui.sleep(4000);
    }
}
