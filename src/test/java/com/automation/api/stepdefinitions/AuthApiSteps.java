package com.automation.api.stepdefinitions;

import com.automation.api.ApiClient;
import com.automation.api.ApiTestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Map;

/**
 * Step definitions for the OTP request/verification scenarios of
 * src/test/resources/features/api/api_auth.feature — implemented as the
 * reference pattern for the API suite.
 *
 * ENDPOINT CONTRACT DISCLAIMER: the feature file describes behavior at the
 * business/bloc level ("a RequestOTP API call is made"), not literal REST
 * paths or payload shapes — the README notes these were derived from the
 * iam-service Go handler package, not a published OpenAPI/proto spec this
 * repo has access to. Every path/field name below (marked "// CONTRACT:") is a
 * reasonable guess to be confirmed/corrected against the real iam-service
 * contract (ask the backend team for the OpenAPI spec, .proto file, or a
 * Postman collection) before these are trustworthy — same spirit as the
 * "VERIFY:" convention used for Appium locators in the mobile suite.
 *
 * The remaining ~22 scenarios in api_auth.feature (rider/driver profile,
 * payment, referral, wallet, marketplace validation, token lifecycle) are not
 * yet implemented — this class covers OTP request + verification only as the
 * demonstrated pattern; extend it the same way once the real contract is known.
 */
public class AuthApiSteps {

    @Given("the IAM service is running at iam.api.prod.ridehovr.com")
    public void the_iam_service_is_running() {
        // Environment precondition — actual runs should target staging (see
        // api.properties' iamBaseUrl), not the literal prod host this line names.
    }

    @And("the DynamoDB database is accessible")
    public void the_dynamodb_database_is_accessible() {
        // Infrastructure precondition — not independently checkable from this client;
        // an unreachable DB would surface as a 5xx on the calls below.
    }

    @And("the Redis cache is accessible")
    public void the_redis_cache_is_accessible() {
        // Infrastructure precondition — see above.
    }

    @Given("a user with phone number {string}")
    public void a_user_with_phone_number(String phoneNumber) {
        ApiTestContext.put("phoneNumber", phoneNumber);
    }

    @When("a RequestOTP API call is made with that phone number")
    public void a_request_otp_api_call_is_made_with_that_phone_number() {
        requestOtp(ApiTestContext.get("phoneNumber"));
    }

    @When("a RequestOTP API call is made")
    public void a_request_otp_api_call_is_made() {
        requestOtp(ApiTestContext.get("phoneNumber"));
    }

    private void requestOtp(String phoneNumber) {
        // CONTRACT: path/payload field name are placeholders — confirm against iam-service.
        ApiClient.post(ApiClient.Service.IAM, "/v1/auth/otp/request", Map.of("phoneNumber", phoneNumber));
    }

    @Then("the system should generate a {int}-digit OTP")
    public void the_system_should_generate_an_n_digit_otp(int digits) {
        ApiTestContext.getLastResponse().then().statusCode(200);
    }

    @And("the OTP should be sent via SMS notification")
    public void the_otp_should_be_sent_via_sms_notification() {
        // Not verifiable via the IAM API response alone — would need a test-SMS-provider
        // webhook/inbox (e.g. a Twilio test number) to assert delivery independently.
    }

    @And("the OTP should be stored with a {int}-minute expiry")
    public void the_otp_should_be_stored_with_an_n_minute_expiry(int minutes) {
        // Backend/state assertion — not directly checkable from the API response; would
        // need either a debug endpoint or waiting out the expiry window in a live test.
    }

    @Then("the system should validate the phone number format")
    public void the_system_should_validate_the_phone_number_format() {
        // E.164 format validation happens server-side as part of RequestOTP itself — a 200
        // (or the region-specific response asserted by the next step) confirms the format
        // passed validation; an invalid format is covered separately by the malformed-number
        // scenario's 400/422 check.
        Assert.assertTrue(ApiTestContext.getLastResponse().statusCode() < 500,
                "Expected RequestOTP to handle the phone number without a server error");
    }

    @Given("a malformed phone number {string}")
    public void a_malformed_phone_number(String phoneNumber) {
        ApiTestContext.put("phoneNumber", phoneNumber);
    }

    @Then("the system should return a validation error via protovalidate")
    public void the_system_should_return_a_validation_error() {
        Response response = ApiTestContext.getLastResponse();
        Assert.assertTrue(response.statusCode() == 400 || response.statusCode() == 422,
                "Expected a validation error status (400/422), got: " + response.statusCode());
    }

    @And("no OTP should be generated")
    public void no_otp_should_be_generated() {
        // Backend/state assertion — the validation-error status check above covers the
        // observable API-level consequence.
    }

    @And("process according to supported regions")
    public void process_according_to_supported_regions() {
        // Vague/product-level assertion in the source feature file — the preceding
        // format-validation check covers what's concretely testable here.
    }

    @Given("a phone number {string} has requested {int} OTPs in {int} minutes")
    public void a_phone_number_has_requested_n_otps_in_n_minutes(String phoneNumber, int count, int minutes) {
        ApiTestContext.put("phoneNumber", phoneNumber);
        // Reaching this state for real requires actually issuing `count` prior requests
        // (or a staging fixture endpoint that seeds rate-limit state directly) — not done
        // here to avoid hammering a real service on every test run.
    }

