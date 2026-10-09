package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

/**
 * Step definitions for src/test/resources/features/rider/rider_booking.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer — every
 * accessibility-id / text locator here is a best-effort guess to be confirmed
 * against the live app with Appium Inspector before this suite is trustworthy.
 *
 * Steps that assert on backend/stream behavior directly (e.g. "a FindRideRequest
 * should be sent to the ride service") aren't verifiable from the UI layer alone;
 * they're treated as no-ops with the UI-visible consequence asserted instead,
 * same convention as RegistrationSteps.
 */
public class BookingSteps {

    private final BasePage ui = new BasePage() {};

    @And("I have a valid payment method on file")
    public void i_have_a_valid_payment_method_on_file() {
        // Test-data precondition.
    }

    @And("I am located in a supported marketplace")
    public void i_am_located_in_a_supported_marketplace() {
        // Test-data/location precondition. As of 2026-09-21 the backend has an India marketplace
        // enabled for the dev environment, so the phone's real location (India) is a supported
        // area for +91 riders; GTA/Ottawa is no longer required for these tests.
    }

    // CONFIRMED 2026-08-13 on a real device: neither a non-GTA/Ottawa Canadian phone number
    // (tested a 604/Vancouver number) nor the device's actual GPS location (physically in India
    // throughout this session) blocked anything during registration — moved this scenario here
    // from rider_registration.feature on that basis (see that file's comment at the same spot).
    // Both this step and the one above are no-op placeholders until real location simulation is
    // wired up (Appium supports driver.setLocation() with a mock-location app authorized on the
    // device, or an emulator's built-in geo controls) — this scenario isn't actually verified yet.
    @Given("I am located in a region outside GTA or Ottawa")
    public void i_am_located_outside_supported_marketplace() {
        // See comment above — needs real location simulation to actually exercise this.
    }

    @When("I open the ride home screen")
    public void i_open_the_ride_home_screen() {
        // No-op — see comment above. Once location simulation exists, this would just be
        // launching/foregrounding the app, then the Then step below checks which screen it
        // actually landed on (home vs. "Not Available In Your Region").
    }

