package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Host;
import com.automation.spec.api.ConnectClient.Result;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automation.spec.api.ConnectClient.m;

/**
 * A signed-in rider on the dev backend, created through the same calls the app makes: request an OTP challenge,
 * verify it with the 111111 staging code (India numbers only), create the rider profile, then refresh to get the full
 * access token. The refresh must be signed with the same DPoP key as the sign-in, so the key lives here.
 */
public final class RiderSession {

    public static final String AUTH = "iam.auth.v1.AuthService/";
    public static final String PROFILE = "iam.rider.v1.RiderProfileService/";

    public final Dpop dpop = new Dpop();
    public String phone;
    public String riderId;
    public String accessToken;
    public String refreshToken;
    public String firstName;
    public String lastName;
    public String email;
    public String signupToken;
    public Result lastVerify;

    private static RiderSession primary;
    private static RiderSession second;

    /** One rider shared by every API scenario in the run (sign-up is slow and rate-limited). */
    public static synchronized RiderSession primary() {
        if (primary == null) {
            primary = signUp("Jane", "Doe");
        }
        return primary;
    }

    /** A second, unrelated rider for "another rider's ..." scenarios. */
    public static synchronized RiderSession second() {
        if (second == null) {
            second = signUp("Sam", "Rider");
        }
        return second;
    }

    public static String uniquePhone() {
        return ApiMarket.canada() ? "+1" + com.automation.spec.rider.PhonePool.canadian()
                : "+91" + com.automation.spec.rider.PhonePool.indian();
    }

    /** Requests an OTP challenge, waiting out the per-number resend cooldown if the server asks for it. */
    public static Result requestChallenge(String phone, Dpop dpop) {
        Result r = null;
        for (int i = 0; i < 6; i++) {
            r = ConnectClient.call(Host.IAM, AUTH + "CreateVerificationChallenge",
                    m("phoneNumber", m("e164", phone), "userType", "USER_TYPE_RIDER"), null, dpop);
            if (r.ok() || !"RESOURCE_EXHAUSTED".equals(r.status())) {
                return r;
            }
            Matcher w = Pattern.compile("wait (\\d+) seconds").matcher(String.valueOf(r.message()));
            sleep((w.find() ? Integer.parseInt(w.group(1)) : 5) * 1000L + 1000L);
        }
        return r;
    }

    public static Result verify(String phone, String code, String deviceToken, Dpop dpop) {
        return ConnectClient.call(Host.IAM, AUTH + "VerifyVerificationChallenge",
                m("phoneNumber", m("e164", phone), "otpCode", code, "userType", "USER_TYPE_RIDER",
                        "deviceToken", deviceToken, "latLng", m("latitude", ApiMarket.lat(), "longitude", ApiMarket.lng())),
                null, dpop);
    }

    public static RiderSession signUp(String first, String last) {
        RiderSession s = new RiderSession();
        s.firstName = first;
        s.lastName = last;
        s.email = (first + "." + last + "+api" + (System.currentTimeMillis() % 1_000_000) + "@example.com").toLowerCase();
        s.phone = uniquePhone();
        Result ch = requestChallenge(s.phone, s.dpop);
        require(ch, "create the OTP challenge");
        Result v = verify(s.phone, "111111", ch.text("deviceToken"), s.dpop);
        require(v, "verify the OTP");
        s.lastVerify = v;
        s.signupToken = v.text("authToken");
        s.refreshToken = v.text("refreshToken");
        // The app sends name and email with CreateRiderProfile; UpdateRiderProfile later ignores the email.
        Result created = ConnectClient.call(Host.IAM, PROFILE + "CreateRiderProfile",
                m("firstName", first, "lastName", last, "email", s.email), s.signupToken, null);
        require(created, "create the rider profile");
        s.riderId = created.text("rider.riderId");
        s.refresh();
        return s;
    }

    public Result refresh() {
        Result r = ConnectClient.call(Host.IAM, AUTH + "RefreshAccessToken", m("refreshToken", refreshToken), null, dpop);
        require(r, "refresh the access token");
        accessToken = r.text("authToken");
        if (r.text("refreshToken") != null) {
            refreshToken = r.text("refreshToken");
        }
        return r;
    }

    public Result call(Host host, String rpc, Object body) {
        return ConnectClient.call(host, rpc, body, accessToken, null);
    }

    public Result iam(String rpc, Object body) {
        return call(Host.IAM, rpc, body);
    }

    public Result ride(String rpc, Object body) {
        return call(Host.RIDE, rpc, body);
    }

    public Result notification(String rpc, Object body) {
        return call(Host.NOTIFICATION, rpc, body);
    }

    private static void require(Result r, String what) {
        if (!r.ok()) {
            throw new IllegalStateException("Could not " + what + ": " + r);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public JsonNode profile() {
        return iam(PROFILE + "GetRiderProfile", m()).at("rider");
    }

    /** A real referral code (the code of a brand-new rider), for tests that need "a friend's code". */
    public static String freshReferralCode() {
        RiderSession friend = signUp("Friend", "Referrer");
        Result r = friend.iam("iam.referral.v1.ReferralService/GetReferralProfile", m());
        if (!r.ok() || r.text("MyReferralCode") == null) {
            throw new IllegalStateException("Could not read a referral code: " + r);
        }
        return r.text("MyReferralCode");
    }
}
