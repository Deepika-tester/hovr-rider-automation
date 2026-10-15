package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Result;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static com.automation.spec.api.ConnectClient.m;

/** Steps for features/api/02_rider_profile_service.feature and 03_payment_api.feature. */
public class ApiProfileSteps {

    private static final String PROFILE = RiderSession.PROFILE;

    private static String riderId() {
        return ApiWorld.me.riderId;
    }

    // ---- profile ----

    @When("I call {string} with riderId {string}")
    public void i_call_with_rider_id(String rpc, String ignoredPlaceholder) {
        // The rider is identified by the access token; the feature's "<riderId>" is a placeholder.
        ApiWorld.call(rpc, m());
    }

    @Then("the profile contains first name, last name, phone and email")
    public void the_profile_contains_first_name_last_name_phone_and_email() {
        JsonNode r = ApiWorld.last.at("rider");
        Assert.assertNotNull(r, "No rider in the response: " + ApiWorld.last.raw());
        for (String f : new String[] {"firstName", "lastName", "email"}) {
            Assert.assertTrue(r.hasNonNull(f) && !r.get(f).asText().isBlank(), "Profile has no " + f + ": " + r);
        }
        Assert.assertTrue(r.at("/phoneNumber/e164").asText("").startsWith("+"), "Profile has no phone number: " + r);
    }

    @When("I call {string} with another rider's id")
    public void i_call_with_another_riders_id(String rpc) {
        RiderSession other = RiderSession.second();
        ApiWorld.call(rpc, m("riderId", other.riderId));
        ApiWorld.memo.put("otherRiderId", other.riderId);
    }

    @When("I call {string} with email {string}")
    public void i_call_with_email(String rpc, String email) {
        ApiWorld.call(rpc, m("riderId", riderId(), "email", email));
    }

    @When("I call {string} with firstName {string}")
    public void i_call_with_first_name(String rpc, String firstName) {
        ApiWorld.call(rpc, m("riderId", riderId(), "firstName", firstName));
    }

    @Then("GetRiderProfile returns the updated values")
    public void get_rider_profile_returns_the_updated_values() {
        JsonNode r = ApiWorld.me.profile();
        Assert.assertEquals(r.path("firstName").asText(), "Jane");
        Assert.assertEquals(r.path("lastName").asText(), "Doe");
        Assert.assertEquals(r.path("preferredName").asText(), "JD");
        Assert.assertEquals(r.path("email").asText(), "jane@example.com");
    }

    @When("I call {string} with language {string}")
    public void i_call_with_language(String rpc, String language) {
        ApiWorld.call(rpc, m("riderId", riderId(), "prefs", m("language", language)));
    }

    // ---- saved places ----

    private static Object latLng() {
        return m("latitude", ApiMarket.lat(), "longitude", ApiMarket.lng());
    }

    @When("I call {string} with name {string} and a valid address with lat\\/lng")
    public void i_call_create_saved_place(String rpc, String name) {
        ApiWorld.call(rpc, m("savedPlace", m("label", name, "address", ApiMarket.address(), "latLng", latLng())));
        if (ApiWorld.last.ok()) {
            ApiWorld.memo.put("placeId", ApiWorld.last.text("savedPlace.id"));
        }
    }

    @When("I call {string} with name {string} and no lat\\/lng")
    public void i_call_create_saved_place_without_coordinates(String rpc, String name) {
        ApiWorld.call(rpc, m("savedPlace", m("label", name, "address", "Somewhere, Mohali")));
        if (ApiWorld.last.ok()) {
            ApiWorld.memo.put("cleanupPlaceId", ApiWorld.last.text("savedPlace.id"));
        }
    }

    @Then("the response contains a saved place id")
    public void the_response_contains_a_saved_place_id() {
        Assert.assertTrue(ApiWorld.last.ok(), "CreateSavedPlace failed: " + ApiWorld.last);
        Assert.assertNotNull(ApiWorld.last.text("savedPlace.id"), "No saved place id in: " + ApiWorld.last.raw());
    }

