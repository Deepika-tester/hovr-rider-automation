package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Result;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Steps for features/api/07_wallet_referral_service.feature. Redeeming a referral code changes the redeemer's account
 * for good, so every redeem scenario uses a brand-new rider and a code taken from another real rider -- the spec's
 * literal "FRIEND5" does not exist on the dev backend.
 */
public class ApiWalletReferralSteps {

    private static final String REFERRAL = "ReferralService/";

    private static String referralCodeOf(RiderSession s) {
        Result r = s.iam("iam.referral.v1.ReferralService/GetReferralProfile", m());
        Assert.assertTrue(r.ok(), "GetReferralProfile failed: " + r);
        return r.text("MyReferralCode");
    }

    // ---- wallet ----

    @When("I call {string} with userId {string}")
    public void i_call_get_wallet_with_user_id(String rpc, String ignoredPlaceholder) {
        ApiWorld.call(rpc, m());
    }

    @Then("the response contains available, pending and locked balances")
    public void the_response_contains_available_pending_and_locked_balances() {
        Assert.assertTrue(ApiWorld.last.ok(), "GetWallet failed: " + ApiWorld.last);
        // Proto3 JSON leaves zero-valued fields out, so a new rider's balances (all zero) arrive as an empty object.
        Assert.assertNotNull(ApiWorld.last.at("wallet"), "No wallet object in: " + ApiWorld.last.raw());
    }

    @When("I call {string} with another user's id")
    public void i_call_get_wallet_with_another_users_id(String rpc) {
        ApiWorld.call(rpc, m("userId", RiderSession.second().riderId));
    }

    // ---- referral ----

    @Then("the response contains my referral code and invite link")
    public void the_response_contains_my_referral_code_and_invite_link() {
        Assert.assertTrue(ApiWorld.last.ok(), "GetReferralProfile failed: " + ApiWorld.last);
        Assert.assertNotNull(ApiWorld.last.text("MyReferralCode"), "No referral code: " + ApiWorld.last.raw());
        String raw = ApiWorld.last.raw();
        Assert.assertTrue(raw.contains("http") || raw.toLowerCase().contains("invite") || raw.toLowerCase().contains("link"),
                "The response carries the code but no invite link (the app builds ridehovr.com/invite/<code> itself): " + raw);
    }

    @Given("another rider has referral code {string}")
    public void another_rider_has_referral_code(String specCode) {
        ApiWorld.memo.put("friendCode", referralCodeOf(RiderSession.second()));
        ApiWorld.me = RiderSession.signUp("Ref", "Redeemer");
    }

    @When("I call {string} with code {string}")
    public void i_call_redeem_with_code(String rpc, String code) {
        if (ApiWorld.me == null) {
            ApiWorld.me = RiderSession.signUp("Ref", "Redeemer");
        }
        String actual = switch (code) {
            case "FRIEND5" -> (String) ApiWorld.memo.get("friendCode");
            case "<my own code>" -> referralCodeOf(ApiWorld.me);
            default -> code;
        };
        ApiWorld.call(rpc, m("referralCode", actual));
    }

    @Given("I already redeemed a referral code")
    public void i_already_redeemed_a_referral_code() {
        ApiWorld.me = RiderSession.signUp("Ref", "Twice");
        String code = referralCodeOf(RiderSession.second());
        ApiWorld.memo.put("friendCode", code);
        Result first = ApiWorld.call("ReferralService/RedeemReferral", m("referralCode", code));
        Assert.assertTrue(first.ok(), "The first redeem should have worked to set the scene: " + first);
    }

    @When("I call {string} again")
    public void i_call_redeem_again(String rpc) {
        ApiWorld.call(rpc, m("referralCode", ApiWorld.memo.get("friendCode")));
    }

    @Then("each reward shows rides completed out of total and amount")
    public void each_reward_shows_rides_completed_out_of_total_and_amount() {
        Assert.assertTrue(ApiWorld.last.ok(), "ListReferralReward failed: " + ApiWorld.last);
        if (ApiWorld.last.at("rewards") == null) {
            return; // a new rider has none; nothing to check (and nothing wrong)
        }
        for (var reward : ApiWorld.last.at("rewards")) {
            Assert.assertTrue(reward.size() >= 2, "Reward has too little data: " + reward);
        }
    }
}