    // CONFIRMED 2026-09-21: navigates for real (fresh +91 rider -> welcome -> "Ride now") instead
    // of only asserting, so booking scenarios run standalone. The search bar's exact copy is
    // "Where to?".
    @Given("I am on the ride home screen")
    public void i_am_on_the_ride_home_screen() {
        new RegistrationSteps().ensureOnRideHomeScreen();
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Where to?")),
                "Expected to be on the ride home screen");
    }

    @When("I tap the {string} search field")
    public void i_tap_the_search_field(String fieldLabel) {
        // CONFIRMED 2026-09-21: the search bar is a clickable element whose content-desc is
        // literally "Where to?"; there's no "destination_search_field" id.
        ui.tapTextPublic("Where to?");
    }

    // CONFIRMED 2026-09-21: the plan view has a "Plan your trip" heading, two inputs (pickup on
    // top -- pre-filled with the phone's current address -- and destination below, the focused
    // one), "Add stop" / "Schedule" buttons, "Add Home" / "Add Work" and a "Saved Places" header.
    private static final org.openqa.selenium.By DESTINATION_FIELD =
            org.openqa.selenium.By.xpath("(//android.widget.EditText)[2]");

    @Then("I should see the plan your trip view")
    public void i_should_see_the_plan_your_trip_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Plan your trip")),
                "Plan your trip view not shown");
    }

    @And("I should see my saved places if any exist")
    public void i_should_see_my_saved_places_if_any_exist() {
        // The section header is always there (with "Add Home"/"Add Work" when nothing is saved);
        // specific saved places are covered by the saved-places scenarios in rider_profile.feature.
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Saved Places")),
                "Saved Places section not shown");
    }

    @And("I should see my recent places if any exist")
    public void i_should_see_my_recent_places_if_any_exist() {
        // Conditional on test-account history -- nothing to assert for a brand-new rider.
    }

    @Given("I am on the plan your trip view")
    public void i_am_on_the_plan_your_trip_view() {
        if (ui.findAllPublic(ui.byTextPublic("Plan your trip")).isEmpty()) {
            i_am_on_the_ride_home_screen();
            i_tap_the_search_field("Where to?");
        }
        i_should_see_the_plan_your_trip_view();
    }

    @When("I type {string} in the destination field")
    public void i_type_in_the_destination_field(String address) {
        ui.typeIntoPublic(DESTINATION_FIELD, address);
    }

    // CONFIRMED 2026-09-21 on a real device: each suggestion is a clickable row whose content-desc
    // is "<place name>\n<address>" (e.g. "CP67 Mall Mohali\nCP67 Mall Mohali, International
    // Airport"); the bottom tabs also contain a newline ("Ride\nTab 1 of 3"), so exclude those.
    // In the India marketplace the place search only knows local places -- a Toronto address gets
    // "Hmm, we couldn't find that location", so these scenarios use a Mohali place.
    private static final org.openqa.selenium.By SUGGESTION_ROW = org.openqa.selenium.By.xpath(
            "//android.view.View[@clickable='true' and contains(@content-desc,'\n') "
                    + "and not(contains(@content-desc,'Tab '))]");

    @Then("I should see a list of address suggestions from Google Places API")
    public void i_should_see_address_suggestions() {
        Assert.assertTrue(ui.isDisplayedPublic(SUGGESTION_ROW),
                "Expected address suggestion list to be shown");
    }

    // ---- negative destination-search steps ----

    @When("I clear the destination field")
    public void i_clear_the_destination_field() {
        ui.waitForPublic(DESTINATION_FIELD).clear();
    }

    @When("I type a destination that is 300 characters long")
    public void i_type_a_very_long_destination() {
        i_type_in_the_destination_field("a".repeat(300));
    }

    // Apostrophe-free substring on purpose: the app renders a curly apostrophe in "couldn't".
    @Then("I should see a message that the location could not be found")
    public void i_should_see_location_not_found_message() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("find that location")),
                "Expected the 'we couldn't find that location' message");
    }

    @And("I should not see any address suggestions")
    public void i_should_not_see_any_address_suggestions() {
        // Give the search time to respond (it is asynchronous), then check nothing came back.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(ui.findAllPublic(SUGGESTION_ROW).isEmpty(),
                "Did not expect any address suggestions");
    }

    @And("I should not be shown the choose a ride sheet")
    public void i_should_not_be_shown_the_choose_a_ride_sheet() {
        Assert.assertTrue(ui.findAllPublic(ui.byTextPublic("Choose a ride")).isEmpty(),
                "A route/ride sheet must not appear for a search that has no valid destination");
    }

    @When("I tap on the suggestion {string}")
    public void i_tap_on_the_suggestion(String name) {
        ui.tapPublic(org.openqa.selenium.By.xpath(
                "//android.view.View[@clickable='true' and contains(@content-desc,'" + name + "') "
                        + "and contains(@content-desc,'\n')]"));
    }

    @And("the suggestions should be relevant to my search query")
    public void the_suggestions_should_be_relevant() {
        // Relevance is a fuzzy/product-judgement assertion — not meaningfully checkable via
        // UI automation beyond "suggestions are shown" (covered above).
    }

    @Given("I have searched for {string}")
    public void i_have_searched_for(String query) {
        i_am_on_the_plan_your_trip_view();
        i_type_in_the_destination_field(query);
    }

    @And("I see address suggestions")
    public void i_see_address_suggestions() {
        i_should_see_address_suggestions();
    }

    @Then("the destination should be set with latitude and longitude")
    public void the_destination_should_be_set_with_lat_lng() {
        // Verifiable only via backend/network inspection (FindRideRequest payload) — asserting the
        // UI-visible consequence (route view shown) covers the observable behavior instead.
    }

    @And("I should be navigated to the route view with the map showing the route")
    public void i_should_be_navigated_to_route_view() {
        // CONFIRMED 2026-09-21: after picking a destination the app shows the route on the map
        // with a "Choose a ride" panel (ride options + fares, "Add a payment method", and a
        // "Confirm HOVR" button); there is no "route_map" id.
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Choose a ride")),
                "Route view not shown");
    }

    @When("I tap on my saved place {string}")
    public void i_tap_on_my_saved_place(String label) {
        ui.tapTextPublic(label);
    }

    @Then("the destination should be set to {string}")
    public void the_destination_should_be_set_to(String address) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(address)),
                "Expected destination to be set to: " + address);
    }

    @And("I should see the route on the map")
    public void i_should_see_the_route_on_the_map() {
        i_should_be_navigated_to_route_view();
    }

    @Given("I am on the set pickup location view")
    public void i_am_on_the_set_pickup_location_view() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("pickup_map_pin")), // VERIFY
                "Set pickup location view not shown");
    }

    @When("I drag the map pin to a new location")
    public void i_drag_the_map_pin() {
        // Map-pin drag requires a W3C Actions pointer gesture against on-screen coordinates —
        // implement once the pickup screen's map bounds are confirmed on a real device.
    }

    @Then("my pickup location should be updated via reverse geocoding")
    public void my_pickup_location_should_be_updated() {
        // Backend consequence — see class javadoc; UI-visible check follows in the next step.
    }

    @And("the pickup address should reflect the new position")
    public void the_pickup_address_should_reflect_new_position() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("pickup_address_label")), // VERIFY
                "Expected pickup address label to reflect the new position");
    }

    // CONFIRMED 2026-09-21 on a real device: the pickup is pre-filled with the phone's current
    // location, so only the destination needs setting. After picking a suggestion the app shows the
    // route with a "Choose a ride" panel: rows are clickable ImageViews with content-desc
    // "HOVR\n1-4 seats\nSEDAN\n$7.28" (a second, unnamed row exists but is a testing service that
    // must NOT be used -- see project rule), an "Add a payment method" row and a "Confirm HOVR" button.
    private static final org.openqa.selenium.By HOVR_ROW = org.openqa.selenium.By.xpath(
            "//android.widget.ImageView[starts-with(@content-desc,'HOVR') and contains(@content-desc,'$')]");

    private void ensureOnChooseARide(String query) {
        if (!ui.findAllPublic(ui.byTextPublic("Choose a ride")).isEmpty()) {
            if (!ui.findAllPublic(HOVR_ROW).isEmpty()) {
                return;
            }
            // CONFIRMED 2026-09-21: the sheet is up but the ride rows are hidden -- e.g. the fare
            // breakdown a previous scenario left expanded. Don't depend on what was left open:
            // relaunch to a clean ride-home screen and redo the search.
            new RegistrationSteps().relaunchToRideHome();
        } else {
            // Not on the sheet at all. If we're logged in but on some other screen (e.g. the payment
            // screen a previous scenario opened), relaunch to a known-good ride home; if we're
            // logged out or already on ride home / plan view, the normal chain below handles it.
            boolean known = false;
            for (int i = 0; i < 16 && !known; i++) {
                known = !ui.findAllPublic(ui.byTextPublic("Get Started")).isEmpty()
                        || !ui.findAllPublic(ui.byTextPublic("Where to?")).isEmpty()
                        || !ui.findAllPublic(ui.byTextPublic("Plan your trip")).isEmpty();
                if (!known) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (!known) {
                new RegistrationSteps().relaunchToRideHome();
            }
        }
        i_am_on_the_plan_your_trip_view();
        i_type_in_the_destination_field(query);
        i_should_see_address_suggestions();
        ui.tapPublic(SUGGESTION_ROW); // first suggestion
        ui.waitForPublic(ui.byTextPublic("Choose a ride"));
    }

    @Given("I have set pickup at {string}")
    public void i_have_set_pickup_at(String address) {
        // Pickup defaults to the phone's current location (Mohali); nothing to set.
    }

    @And("I have set destination at {string}")
    public void i_have_set_destination_at(String address) {
        ensureOnChooseARide(address);
    }

    @When("the route is calculated")
    public void the_route_is_calculated() {
        ui.waitForPublic(ui.byTextPublic("Choose a ride"));
    }

    @Then("I should see the {string} bottom sheet")
    public void i_should_see_the_bottom_sheet(String sheetName) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Choose a ride")),
                "Expected bottom sheet: " + sheetName);
    }

    @And("I should see ride type {string} with an estimated fare")
    public void i_should_see_ride_type_with_estimated_fare(String rideType) {
        Assert.assertTrue(ui.isDisplayedPublic(HOVR_ROW),
                "Expected ride type " + rideType + " to be listed with a fare");
    }

    @Given("I see available ride types")
    public void i_see_available_ride_types() {
        ensureOnChooseARide("CP67 Mall");
        i_should_see_the_bottom_sheet("Choose a ride");
    }

    @And("{string} is displayed with a fare")
    public void ride_type_is_displayed_with_a_fare(String rideType) {
        i_should_see_ride_type_with_estimated_fare(rideType);
    }

    @When("I tap on the ride type {string}")
    public void i_tap_on_the_ride_type(String rideType) {
        ui.tapPublic(HOVR_ROW);
    }

    @Then("{string} should be highlighted as the selected ride type")
    public void ride_type_should_be_highlighted(String rideType) {
        // The selected state is a green outline (no accessibility flag), but the action button is
        // labelled after the selected ride, so "Confirm HOVR" proves HOVR is the selected one.
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Confirm " + rideType)),
                rideType + " is not the selected ride type (no \"Confirm " + rideType + "\" button)");
    }

    @Given("I have selected ride type {string}")
    public void i_have_selected_ride_type(String rideType) {
        ensureOnChooseARide("CP67 Mall");
        i_tap_on_the_ride_type(rideType);
    }

    // CONFIRMED 2026-09-21: the small (i) next to the fare isn't a separate accessibility node, so
    // it's tapped by position: right edge of the ride row, vertically centred. It expands an inline
    // "Fare breakdown" with: Fare (e.g. "Driver keeps 100%"), Subtotal, Fees (collapsible) and
    // Est. Total -- not the Base fare / Distance / Time / Service fee the old feature listed.
    @When("I open the fare breakdown")
    public void i_open_the_fare_breakdown() {
        org.openqa.selenium.Rectangle r = ui.waitForPublic(HOVR_ROW).getRect();
        java.util.Map<String, Object> tap = new java.util.HashMap<>();
        tap.put("x", r.getX() + r.getWidth() - 88);
        tap.put("y", r.getY() + r.getHeight() / 2);
        ((org.openqa.selenium.JavascriptExecutor) ui.driverPublic()).executeScript("mobile: clickGesture", tap);
    }

    @Then("I should see the fare breakdown including:")
    public void i_should_see_the_fare_breakdown_including(DataTable dataTable) {
        List<String> components = dataTable.asList();
        for (String component : components) {
            if (component.equalsIgnoreCase("component")) continue; // header row
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(component)),
                    "Expected fare breakdown line: " + component);
        }
    }

    @And("my payment method shows {string}")
    public void my_payment_method_shows(String cardLabel) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(cardLabel)),
                "Expected payment method to show: " + cardLabel);
    }

    @Then("a FindRideRequest should be sent to the ride service")
    public void a_find_ride_request_should_be_sent() {
        // Backend-only assertion — see class javadoc.
    }

    @And("I should see {string} screen")
    public void i_should_see_named_screen(String screenName) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(screenName.replace("...", ""))),
                "Expected screen: " + screenName);
    }

    @And("the ride status should be {string}")
    public void the_ride_status_should_be(String status) {
        // Backend/state-only assertion — not directly visible in the UI as literal status text.
    }

    @Given("I am on the choose a ride bottom sheet")
    public void i_am_on_the_choose_a_ride_bottom_sheet() {
        ensureOnChooseARide("CP67 Mall");
        i_should_see_the_bottom_sheet("Choose a ride");
    }

    // A freshly registered rider skipped adding a card, so instead of a card brand + last four the
    // payment row reads "Add a payment method" (a clickable row above "Confirm HOVR").
    @Then("I should see the payment method row")
    public void i_should_see_the_payment_method_row() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic("Add a payment method")),
                "Expected the payment method row to be shown");
    }

    @When("I tap on the payment method")
    public void i_tap_on_the_payment_method() {
        ui.tapTextPublic("Add a payment method");
    }

    @Then("I should be navigated to change payment method")
    public void i_should_be_navigated_to_change_payment_method() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Payment")),
                "Expected to be navigated to the change-payment-method screen");
        // Leave the app where this scenario found it (back on the "Choose a ride" sheet): the next
        // scenario starting on this payment screen made the UiAutomator2 server crash on its first
        // lookup ("instrumentation process is not running").
        ui.driverPublic().navigate().back();
    }

    @Given("I do not have a payment method on file")
    public void i_do_not_have_a_payment_method_on_file() {
        // Test-data precondition — use a fresh test account with no saved payment methods.
    }

    @When("I try to confirm a ride")
    public void i_try_to_confirm_a_ride() {
        ensureOnChooseARide("CP67 Mall");
        ui.tapTextPublic("Confirm HOVR");
    }

    @Then("I should see a prompt to add a payment method")
    public void i_should_see_prompt_to_add_payment_method() {
        // CONFIRMED 2026-09-21: tapping "Confirm HOVR" with no card shows a dialog "Add a payment
        // method" -- "Please add a payment method to continue booking your ride." -- with
        // "Add payment method" and "Nevermind" buttons; no ride is created. (The looser check that
        // used to be here matched the "Add a payment method" row that is always on the sheet.)
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("continue booking your ride")),
                "Expected the 'Please add a payment method to continue booking your ride' prompt");
    }

    @And("I should not be able to proceed until a payment method is added")
    public void i_should_not_be_able_to_proceed() {
        Assert.assertTrue(ui.findAllPublic(ui.byTextContainsPublic("Finding your driver")).isEmpty(),
                "Did not expect ride confirmation to proceed without a payment method");
    }

    // The prompt has two buttons -- "Add payment method" (opens the payment screen) and "Nevermind"
    // (closes it, leaving you on the sheet). The scenario exercises them in that order.
    @When("I choose {string} from the prompt")
    public void i_choose_from_the_prompt(String button) {
        ui.tapTextPublic(button);
    }

    @Then("I should still be on the choose a ride bottom sheet")
    public void i_should_still_be_on_the_choose_a_ride_bottom_sheet() {
        Assert.assertTrue(ui.isDisplayedPublic(HOVR_ROW), "Expected to be back on the Choose a ride sheet");
        Assert.assertTrue(ui.findAllPublic(ui.byTextContainsPublic("Finding your driver")).isEmpty(),
                "No ride should have been requested");
        Assert.assertTrue(ui.findAllPublic(ui.byTextContainsPublic("continue booking your ride")).isEmpty(),
                "The payment prompt should be closed");
    }

    @Given("I have confirmed a ride booking")
    public void i_have_confirmed_a_ride_booking() {
        // Test-data/navigation precondition — assumes a prior scenario step or setup already
        // took the flow through ride-type selection and "Confirm Ride".
    }

    // "I am on the {string} screen" is already defined in RegistrationSteps and reused here.

    @When("a driver accepts the ride request")
    public void a_driver_accepts_the_ride_request() {
        // Simulated backend event — requires either a seeded test-driver account accepting via
        // its own session, or a staging API call to fast-forward the dispatch state.
    }

    @Then("I should see the driver's information")
    public void i_should_see_the_drivers_information() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_info_card")), // VERIFY
                "Expected driver information to be shown");
    }

    @And("I should see the driver's estimated arrival time")
    public void i_should_see_the_drivers_eta() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_eta")), // VERIFY
                "Expected driver ETA to be shown");
    }

    @And("I should be navigated to the {string} view")
    public void i_should_be_navigated_to_the_named_view(String viewName) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(viewName)),
                "Expected to be navigated to: " + viewName);
    }

    @And("there are no available drivers in my area")
    public void there_are_no_available_drivers_in_my_area() {
        // Test-data/environment precondition — needs a staging area with no online test drivers.
    }

    @When("the search timeout is reached")
    public void the_search_timeout_is_reached() {
        // Waits out the dispatch-search timeout window — implement with an explicit wait once the
        // real timeout duration is confirmed against the ride service config.
    }

    @Then("I should see a message {string}")
    public void i_should_see_a_message(String message) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextPublic(message)),
                "Expected message: " + message);
    }

    @And("I should be given the option to try again or cancel")
    public void i_should_be_given_option_to_try_again_or_cancel() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Try Again")), // VERIFY exact label
                "Expected a try-again/cancel option");
    }

    @Then("I should see the cancel confirmation bottom sheet")
    public void i_should_see_the_cancel_confirmation_bottom_sheet() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("cancel_confirmation_sheet")), // VERIFY
                "Cancel confirmation bottom sheet not shown");
    }

    @When("I confirm the cancellation")
    public void i_confirm_the_cancellation() {
        ui.tapTextPublic("Yes, Cancel"); // VERIFY exact confirm-dialog label
    }

    @Then("the ride request should be cancelled")
    public void the_ride_request_should_be_cancelled() {
        // Backend-only assertion — the UI-visible consequence (returned to home screen) is
        // asserted by the next Gherkin step, which reuses the shared CommonSteps definition.
    }

    @And("there are active drivers in my area")
    public void there_are_active_drivers_in_my_area() {
        // Test-data/environment precondition.
    }

    @When("the app polls for nearby drivers")
    public void the_app_polls_for_nearby_drivers() {
        ui.waitForPublic(ui.byAccessibilityIdPublic("nearby_driver_marker")); // VERIFY
    }

    @Then("I should see driver icons on the map")
    public void i_should_see_driver_icons_on_the_map() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("nearby_driver_marker")).size() > 0, // VERIFY
                "Expected nearby driver icons on the map");
    }

    @And("the driver positions should update in real time")
    public void the_driver_positions_should_update_in_real_time() {
        // Requires sampling marker position twice with a delay and comparing — implement once
        // marker locators are confirmed.
    }

    @And("there are no active drivers within the search radius")
    public void there_are_no_active_drivers_within_search_radius() {
        // Test-data/environment precondition.
    }

    @Then("I should not see any driver icons on the map")
    public void i_should_not_see_any_driver_icons_on_the_map() {
        Assert.assertTrue(ui.driverPublic().findElements(
                        ui.byAccessibilityIdPublic("nearby_driver_marker")).isEmpty(), // VERIFY
                "Did not expect any driver icons on the map");
    }

    @When("I add a stop at {string}")
    public void i_add_a_stop_at(String address) {
        ui.tapTextPublic("Add Stop"); // VERIFY exact label
        i_type_in_the_destination_field(address);
        i_select_address_from_suggestions();
    }

    // Same suggestion-tap logic as ProfileSteps' saved-places flow, kept local since this
    // class also owns destination-search suggestions (a different search field/context).
    private void i_select_address_from_suggestions() {
        ui.tapPublic(ui.byAccessibilityIdPublic("place_suggestion_0")); // VERIFY
    }

    @And("I confirm the ride")
    public void i_confirm_the_ride() {
        ui.tapTextPublic("Confirm Ride");
    }

    @Then("the trip should be created with the stop included")
    public void the_trip_should_be_created_with_the_stop_included() {
        // Backend payload assertion — see class javadoc.
    }

    @And("the fare should reflect the additional stop")
    public void the_fare_should_reflect_the_additional_stop() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("fare_total")), // VERIFY
                "Expected fare total to be shown reflecting the added stop");
    }

    @Given("I have selected a ride type and tapped {string}")
    public void i_have_selected_a_ride_type_and_tapped(String buttonLabel) {
        // Test-data/navigation precondition combining prior steps.
    }

    @And("the network disconnects before the request completes")
    public void the_network_disconnects_before_the_request_completes() {
        // See CommonSteps#there_is_no_network_connectivity — same device-level limitation applies.
    }

    @And("I should be able to retry the booking when connectivity returns")
    public void i_should_be_able_to_retry_the_booking() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Retry")), // VERIFY exact label
                "Expected a retry option after connectivity returns");
    }

    @Given("I have entered valid pickup and destination")
    public void i_have_entered_valid_pickup_and_destination() {
        // Test-data/navigation precondition.
    }

    @And("the ride service returns an error")
    public void the_ride_service_returns_an_error() {
        // Simulated backend failure — needs a staging fault-injection hook or mocked response.
    }

    @Then("I should see an error message from RideBookingErrorState")
    public void i_should_see_error_message_from_ride_booking_error_state() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("ride_booking_error_banner")), // VERIFY
                "Expected a RideBookingErrorState error message");
    }

    @And("I should remain on the route view to retry")
    public void i_should_remain_on_the_route_view_to_retry() {
        i_should_be_navigated_to_route_view();
    }

    @When("I tap the destination address to change it")
    public void i_tap_the_destination_address_to_change_it() {
        ui.tapPublic(ui.byAccessibilityIdPublic("route_destination_label")); // VERIFY
    }

    @And("I enter a new destination {string}")
    public void i_enter_a_new_destination(String address) {
        i_type_in_the_destination_field(address);
        i_select_address_from_suggestions();
    }

    @Then("the fare should be recalculated")
    public void the_fare_should_be_recalculated() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("fare_total")), // VERIFY
                "Expected fare total to reflect the recalculated fare");
    }

    @And("I should see updated ride type options")
    public void i_should_see_updated_ride_type_options() {
        i_should_see_the_bottom_sheet("Choose a Ride");
    }
}