    @When("I call {string} with that id")
    public void i_call_with_that_id(String rpc) {
        String id = (String) ApiWorld.memo.get("placeId");
        Assert.assertNotNull(id, "No saved place was created earlier in the scenario");
        ApiWorld.call(rpc, m("savedPlaceId", id));
    }

    @Then("the place name is {string}")
    public void the_place_name_is(String name) {
        Assert.assertEquals(ApiWorld.last.text("savedPlace.label"), name,
                "GetSavedPlace returned: " + ApiWorld.last.raw());
    }

    @When("I call {string} with name {string}")
    public void i_call_update_saved_place_with_name(String rpc, String name) {
        String id = (String) ApiWorld.memo.get("placeId");
        // Update takes a flat body (savedPlaceId, label, address, position); the nested form used by Create fails with a
        // DynamoDB ConditionalCheckFailedException, and a "latLng" field is silently ignored.
        ApiWorld.call(rpc, m("savedPlaceId", id, "label", name, "address", ApiMarket.address(), "position", latLng()));
    }

    private List<String> placeLabels() {
        Result r = ApiWorld.me.iam(PROFILE + "ListSavedPlaces", m());
        List<String> labels = new ArrayList<>();
        JsonNode arr = r.at("savedPlaces");
        if (arr != null) {
            arr.forEach(p -> labels.add(p.path("label").asText("")));
        }
        return labels;
    }

    @Then("ListSavedPlaces contains {string}")
    public void list_saved_places_contains(String name) {
        Assert.assertTrue(placeLabels().contains(name), "ListSavedPlaces has: " + placeLabels());
    }

    @Then("ListSavedPlaces does not contain {string}")
    public void list_saved_places_does_not_contain(String name) {
        Assert.assertFalse(placeLabels().contains(name), "ListSavedPlaces still has: " + placeLabels());
    }

    @When("I call {string} with id {string}")
    public void i_call_with_id(String rpc, String id) {
        ApiWorld.call(rpc, m("savedPlaceId", id));
    }

    @Then("at most one method is marked default")
    public void at_most_one_method_is_marked_default() {
        Assert.assertTrue(ApiWorld.last.ok(), "ListPaymentMethods failed: " + ApiWorld.last);
        int defaults = 0;
        JsonNode arr = ApiWorld.last.at("paymentMethods");
        if (arr != null) {
            for (JsonNode p : arr) {
                if (p.path("isDefault").asBoolean(false) || p.path("default").asBoolean(false)) {
                    defaults++;
                }
            }
        }
        Assert.assertTrue(defaults <= 1, "More than one default payment method: " + ApiWorld.last.raw());
    }

    // ---- payment ----

    @Then("the response contains a session id and session data")
    public void the_response_contains_a_session_id_and_session_data() {
        Assert.assertTrue(ApiWorld.last.ok(), "CreateAdyenPaymentSession failed: " + ApiWorld.last);
        Assert.assertNotNull(ApiWorld.last.text("sessionId"), "No sessionId");
        Assert.assertNotNull(ApiWorld.last.text("sessionData"), "No sessionData");
    }

    @Then("the call completes within {int} seconds")
    public void the_call_completes_within_seconds(int seconds) {
        Assert.assertTrue(ApiWorld.last.millis() <= seconds * 1000L, "The call took " + ApiWorld.last.millis() + " ms");
    }

    @Given("the rider has no saved cards")
    public void the_rider_has_no_saved_cards() {
        ApiWorld.me = RiderSession.second(); // the second rider never has a card added
    }

    @Then("the response contains an empty list")
    public void the_response_contains_an_empty_list() {
        Assert.assertTrue(ApiWorld.last.ok(), "ListPaymentMethods failed: " + ApiWorld.last);
        JsonNode arr = ApiWorld.last.at("paymentMethods");
        Assert.assertTrue(arr == null || arr.isEmpty(), "Expected no payment methods: " + ApiWorld.last.raw());
    }

    @When("I call {string} with paymentMethodId {string}")
    public void i_call_with_payment_method_id(String rpc, String id) {
        ApiWorld.call(rpc, m("paymentMethodId", id));
    }
}
