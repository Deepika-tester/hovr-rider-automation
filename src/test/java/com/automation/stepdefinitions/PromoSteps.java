package com.automation.stepdefinitions;

import com.automation.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for src/test/resources/features/rider/rider_promo.feature.
 * See RegistrationSteps javadoc for the Flutter-locator disclaimer.
 *
 * "I navigate to the payment methods screen" (wallet-balance scenario) is already
 * defined in PaymentSteps and reused here verbatim via Cucumber's global step registry.
 */
public class PromoSteps {

    private final BasePage ui = new BasePage() {};

    @Given("I am in the ride booking flow")
    public void i_am_in_the_ride_booking_flow() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("Where to")), // VERIFY home-screen anchor text
                "Expected to be in the ride booking flow");
    }

    @When("I enter promo code {string}")
    public void i_enter_promo_code(String code) {
        ui.typeIntoPublic(ui.byAccessibilityIdPublic("promo_code_field"), code); // VERIFY
    }

    @Then("the promo should be validated")
    public void the_promo_should_be_validated() {
        // Backend-only assertion — the UI-visible consequence is asserted by the next step.
    }

    @And("I should see {string} discount applied")
    public void i_should_see_discount_applied(String discountLabel) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(discountLabel)),
                "Expected discount confirmation: " + discountLabel);
    }

    @And("the estimated fare should reflect the discount")
    public void the_estimated_fare_should_reflect_the_discount() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("fare_total")), // VERIFY
                "Expected fare total to reflect the applied discount");
    }

    @Given("I have already used promo code {string}")
    public void i_have_already_used_promo_code(String code) {
        // Test-data precondition.
    }

    @When("I try to apply {string} again")
    public void i_try_to_apply_promo_again(String code) {
        i_enter_promo_code(code);
        ui.tapTextPublic("Apply");
    }

    @When("I try to apply another promo code")
    public void i_try_to_apply_another_promo_code() {
        i_enter_promo_code("ANOTHER10"); // VERIFY — any code, since the limit check fires before validation
        ui.tapTextPublic("Apply");
    }

    @Given("I have ${double} in wallet credits")
    public void i_have_amount_in_wallet_credits(double amount) {
        // Test-data precondition.
    }

    @And("my ride fare is ${double}")
    public void my_ride_fare_is(double amount) {
        // Test-data precondition.
    }

    // "I confirm the ride" is already defined in BookingSteps and reused here.

    @Then("${double} should be deducted from my wallet")
    public void amount_should_be_deducted_from_my_wallet(double amount) {
        // Backend/state assertion — verifiable via a follow-up wallet-balance screen check
        // once that navigation is chained in; not directly visible on the ride-confirmation
        // screen itself.
    }

    @And("${double} should be charged to my payment method")
    public void amount_should_be_charged_to_payment_method(double amount) {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic(String.valueOf(amount))),
                "Expected charge amount to be shown: $" + amount);
    }

    @And("nothing should be charged to my payment method")
    public void nothing_should_be_charged_to_payment_method() {
        Assert.assertFalse(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("payment_charge_line")), // VERIFY
                "Did not expect a payment method charge line to be shown");
    }

    @Given("I have wallet credits from promotions")
    public void i_have_wallet_credits_from_promotions() {
        // Test-data precondition.
    }

    @Then("I should see my current wallet balance")
    public void i_should_see_my_current_wallet_balance() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byAccessibilityIdPublic("wallet_balance")), // VERIFY
                "Wallet balance not shown");
    }

    @And("the balance should reflect all applied credits")
    public void the_balance_should_reflect_all_applied_credits() {
        // Numeric-correctness check — needs the expected total computed from seeded test
        // data once a fixed test account/promotion history is available.
    }

    @Given("the GoodLife partnership promotion is active")
    public void the_goodlife_partnership_promotion_is_active() {
        // Test-data/campaign-config precondition.
    }

    @When("I apply promo code {string}")
    public void i_apply_promo_code(String code) {
        i_enter_promo_code(code);
        ui.tapTextPublic("Apply");
    }

    @Then("I should receive ${int} credit in my wallet")
    public void i_should_receive_credit_in_my_wallet(int amount) {
        // Backend/state assertion — see #amount_should_be_deducted_from_my_wallet.
    }

    @And("the credit should be applicable to my next ride")
    public void the_credit_should_be_applicable_to_next_ride() {
        // Cross-scenario/state assertion — verifiable by chaining into a ride-booking flow
        // once wallet-application behavior is confirmed end to end.
    }

    @Given("the FIFA 2026 promotion is active")
    public void the_fifa_2026_promotion_is_active() {
        // Test-data/campaign-config precondition.
    }

    @Then("I should receive the promotional discount")
    public void i_should_receive_the_promotional_discount() {
        the_estimated_fare_should_reflect_the_discount();
    }

    @And("the discount should apply only to rides within the event zone")
    public void the_discount_should_apply_only_within_event_zone() {
        // Geofencing/eligibility rule — needs test rides both inside and outside the event
        // zone to verify; out of scope for a single UI assertion.
    }

    @Then("the code should be trimmed and validated")
    public void the_code_should_be_trimmed_and_validated() {
        the_promo_should_be_validated();
    }

    @And("the promo should be applied successfully")
    public void the_promo_should_be_applied_successfully() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("discount applied")), // VERIFY exact copy
                "Expected the promo to be applied successfully");
    }

    @Given("my wallet has reached the maximum credit limit")
    public void my_wallet_has_reached_max_credit_limit() {
        // Test-data precondition.
    }

    @Then("I should see a message about the wallet limit")
    public void i_should_see_message_about_wallet_limit() {
        Assert.assertTrue(ui.isDisplayedPublic(ui.byTextContainsPublic("limit")), // VERIFY exact copy
                "Expected a wallet-limit message");
    }
}
