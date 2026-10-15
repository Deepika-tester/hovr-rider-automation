package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Result;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Map;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Shared wording of the nine API feature files: the Background steps, "the response status is ..." and the generic
 * "I call <RPC> with: <table>" dispatcher. Feature-specific steps live in the other Api*Steps classes.
 *
 * Each scenario's last response is printed into the report (cucumber embeds it), because the status line alone
 * ("expected INVALID_ARGUMENT, got UNKNOWN") is the whole point of most failures here.
 */
public class ApiCommonSteps {

    private final io.cucumber.java.Scenario[] scenario = new io.cucumber.java.Scenario[1];

    @Before(order = 20000)
    public void reset(io.cucumber.java.Scenario s) {
        ApiWorld.reset();
        scenario[0] = s;
    }

    @io.cucumber.java.After(order = 20000)
    public void attachLastResponse(io.cucumber.java.Scenario s) {
        if (ApiWorld.last != null) {
            s.attach(("Last response: " + ApiWorld.last + "\n\n" + abbreviate(ApiWorld.last.raw())).getBytes(), "text/plain", "last API response");
        }
    }

    static String abbreviate(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.length() > 1500 ? raw.substring(0, 1500) + " ...[truncated]" : raw;
    }

    // ---- Background ----

    @Given("the API base URL is configured for the {string} environment")
    public void the_api_base_url_is_configured_for_the_environment(String env) {
        // The spec says "staging"; the only backend this project's app builds talk to is the dev environment
        // (iam / ca.ride / ca.notification .api.dev.ridehovr.com). Listed in the report's spec corrections.
    }

    @Given("I use a gRPC\\/ConnectRPC client with protobuf encoding")
    public void i_use_a_grpc_connectrpc_client() {
        // The suite uses ConnectRPC's JSON encoding of the same unary calls (see ConnectClient).
    }

    @Given("I am authenticated as a rider")
    public void i_am_authenticated_as_a_rider() {
        ApiWorld.me = RiderSession.primary();
    }

    @Given("I am authenticated as a rider with id {string}")
    public void i_am_authenticated_as_a_rider_with_id(String ignoredPlaceholder) {
        ApiWorld.me = RiderSession.primary(); // the feature's "<riderId>" is a placeholder, not a real id
    }

    @Given("I am authenticated as a rider with a default payment method")
    public void i_am_authenticated_as_a_rider_with_a_default_payment_method() {
        ApiWorld.me = RiderSession.primary();
        // Google Pay is the app's own default; make sure the profile says so.
        ApiWorld.me.iam(RiderSession.PROFILE + "SetDefaultPaymentMethod", m("paymentMethodId", "GOOGLEPAY"));
    }

    // ---- status assertions ----

    @Then("the response status is {string}")
    public void the_response_status_is(String expected) {
        Assert.assertNotNull(ApiWorld.last, "No call has been made");
        Assert.assertEquals(ApiWorld.last.status(), expected,
                "Expected status " + expected + " but got " + ApiWorld.last + "  |  body: " + abbreviate(ApiWorld.last.raw()));
    }

    @Then("the response status is {string} or {string}")
    public void the_response_status_is_either(String a, String b) {
        String got = ApiWorld.last.status();
        Assert.assertTrue(got.equals(a) || got.equals(b),
                "Expected status " + a + " or " + b + " but got " + ApiWorld.last + "  |  body: " + abbreviate(ApiWorld.last.raw()));
    }

    // ---- generic "I call X with: <table>" ----

    @When("I call {string} with:")
    public void i_call_with(String rpc, DataTable table) {
        Map<String, String> t = table.asMap(String.class, String.class);
        String method = rpc.contains("/") ? rpc.substring(rpc.indexOf('/') + 1) : rpc;
        switch (method) {
            case "CreateVerificationChallenge" -> ApiWorld.call(rpc,
                    m("phoneNumber", m("e164", t.get("phone")), "userType", "USER_TYPE_" + t.get("userType")));
            case "VerifyVerificationChallenge" -> ApiAuthSteps.verify(rpc, t.get("otpCode"));
            case "UpdateRiderProfile" -> ApiWorld.call(rpc, m("riderId", ApiWorld.me.riderId,
                    "firstName", t.get("firstName"), "lastName", t.get("lastName"),
                    "preferredName", t.get("preferredName"), "email", t.get("email")));
            case "UpdateRiderSafetyPrefs" -> ApiWorld.call(rpc, m("riderId", ApiWorld.me.riderId,
                    "safety", m("pinMode", "true".equals(t.get("verifyRidePin")) ? "PIN_MODE_ON" : "PIN_MODE_OFF",
                            "wheelchairAssistance", "true".equals(t.get("wheelchairAssistance")))));
            case "FindRide" -> ApiWorld.call(rpc, m("pickup", ApiRideSteps.point(t.get("pickup")), "dropoff", ApiRideSteps.point(t.get("dropoff"))));
            case "CreateTicket" -> ApiWorld.call(rpc, m("subject", t.get("subject"), "body", t.get("description"), "type", "question"));
            default -> throw new IllegalArgumentException("No table mapping for " + rpc);
        }
    }
}