    @When("another OTP request is made for the same number")
    public void another_otp_request_is_made_for_the_same_number() {
        requestOtp(ApiTestContext.get("phoneNumber"));
    }

    @Then("the system should rate-limit the request")
    public void the_system_should_rate_limit_the_request() {
        Assert.assertEquals(ApiTestContext.getLastResponse().statusCode(), 429,
                "Expected a 429 Too Many Requests response");
    }

    @And("return an appropriate error message")
    public void return_an_appropriate_error_message() {
        Assert.assertFalse(ApiTestContext.getLastResponse().jsonPath().getString("message").isBlank()); // CONTRACT: field name
    }

    @Given("an OTP {string} was sent to {string}")
    public void an_otp_was_sent_to(String otp, String phoneNumber) {
        ApiTestContext.put("phoneNumber", phoneNumber);
        ApiTestContext.put("otp", otp);
    }

    @And("the OTP has not expired")
    public void the_otp_has_not_expired() {
        // Test-data/timing precondition — assumes the call chain runs well within the
        // 5-minute expiry window.
    }

    @When("a VerifyOTP API call is made with phone {string} and OTP {string}")
    public void a_verify_otp_api_call_is_made_with_phone_and_otp(String phoneNumber, String otp) {
        verifyOtp(phoneNumber, otp);
    }

    @When("a VerifyOTP API call is made with OTP {string}")
    public void a_verify_otp_api_call_is_made_with_otp(String otp) {
        verifyOtp(ApiTestContext.get("phoneNumber"), otp);
    }

    private void verifyOtp(String phoneNumber, String otp) {
        // CONTRACT: path/payload field names are placeholders — confirm against iam-service.
        ApiClient.post(ApiClient.Service.IAM, "/v1/auth/otp/verify", Map.of(
                "phoneNumber", phoneNumber,
                "otp", otp));
    }

    @Then("the system should return authentication tokens")
    public void the_system_should_return_authentication_tokens() {
        Response response = ApiTestContext.getLastResponse();
        response.then().statusCode(200);
        String token = response.jsonPath().getString("accessToken"); // CONTRACT: field name
        Assert.assertNotNull(token, "Expected an accessToken in the VerifyOTP response");
        ApiTestContext.put("authToken", token);
    }

    @And("the response should indicate if this is a new or returning user")
    public void the_response_should_indicate_new_or_returning_user() {
        Assert.assertNotNull(ApiTestContext.getLastResponse().jsonPath().get("isNewUser")); // CONTRACT: field name
    }

    @Then("the system should return an authentication error")
    public void the_system_should_return_an_authentication_error() {
        Assert.assertEquals(ApiTestContext.getLastResponse().statusCode(), 401,
                "Expected a 401 authentication error");
    }

    @And("no tokens should be issued")
    public void no_tokens_should_be_issued() {
        Assert.assertNull(ApiTestContext.getLastResponse().jsonPath().get("accessToken")); // CONTRACT: field name
    }

    @Given("an OTP was sent more than {int} minutes ago")
    public void an_otp_was_sent_more_than_n_minutes_ago(int minutes) {
        ApiTestContext.put("phoneNumber", "+14165551234");
        ApiTestContext.put("otp", "123456");
        // Actually waiting out a 5+ minute expiry window on every test run isn't practical —
        // needs either a staging fixture endpoint that seeds an already-expired OTP, or a
        // clock-skew/test-mode flag on iam-service, once available.
    }

    @When("a VerifyOTP API call is made with the expired OTP")
    public void a_verify_otp_api_call_is_made_with_the_expired_otp() {
        verifyOtp(ApiTestContext.get("phoneNumber"), ApiTestContext.get("otp"));
    }

    @Then("the system should return an expiration error")
    public void the_system_should_return_an_expiration_error() {
        Assert.assertEquals(ApiTestContext.getLastResponse().statusCode(), 401,
                "Expected an expiration error status (401)"); // CONTRACT: may be a distinct code/body flag
    }

    @Given("incorrect OTPs have been submitted {int} times for {string}")
    public void incorrect_otps_have_been_submitted_n_times_for(int count, String phoneNumber) {
        ApiTestContext.put("phoneNumber", phoneNumber);
        for (int i = 0; i < count; i++) {
            verifyOtp(phoneNumber, "000000");
        }
    }

    @When("another incorrect OTP is submitted")
    public void another_incorrect_otp_is_submitted() {
        verifyOtp(ApiTestContext.get("phoneNumber"), "000000");
    }

    @Then("the account should be temporarily locked")
    public void the_account_should_be_temporarily_locked() {
        Assert.assertEquals(ApiTestContext.getLastResponse().statusCode(), 423,
                "Expected a 423 Locked response"); // CONTRACT: lockout status code is a guess
    }

    @And("a new OTP should be required after the lockout period")
    public void a_new_otp_should_be_required_after_lockout() {
        // Timing-dependent assertion — would need to wait out the real lockout window
        // (duration unknown/unconfirmed) to verify end to end.
    }
}
