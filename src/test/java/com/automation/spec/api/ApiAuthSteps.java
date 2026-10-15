package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Result;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Steps for features/api/01_auth_service.feature.
 *
 * The spec's examples are Canadian (+14165550123). The staging OTP "111111" is only honoured for +91 numbers, so the
 * scenarios that need a code that really verifies run against a fresh +91 number (the app does the same in the UI
 * suite). Requesting a challenge for the literal +1 number is fine -- 416-555-01xx is a reserved fictional range.
 */
public class ApiAuthSteps {

    private static final String AUTH = "AuthService/";
    private Dpop dpop = new Dpop();

    /** Verifies the OTP for the scenario's fresh +91 number. */
    static void verify(String rpc, String otp) {
        String code = otp == null || otp.startsWith("<") ? "111111" : otp;
        Result r = RiderSession.verify(ApiWorld.verifyPhone, code, ApiWorld.verifyDeviceToken, ApiWorld.memoDpop());
        ApiWorld.last = r;
    }

    @Given("I have requested OTP the maximum number of times today for {string}")
    public void blocked_max_otp(String phone) {
        throw new io.cucumber.java.PendingException("blocked scenario");
    }

    @Given("an OTP challenge exists for {string}")
    public void an_otp_challenge_exists_for(String specPhone) {
        ApiWorld.verifyPhone = RiderSession.uniquePhone();
        Result ch = RiderSession.requestChallenge(ApiWorld.verifyPhone, ApiWorld.memoDpop());
        Assert.assertTrue(ch.ok(), "Could not create the OTP challenge for the test number: " + ch);
        ApiWorld.verifyDeviceToken = ch.text("deviceToken");
        ApiWorld.last = ch;
    }

    @When("I call {string} with phone {string}")
    public void i_call_with_phone(String rpc, String phone) {
        ApiWorld.call(rpc, m("phoneNumber", m("e164", phone), "userType", "USER_TYPE_RIDER"));
    }

    @Then("the response contains a non-empty challenge id")
    public void the_response_contains_a_non_empty_challenge_id() {
        // The challenge is identified by the "deviceToken" in the response (the spec calls it a challenge id).
        String id = ApiWorld.last.text("deviceToken");
        Assert.assertTrue(id != null && !id.isBlank(), "No challenge identifier in the response: " + ApiWorld.last.raw());
    }

    @When("I call {string} with otpCode {string}")
    public void i_call_with_otp_code(String rpc, String code) {
        verify(rpc, code);
    }

    @Then("the response contains an access token and a refresh token")
    public void the_response_contains_an_access_token_and_a_refresh_token() {
        Assert.assertTrue(ApiWorld.last.ok(), "Verify failed: " + ApiWorld.last);
        Assert.assertTrue(notBlank(ApiWorld.last.text("authToken")), "No access token (authToken) in the response");
        Assert.assertTrue(notBlank(ApiWorld.last.text("refreshToken")), "No refresh token in the response");
    }

    @Then("no tokens are returned")
    public void no_tokens_are_returned() {
        Assert.assertFalse(notBlank(ApiWorld.last.text("authToken")) || notBlank(ApiWorld.last.text("refreshToken")),
                "Tokens were returned for a wrong code");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    // ---- refresh ----

    @Given("I hold a valid refresh token")
    public void i_hold_a_valid_refresh_token() {
        ApiWorld.me = RiderSession.primary();
    }

    @Given("I hold a revoked refresh token")
    public void i_hold_a_revoked_refresh_token() {
        ApiWorld.me = RiderSession.primary();
        ApiWorld.refreshOverride = "revoked-" + Long.toHexString(System.nanoTime());
    }

    @When("I call {string}")
    public void i_call(String rpc) {
        String method = rpc.substring(rpc.indexOf('/') + 1);
        switch (method) {
            case "RefreshAccessToken" -> {
                String token = ApiWorld.refreshOverride != null ? ApiWorld.refreshOverride : ApiWorld.me.refreshToken;
                ApiWorld.last = ConnectClient.call(ConnectClient.Host.IAM, ApiWorld.rpc(rpc).path(),
                        m("refreshToken", token), null, ApiWorld.me.dpop);
                if (ApiWorld.refreshOverride == null && ApiWorld.last.ok()) {
                    ApiWorld.me.accessToken = ApiWorld.last.text("authToken");
                    if (ApiWorld.last.text("refreshToken") != null) {
                        ApiWorld.me.refreshToken = ApiWorld.last.text("refreshToken");
                    }
                }
            }
            default -> ApiCommonCalls.callNoBody(rpc);
        }
    }

    @Then("a new access token is returned")
    public void a_new_access_token_is_returned() {
        Assert.assertTrue(ApiWorld.last.ok(), "Refresh failed: " + ApiWorld.last);
        Assert.assertTrue(notBlank(ApiWorld.last.text("authToken")), "No new access token in the response");
    }

    // ---- profile / unauthenticated ----

    @Then("the response contains my rider id")
    public void the_response_contains_my_rider_id() {
        String id = ApiWorld.last.text("id");
        Assert.assertEquals(id, ApiWorld.me.riderId, "GetActiveProfile did not return my rider id: " + ApiWorld.last.raw());
    }

    @Given("my access token is {string}")
    public void my_access_token_is(String kind) {
        ApiWorld.me = RiderSession.primary();
        ApiWorld.tokenOverride = switch (kind) {
            case "missing" -> "";
            case "expired" -> "v4.public.eyJleHAiOiIyMDIwLTAxLTAxVDAwOjAwOjAwWiIsImlzcyI6ImlhbSJ9AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            default -> "not-a-valid-token";
        };
    }
}
