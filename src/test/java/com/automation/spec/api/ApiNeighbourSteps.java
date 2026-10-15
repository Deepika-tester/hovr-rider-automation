package com.automation.spec.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import static com.automation.spec.api.ConnectClient.m;

/** Steps for features/api/06_neighbour_rider_service.feature (Co-Commute). Booking/posting scenarios are @blocked. */
public class ApiNeighbourSteps {

    private static final Object MOHALI = ApiMarket.pickup();
    private static final Object CHANDIGARH = ApiMarket.dropoff();

    @When("I call {string} with my location")
    public void i_call_search_neighbour_trips_with_my_location(String rpc) {
        ApiWorld.call(rpc, m("pickup", MOHALI));
    }

    @Then("each journey has leave time, seats left and price")
    public void each_journey_has_leave_time_seats_left_and_price() {
        Assert.assertTrue(ApiWorld.last.ok(), "SearchNeighbourTrips failed: " + ApiWorld.last);
        JsonNode trips = ApiWorld.last.at("trips");
        Assert.assertTrue(trips != null && trips.size() > 0, "No journeys near the test location: " + ApiWorld.last.raw());
        for (JsonNode t : trips) {
            java.util.List<String> keys = new java.util.ArrayList<>();
            t.fieldNames().forEachRemaining(keys::add);
            Assert.assertTrue(t.has("departAt"), "A journey has no leave time; fields: " + keys);
            boolean seats = keys.stream().anyMatch(k -> k.toLowerCase().contains("seat"));
            boolean price = keys.stream().anyMatch(k -> k.toLowerCase().contains("price") || k.toLowerCase().contains("fare"));
            Assert.assertTrue(seats && price, "A journey has leave time but no seats-left/price fields; fields: " + keys);
        }
    }

    @Given("a neighbour journey exists")
    public void a_neighbour_journey_exists() {
        ApiWorld.me = RiderSession.primary();
        var r = ApiWorld.call("NeighbourRiderService/SearchNeighbourTrips", m("pickup", MOHALI));
        Assert.assertTrue(r.ok() && r.at("trips") != null && r.at("trips").size() > 0, "No journey exists near Mohali right now: " + r);
        ApiWorld.memo.put("neighbourTripId", r.text("trips"));
        ApiWorld.memo.put("neighbourTripId", r.at("trips").get(0).get("neighbourTripId").asText());
    }

    @When("I call {string} with its id")
    public void i_call_get_neighbour_trip_with_its_id(String rpc) {
        ApiWorld.call(rpc, m("neighbourTripId", ApiWorld.memo.get("neighbourTripId")));
    }

    @Then("the response contains driver, route and seats")
    public void the_response_contains_driver_route_and_seats() {
        JsonNode t = ApiWorld.last.at("trip");
        Assert.assertNotNull(t, "No trip in the response: " + ApiWorld.last.raw());
        java.util.List<String> keys = new java.util.ArrayList<>();
        t.fieldNames().forEachRemaining(keys::add);
        Assert.assertTrue(t.has("driver") && t.has("origin") && t.has("destination"), "Missing driver/route; fields: " + keys);
        Assert.assertTrue(keys.stream().anyMatch(k -> k.toLowerCase().contains("seat")), "No seats field; fields: " + keys);
    }

    @Then("the response contains the held fee amount")
    public void the_response_contains_the_held_fee_amount() {
        Assert.assertTrue(ApiWorld.last.ok(), "GetNeighbourPlatformFee failed: " + ApiWorld.last);
        Assert.assertNotNull(ApiWorld.last.at("platformFee"), "No platformFee in: " + ApiWorld.last.raw());
    }

    @When("I call {string} without leave time")
    public void i_call_post_neighbour_request_without_leave_time(String rpc) {
        ApiWorld.call(rpc, m("pickup", MOHALI, "dropoff", CHANDIGARH, "seats", 1));
    }
}
