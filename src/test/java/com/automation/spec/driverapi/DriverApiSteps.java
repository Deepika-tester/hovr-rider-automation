package com.automation.spec.driverapi;

import com.automation.spec.api.ConnectClient.Result;
import com.automation.spec.api.Dpop;
import com.automation.spec.rider.PhonePool;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Steps for features/driver-api. Generic "I call <RPC> with: <table>" dispatcher: field names are snake_case in the
 * spec and camelCase on the wire (ConnectRPC JSON), placeholders such as "<driver id>" come from the signed-in driver,
 * and "lat,lng" strings become LatLng messages. The last response is attached to every scenario in the report.
 */
public class DriverApiSteps {

    private static Result last;
    private static DriverApi.Session session;
    private static String tokenOverride;
    private static Dpop dpop;
    private static final Map<String, String> phones = new LinkedHashMap<>();
    private static final Map<String, Object> memo = new LinkedHashMap<>();

    @Before(order = 20000)
    public void reset() {
        last = null;
        session = null;
        tokenOverride = null;
        dpop = new Dpop();
        phones.clear();
        memo.clear();
    }

    @io.cucumber.java.After(order = 20000)
    public void attach(Scenario s) {
        if (last != null) {
            String raw = last.raw() == null ? "" : last.raw();
            s.attach(("Last response: " + last + "\n\n" + (raw.length() > 1500 ? raw.substring(0, 1500) + " ...[truncated]" : raw)).getBytes(),
                    "text/plain", "last API response");
        }
    }

    // ---- Background / session ----

    @Given("the API base URL for the {string} environment")
    public void base_url(String env) {
        // The spec says "dev"; that is what these builds talk to (iam / ca.driver / ca.ride / ca.notification .api.dev.ridehovr.com).
    }

    @Given("the {string} header is {string}")
    public void header_is(String name, String value) {
        // The spec says application/proto; the suite uses Connect's JSON encoding of the same unary calls.
    }

    @Given("an authenticated driver session")
    public void authenticated() {
        session = DriverApi.approvedDriver();
    }

    @Given("an authenticated driver session for an approved driver")
    public void authenticated_approved() {
        session = DriverApi.approvedDriver();
    }

    @Given("no Authorization header")
    public void no_auth() {
        tokenOverride = "";
    }

    @Given("a verification challenge was created for {string}")
    public void challenge_created(String specPhone) {
        String phone = phoneFor(specPhone);
        Result ch = DriverApi.call(DriverApi.AUTH + "CreateVerificationChallenge",
                m("phoneNumber", m("e164", phone), "userType", "USER_TYPE_DRIVER"), null, dpop);
        Assert.assertTrue(ch.ok(), "Could not create the verification challenge: " + ch);
        memo.put("deviceToken", ch.text("deviceToken"));
    }

    @Given("a valid refresh token")
    public void valid_refresh_token() {
        session = DriverApi.approvedDriver();
    }

    // ---- calling ----

    private static String phoneFor(String spec) {
        return phones.computeIfAbsent(spec, k -> k.startsWith("+1") && k.length() == 12 ? "+1" + PhonePool.canadian() : k);
    }

    private static Object value(String key, String v) {
        if (v == null) {
            return "";
        }
        v = v.trim();
        switch (v) {
            case "<driver id>", "<driver id>.":
                return need(session != null ? session.driverId : null, "driver id");
            case "<marketplace id>":
                return need(session != null ? session.marketplaceId : null, "marketplace id");
            case "<uuid>":
                return UUID.randomUUID().toString();
            case "<valid otp>":
                return "111111";
            case "<fcm token>":
                return String.valueOf(memo.getOrDefault("deviceToken", "api-test-" + UUID.randomUUID()));
            case "<refresh token>":
                return need(session != null ? session.refreshToken : null, "refresh token");
            case "<expired refresh token>":
                return "expired.refresh.token";
            case "<version>":
                return "6.2.4+592";
            case "<other driver id>", "<other alert id>":
                return "00000000-0000-0000-0000-000000000001";
            case "<app success deeplink>":
                return "hovr://payment/success";
            default:
                break;
        }
        if (v.matches("\\+1\\d{10}")) {
            return phoneFor(v);
        }
        if (v.matches("-?\\d+\\.\\d+,\\s*-?\\d+\\.\\d+")) {
            String[] p = v.split(",\\s*");
            return m("latitude", Double.parseDouble(p[0]), "longitude", Double.parseDouble(p[1]));
        }
        if (key.equals("latitude") || key.equals("longitude")) {
            return Double.parseDouble(v);
        }
        if (key.matches("page_size|size_bytes|page")) {
            return Long.parseLong(v);
        }
        if (v.equals("true") || v.equals("false")) {
            return Boolean.parseBoolean(v);
        }
        return v;
    }

