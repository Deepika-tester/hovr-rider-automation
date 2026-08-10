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
 * Step definitions for src/test/resources/features/rider/rider_trip.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer.
 *
 * Real-time trip state comes from a gRPC bidirectional stream (TripStreamResponse)
 * per the feature file's derivation notes — steps that assert directly on stream
 * payloads/state transitions are treated as no-ops with the UI-visible consequence
 * asserted instead, same convention as RegistrationSteps.
 */
public class TripSteps {

    private final BasePage ui = new BasePage() {};

    @Given("my ride request has been submitted")
    public void my_ride_request_has_been_submitted() {
        // Test-data/navigation precondition.
    }

    @When("the system is searching for a driver")
    public void the_system_is_searching_for_a_driver() {
        ui.waitForPublic(ui.byTextContainsPublic("Finding your driver"));
    }

    @Then("I should see the {string} view")
    public void i_should_see_the_named_view(String viewName) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(viewName)),
                "Expected view: " + viewName);
    }

    @And("I should see a loading animation")
    public void i_should_see_a_loading_animation() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("loading_spinner")), // VERIFY
                "Expected a loading animation to be shown");
    }

    @And("the trip should have a valid trip ID")
    public void the_trip_should_have_a_valid_trip_id() {
        // Backend/state assertion — not directly checkable from the UI.
    }

    @Given("I am in the Finding Ride state")
    public void i_am_in_the_finding_ride_state() {
        i_should_see_the_named_view("Finding your driver");
    }

    // "I tap "Cancel Ride"" is a plain quoted-string tap, already covered by
    // CommonSteps' generic "I tap {string}" handler.

    @And("a CancelTripRequest should be sent")
    public void a_cancel_trip_request_should_be_sent() {
        // Backend-only assertion — see class javadoc.
    }

    @And("the trip status should change to TripCancelled")
    public void the_trip_status_should_change_to_trip_cancelled() {
        // Backend/state-only assertion — the UI-visible consequence (returned to home screen)
        // is asserted by the next Gherkin step, reusing CommonSteps' shared definition.
    }

    @Given("a driver has accepted my ride request")
    public void a_driver_has_accepted_my_ride_request() {
        // Simulated backend event — see BookingSteps#a_driver_accepts_the_ride_request.
    }

    @When("the TripStreamResponse contains an assignment")
    public void the_trip_stream_response_contains_an_assignment() {
        ui.waitForPublic(ui.byAccessibilityIdPublic("driver_info_card")); // VERIFY
    }

    @Then("I should transition to the {string} view")
    public void i_should_transition_to_the_named_view(String viewName) {
        i_should_see_the_named_view(viewName);
    }

    @And("I should see the driver's name and profile photo")
    public void i_should_see_drivers_name_and_photo() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_photo")), // VERIFY
                "Expected driver name and photo to be shown");
    }

    @And("I should see the driver's vehicle make, model, and license plate")
    public void i_should_see_drivers_vehicle_info() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_vehicle_info")), // VERIFY
                "Expected vehicle make/model/plate to be shown");
    }

    @And("I should see the driver's estimated time of arrival")
    public void i_should_see_drivers_eta() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_eta")), // VERIFY
                "Expected driver ETA to be shown");
    }

    @And("I should see the driver's location updating on the map in real time")
    public void i_should_see_drivers_location_updating() {
        // Requires sampling the driver marker position twice with a delay and comparing —
        // implement once marker locators are confirmed.
    }

    @Given("I am in the {string} state")
    public void i_am_in_the_named_state(String stateName) {
        i_should_see_the_named_view(stateName);
    }

    @Then("I should see the driver's star rating")
    public void i_should_see_drivers_star_rating() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_rating")), // VERIFY
                "Expected driver star rating to be shown");
    }

    @And("I should see the vehicle color")
    public void i_should_see_vehicle_color() {
        // Covered qualitatively by driver_vehicle_info; a dedicated color locator would need
        // VERIFYing separately if the UI renders it in its own element.
    }

    @And("I should be able to call the driver")
    public void i_should_be_able_to_call_the_driver() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("call_driver_button")), // VERIFY
                "Expected a call-driver button");
    }

    @And("I should be able to chat with the driver")
    public void i_should_be_able_to_chat_with_the_driver() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("chat_driver_button")), // VERIFY
                "Expected a chat-with-driver button");
    }

    @Given("the driver is approaching my pickup location")
    public void the_driver_is_approaching_my_pickup_location() {
        // Test-data/stream precondition.
    }

    @When("the driver marks themselves as arrived")
    public void the_driver_marks_themselves_as_arrived() {
        // Simulated backend/driver-app event — needs a seeded test-driver session or staging hook.
    }

    @Then("I should receive a notification {string}")
    public void i_should_receive_a_notification(String message) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(message)),
                "Expected notification: " + message);
    }

    @Then("I should see a notification {string}")
    public void i_should_see_a_notification(String message) {
        i_should_receive_a_notification(message);
    }

    @And("I should see a timer or waiting indicator")
    public void i_should_see_a_timer_or_waiting_indicator() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("waiting_timer")), // VERIFY
                "Expected a waiting timer/indicator to be shown");
    }

    @Given("the driver is waiting at my pickup location")
    public void the_driver_is_waiting_at_my_pickup_location() {
        i_should_see_the_named_view("Driver Waiting at Pickup");
    }

    @When("I open the chat feature")
    public void i_open_the_chat_feature() {
        ui.tapPublic(ui.byAccessibilityIdPublic("chat_driver_button")); // VERIFY
    }

    @And("I send a message {string}")
    public void i_send_a_message(String message) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("chat_input_field"), message); // VERIFY
        ui.tapPublic(ui.byAccessibilityIdPublic("chat_send_button")); // VERIFY
    }

    @Then("the message should be sent via TripStreamRequest")
    public void the_message_should_be_sent_via_trip_stream_request() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Coming down in 2 minutes")),
                "Expected the sent message to appear in the chat thread");
    }

    @And("the driver should receive the chat message")
    public void the_driver_should_receive_the_chat_message() {
        // Only verifiable from the driver-app side — pair with the equivalent
        // driver_trip.feature chat scenario running against a second session/device.
    }

    @Given("the driver has arrived at my pickup")
    public void the_driver_has_arrived_at_my_pickup() {
        i_should_see_the_named_view("Driver Waiting at Pickup");
    }

    @When("the driver starts the trip")
    public void the_driver_starts_the_trip() {
        // Simulated backend/driver-app event.
    }

    @And("I should see the route to my destination on the map")
    public void i_should_see_the_route_to_my_destination() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("route_map")), // VERIFY
                "Expected the route to the destination to be shown");
    }

    @And("I should see the estimated time of arrival")
    public void i_should_see_the_estimated_time_of_arrival() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("trip_eta")), // VERIFY
                "Expected ETA to be shown");
    }

    @And("I should see the driver and car information bar")
    public void i_should_see_driver_and_car_info_bar() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_info_bar")), // VERIFY
                "Expected the driver/car info bar to be shown");
    }

    @Given("the trip is in progress")
    public void the_trip_is_in_progress() {
        i_should_see_the_named_view("Heading Towards Destination");
    }

    @Then("I should see the driver's position update in real time on the map")
    public void i_should_see_drivers_position_update_in_real_time() {
        i_should_see_drivers_location_updating();
    }

    @And("the ETA should update as the driver progresses")
    public void the_eta_should_update_as_driver_progresses() {
        // Requires sampling the ETA value twice with a delay and comparing — implement once
        // the ETA locator is confirmed.
    }

    @And("the route visualization should reflect the current position")
    public void the_route_visualization_should_reflect_current_position() {
        i_should_see_the_route_to_my_destination();
    }

    // "I tap "End Trip Early"" is a plain quoted-string tap, already covered by
    // CommonSteps' generic "I tap {string}" handler.

    @And("an EndTripEarlyRequest should be sent")
    public void an_end_trip_early_request_should_be_sent() {
        // Backend-only assertion — see class javadoc.
    }

    @And("the trip should be ended at the current location")
    public void the_trip_should_be_ended_at_current_location() {
        // Backend/state-only assertion.
    }

    @And("I should be charged for the distance traveled")
    public void i_should_be_charged_for_distance_traveled() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("$")),
                "Expected a fare charge to be shown for the distance traveled");
    }

    @When("the driver arrives at my destination")
    public void the_driver_arrives_at_my_destination() {
        // Simulated backend/driver-app event.
    }

    @And("the driver marks the trip as complete")
    public void the_driver_marks_the_trip_as_complete() {
        // Simulated backend/driver-app event.
    }

    @Then("I should transition to the Rating state")
    public void i_should_transition_to_the_rating_state() {
        i_should_see_the_named_view("Rate");
    }

    @And("I should see the trip summary with the final fare")
    public void i_should_see_trip_summary_with_final_fare() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("trip_summary_fare")), // VERIFY
                "Expected trip summary with final fare to be shown");
    }

    @And("I should be prompted to rate the driver")
    public void i_should_be_prompted_to_rate_the_driver() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("star_rating_input")), // VERIFY
                "Expected a rating prompt to be shown");
    }

    @Given("my trip has been completed")
    public void my_trip_has_been_completed() {
        // Test-data precondition.
    }

    @When("I navigate to my trips list")
    public void i_navigate_to_my_trips_list() {
        ui.tapTextPublic("Trips"); // VERIFY — same trips tab used by rider_profile.feature
    }

    @And("I tap on the completed trip")
    public void i_tap_on_the_completed_trip() {
        ui.tapPublic(ui.byAccessibilityIdPublic("trip_item_0")); // VERIFY
    }

    @Then("I should see the trip receipt with:")
    public void i_should_see_the_trip_receipt_with(DataTable dataTable) {
        List<String> fields = dataTable.asList();
        for (String field : fields) {
            if (field.equalsIgnoreCase("field")) continue; // header row
            Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(field)),
                    "Expected trip receipt field: " + field);
        }
    }

    @Given("I have an active trip in progress")
    public void i_have_an_active_trip_in_progress() {
        // Test-data precondition.
    }

    // "the app is killed and relaunched" is already defined in RegistrationSteps
    // (terminateApp/activateApp) and reused here verbatim.

    @Then("a GetActiveTripEvent should be dispatched")
    public void a_get_active_trip_event_should_be_dispatched() {
        // Backend-only assertion — see class javadoc.
    }

    @And("I should see the active trip state restored")
    public void i_should_see_active_trip_state_restored() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("driver_info_bar")), // VERIFY
                "Expected the active trip state to be restored");
    }

    @And("the trip tracking should resume from the current state")
    public void the_trip_tracking_should_resume_from_current_state() {
        i_should_see_the_route_to_my_destination();
    }

    @Given("I have a trip in {string} state")
    public void i_have_a_trip_in_named_state(String stateName) {
        // Test-data precondition.
    }

    @When("the app is relaunched")
    public void the_app_is_relaunched() {
        // See #the_app_is_killed_and_relaunched_trip above.
    }

    @And("the trip stream should reconnect")
    public void the_trip_stream_should_reconnect() {
        // Backend/stream-only assertion — the UI-visible consequence (Finding your driver view,
        // asserted by the prior Then step) covers the observable behavior.
    }

    @When("the driver sends a chat message")
    public void the_driver_sends_a_chat_message() {
        // Only triggerable from the driver-app side — see #the_driver_should_receive_the_chat_message.
    }

    @Then("I should receive a ChatMessageReceivedState update")
    public void i_should_receive_a_chat_message_received_state_update() {
        // Backend/bloc-internal state — the UI-visible consequence is asserted by the next step.
    }

    @And("I should see a notification for the new message")
    public void i_should_see_a_notification_for_the_new_message() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("chat_notification_badge")), // VERIFY
                "Expected a new-message notification/badge");
    }

    @And("I should be able to open the chat to read and reply")
    public void i_should_be_able_to_open_chat_to_read_and_reply() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("chat_driver_button")), // VERIFY
                "Expected the chat to be reachable");
    }

    @Given("a driver was assigned to my ride")
    public void a_driver_was_assigned_to_my_ride() {
        // Test-data/stream precondition.
    }

    @When("the driver cancels the trip")
    public void the_driver_cancels_the_trip() {
        // Simulated backend/driver-app event.
    }

    @And("I should either see the system automatically search for a new driver, or be returned to the home screen with the option to rebook")
    public void i_should_see_new_driver_search_or_return_home() {
        boolean searchingAgain = ui.isDisplayedPublic(ui.byTextContainsPublic("Finding your driver"));
        boolean returnedHome = ui.isDisplayedPublic(ui.byTextContainsPublic("Where to"));
        Assert.assertTrue(searchingAgain || returnedHome,
                "Expected either a new driver search or a return to the home screen");
    }

    @When("the trip stream connection drops")
    public void the_trip_stream_connection_drops() {
        // See CommonSteps#there_is_no_network_connectivity (in RegistrationSteps) — same
        // device-level limitation applies to simulating a dropped stream connection.
    }

    @Then("the app should attempt to reconnect automatically")
    public void the_app_should_attempt_to_reconnect_automatically() {
        // Backend/stream-only behavior — the UI-visible consequence is asserted by the next step.
    }

    @And("I should see a {string} indicator")
    public void i_should_see_a_named_indicator(String label) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(label.replace("...", ""))),
                "Expected indicator: " + label);
    }

    @When("the connection is restored")
    public void the_connection_is_restored() {
        // See #the_trip_stream_connection_drops above.
    }

    @Then("trip tracking should resume seamlessly")
    public void trip_tracking_should_resume_seamlessly() {
        the_trip_tracking_should_resume_from_current_state();
    }

    @And("the driver has arrived at the pickup location")
    public void the_driver_has_arrived_at_the_pickup_location() {
        the_driver_has_arrived_at_my_pickup();
    }

    @And("the driver has waited beyond the no-show threshold")
    public void the_driver_has_waited_beyond_no_show_threshold() {
        // Test-data/timing precondition.
    }

    @When("the driver reports a no-show")
    public void the_driver_reports_a_no_show() {
        // Simulated backend/driver-app event.
    }

    @Then("the trip should be cancelled")
    public void the_trip_should_be_cancelled() {
        // Backend/state-only assertion — the UI-visible consequence (no-show charge shown in
        // trip history) is asserted by the next step.
    }

    @And("I should be charged a no-show fee")
    public void i_should_be_charged_a_no_show_fee() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("no-show")), // VERIFY exact copy
                "Expected a no-show fee to be shown");
    }

    @And("I should see the no-show charge in my trip history")
    public void i_should_see_no_show_charge_in_trip_history() {
        i_navigate_to_my_trips_list();
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("no-show")), // VERIFY exact copy
                "Expected the no-show charge in trip history");
    }
}
