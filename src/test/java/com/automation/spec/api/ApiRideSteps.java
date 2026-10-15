package com.automation.spec.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Steps for features/api/04_ride_service.feature and 05_trip_service.feature.
 *
 * Anything that would create a trip or a scheduled ride is never called: those scenarios are @blocked until the
 * ride-confirming scenarios are approved (they create real ride requests on the shared dev backend). Invalid-time
 * scheduling is safe -- it is rejected before anything is created.
 */
public class ApiRideSteps {

    private static final Map<String, Object> MOHALI = ApiMarket.pickup();      // the test pickup (Mohali or Toronto)
    private static final Map<String, Object> CHANDIGARH = ApiMarket.dropoff(); // the test drop-off

    /** "43.6453,-79.3806" -> {"latLng": {...}} */
    static Map<String, Object> point(String latLng) {
        String[] p = latLng.split(",");
        return m("latLng", m("latitude", Double.parseDouble(p[0].trim()), "longitude", Double.parseDouble(p[1].trim())));
    }

    // ---- ride service ----

    @Then("the response contains at least one ride service")
    public void the_response_contains_at_least_one_ride_service() {
        Assert.assertTrue(ApiWorld.last.ok(), "FindRide failed: " + ApiWorld.last);
        JsonNode services = ApiWorld.last.at("estimate.serviceResults");
        Assert.assertTrue(services != null && services.size() >= 1, "No ride service in: " + abbreviated());
    }

    @Then("each ride service has a fare estimate and pickup ETA")
    public void each_ride_service_has_a_fare_estimate_and_pickup_eta() {
        for (JsonNode s : ApiWorld.last.at("estimate.serviceResults")) {
            Assert.assertTrue(s.has("estimatedTotal"), "A ride service has no fare estimate: " + s.fieldNames());
            List<String> keys = new ArrayList<>();
            s.fieldNames().forEachRemaining(keys::add);
            boolean eta = keys.stream().anyMatch(k -> k.toLowerCase().contains("eta") || k.toLowerCase().contains("pickup"));
            Assert.assertTrue(eta, "No pickup ETA on the ride service; its fields are " + keys);
        }
    }

    @When("I call {string} with pickup outside the service area")
    public void i_call_find_ride_with_pickup_outside_the_service_area(String rpc) {
        ApiWorld.call(rpc, m("pickup", m("latLng", m("latitude", 10.0, "longitude", 65.0)), "dropoff", CHANDIGARH));
    }

    @When("I call {string} with a dropoff unreachable by road")
    public void i_call_find_ride_with_a_dropoff_unreachable_by_road(String rpc) {
        ApiWorld.call(rpc, m("pickup", MOHALI, "dropoff", m("latLng", m("latitude", 10.0, "longitude", 65.0))));
    }

    @When("I call {string} with {int} intermediate stops")
    public void i_call_find_ride_with_intermediate_stops(String rpc, int n) {
        List<Object> stops = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            stops.add(m("latLng", m("latitude", ApiMarket.lat() + 0.004 + i * 0.003, "longitude", ApiMarket.lng() + 0.01 + i * 0.01)));
        }
        ApiWorld.call(rpc, m("pickup", MOHALI, "dropoff", CHANDIGARH, "stops", stops));
        ApiWorld.memo.put("stopCount", n);
    }

    @Then("the fare estimate includes all stops")
    public void the_fare_estimate_includes_all_stops() {
        Assert.assertTrue(ApiWorld.last.ok(), "FindRide failed: " + ApiWorld.last);
        JsonNode stops = ApiWorld.last.at("estimate.stops");
        Assert.assertEquals(stops == null ? 0 : stops.size(), ApiWorld.memo.get("stopCount"), "Stops echoed in the estimate: " + stops);
        Assert.assertTrue(ApiWorld.last.at("estimate.serviceResults").size() >= 1, "No fare in the estimate");
    }

    @When("I call {string} with a pickup time {}")
    public void i_call_schedule_ride_with_a_pickup_time(String rpc, String when) {
        Instant t = switch (when) {
            case "5 minutes from now" -> Instant.now().plus(5, ChronoUnit.MINUTES);
            case "8 days from now" -> Instant.now().plus(8, ChronoUnit.DAYS);
            case "1 hour in the past" -> Instant.now().minus(1, ChronoUnit.HOURS);
            default -> throw new IllegalArgumentException("Only invalid times are exercised here, got: " + when);
        };
        // A real quote is needed first: the request is rejected for the missing estimate before it looks at the time.
        var quote = ApiWorld.call("RideService/FindRide", m("pickup", MOHALI, "dropoff", CHANDIGARH));
        Assert.assertTrue(quote.ok(), "Could not get a quote to schedule against: " + quote);
        ApiWorld.call(rpc, m("estimateId", quote.text("estimate.estimateId"), "selectedServiceId", "hovr",
                "scheduledPickupTime", t.truncatedTo(ChronoUnit.SECONDS).toString()));
    }

    @When("I call {string} with my current location")
    public void i_call_find_nearby_drivers(String rpc) {
        ApiWorld.call(rpc, m("location", MOHALI));
    }

    private String abbreviated() {
        return ApiCommonSteps.abbreviate(ApiWorld.last.raw());
    }

    // ---- trip service ----

    @Given("a ride quote exists for a valid route")
    public void a_ride_quote_exists_for_a_valid_route() {
        var quote = ApiWorld.call("RideService/FindRide", m("pickup", MOHALI, "dropoff", CHANDIGARH));
        Assert.assertTrue(quote.ok(), "Could not get a ride quote: " + quote);
        ApiWorld.memo.put("estimateId", quote.text("estimate.estimateId"));
    }

    @Given("I have no active trip")
    public void i_have_no_active_trip() {
        // The shared rider never books a ride in this suite.
    }

    @Then("the response contains no trip or status {string}")
    public void the_response_contains_no_trip_or_status(String status) {
        boolean noTrip = ApiWorld.last.ok() && (ApiWorld.last.at("trip") == null || ApiWorld.last.at("trip").isEmpty());
        Assert.assertTrue(noTrip || status.equals(ApiWorld.last.status()), "Unexpected: " + ApiWorld.last + " " + abbreviated());
    }
}