    private static String need(String v, String what) {
        Assert.assertNotNull(v, "The signed-in driver has no " + what + " in the sign-in responses");
        return v;
    }

    private static boolean dpopCall(String rpc) {
        return rpc.endsWith("/CreateVerificationChallenge") || rpc.endsWith("/VerifyVerificationChallenge")
                || rpc.endsWith("/RefreshAccessToken");
    }

    private static Result send(String rpc, Object body) {
        String token = tokenOverride != null ? tokenOverride : (session == null ? null : session.accessToken);
        Dpop d = dpopCall(rpc) ? (session != null && rpc.endsWith("/RefreshAccessToken") ? session.dpop : dpop) : null;
        last = DriverApi.call(rpc, body, token, d);
        return last;
    }

    @When("I call {string} with:")
    public void call_with(String rpc, DataTable table) {
        Map<String, Object> body = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : table.asMap(String.class, String.class).entrySet()) {
            if (e.getKey().equals("field")) {
                continue;
            }
            Object v = value(e.getKey(), e.getValue());
            String key = DriverApi.camel(e.getKey());
            if (key.equals("phoneNumber")) {
                body.put("phoneNumber", m("e164", v));
            } else {
                body.put(key, v);
            }
        }
        if (rpc.endsWith("/CreateVerificationChallenge") || rpc.endsWith("/VerifyVerificationChallenge")) {
            body.putIfAbsent("userType", "USER_TYPE_DRIVER");
        }
        send(rpc, body);
    }

    @When("I call {string} with an empty request")
    public void call_empty(String rpc) {
        send(rpc, m());
    }

    // ---- assertions ----

    private static void expect(String... codes) {
        Assert.assertNotNull(last, "No call has been made");
        for (String c : codes) {
            if (last.status().equalsIgnoreCase(c)) {
                return;
            }
        }
        Assert.fail("Expected response code " + String.join(" or ", codes) + " but got " + last
                + "  |  body: " + (last.raw() == null ? "" : last.raw().substring(0, Math.min(400, last.raw().length()))));
    }

    @Then("the response code is {string}")
    public void code_is(String code) {
        expect(code);
    }

    @Then("the response code is {string} or {string}")
    public void code_is_either(String a, String b) {
        expect(a, b);
    }

    @Then("an OTP is sent to {string}")
    public void otp_sent(String phone) {
        expect("ok");
    }

    @Then("^the response contains (.*)$")
    public void contains(String what) {
        expect("ok");
        String w = what.toLowerCase();
        String raw = last.raw() == null ? "" : last.raw();
        if (w.contains("access token")) {
            Assert.assertNotNull(last.text("authToken"), "No access token in: " + raw);
        } else if (w.contains("refresh token")) {
            Assert.assertNotNull(last.text("refreshToken"), "No refresh token in: " + raw);
        } else if (w.contains("https")) {
            Assert.assertTrue(raw.contains("https://"), "No https URL in: " + raw);
        } else if (w.contains("version")) {
            Assert.assertTrue(raw.toLowerCase().contains("version"), "No version in: " + raw);
        } else {
            Assert.assertTrue(raw.length() > 2 && !raw.replace(" ", "").equals("{}"), "Empty response, expected " + what);
        }
    }

    @Then("^the response contains a new access token$")
    public void new_access_token() {
        expect("ok");
        Assert.assertNotNull(last.text("authToken"), "No access token in: " + last.raw());
    }
}
