package com.automation.spec.driverapi;

import com.automation.spec.api.ConnectClient;
import com.automation.spec.api.ConnectClient.Host;
import com.automation.spec.api.ConnectClient.Result;
import com.automation.spec.api.Dpop;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automation.spec.api.ConnectClient.m;

/**
 * The driver app's backend. Driver RPCs live on four hosts (found by probing the dev environment 2026-10-15):
 * iam.* and the driver sign-in on iam.api, driver.v1.* and the Co-Commute eligibility check on ca.driver.api, ride.v1.* and
 * neighbour.v1.driver.* on ca.ride.api, notification.v1.* on ca.notification.api. Streaming RPCs (StreamDriver,
 * StreamLocation, StreamTrip) answer 415 to a unary JSON call and are not exercised.
 *
 * A driver session signs in exactly like the app (OTP challenge -> verify with 111111 -> refresh, DPoP-bound) and is shared
 * by the whole run. The approved test driver's OTP quota on the dev backend is small (the backend answers "too many OTP
 * requests today"), so a session is requested ONCE per run and never retried.
 */
final class DriverApi {

    private DriverApi() {}

    static final String AUTH = "iam.auth.v1.AuthService/";

    static Host host(String rpc) {
        if (rpc.startsWith("iam.")) {
            return Host.IAM;
        }
        if (rpc.startsWith("ride.") || rpc.startsWith("neighbour.v1.driver.")) {
            return Host.RIDE;
        }
        if (rpc.startsWith("notification.")) {
            return Host.NOTIFICATION;
        }
        return DRIVER;
    }

    /** ca.driver.api is not in the rider suite's Host enum; carried as a plain host name. */
    static final Host DRIVER = null;

    static Result call(String rpc, Object body, String token, Dpop dpop) {
        Host h = host(rpc);
        if (h == null) {
            return ConnectClient.callHost("ca.driver.api.dev.ridehovr.com", rpc, body, token, dpop);
        }
        return ConnectClient.call(h, rpc, body, token, dpop);
    }

    static final class Session {
        final Dpop dpop = new Dpop();
        String phone;
        String accessToken;
        String refreshToken;
        String driverId;
        String marketplaceId;
        Result verify;
        Result profile;
    }

    private static Session shared;
    private static String sharedFailure;

    static synchronized Session approvedDriver() {
        if (shared != null) {
            return shared;
        }
        if (sharedFailure != null) {
            throw new IllegalStateException(sharedFailure);
        }
        if (Boolean.getBoolean("driverApi.noLogin")) {
            throw new IllegalStateException("signing in as the test driver is switched off for this run (-DdriverApi.noLogin=true)");
        }
        String phone = "+1" + com.automation.utils.ConfigReader.get("testDriverPhone", "9055652565");
        try {
            shared = login(phone);
            return shared;
        } catch (RuntimeException e) {
            sharedFailure = e.getMessage();
            throw e;
        }
    }

    static Session login(String phone) {
        Session s = new Session();
        s.phone = phone;
        Result ch = call(AUTH + "CreateVerificationChallenge",
                m("phoneNumber", m("e164", phone), "userType", "USER_TYPE_DRIVER"), null, s.dpop);
        need(ch, "request the OTP for " + phone);
        s.verify = call(AUTH + "VerifyVerificationChallenge",
                m("phoneNumber", m("e164", phone), "otpCode", "111111", "userType", "USER_TYPE_DRIVER",
                        "deviceToken", ch.text("deviceToken"), "latLng", m("latitude", 43.6532, "longitude", -79.3832)),
                null, s.dpop);
        need(s.verify, "verify the OTP");
        s.accessToken = s.verify.text("authToken");
        s.refreshToken = s.verify.text("refreshToken");
        Result r = call(AUTH + "RefreshAccessToken", m("refreshToken", s.refreshToken), null, s.dpop);
        if (r.ok()) {
            s.accessToken = r.text("authToken") != null ? r.text("authToken") : s.accessToken;
            if (r.text("refreshToken") != null) {
                s.refreshToken = r.text("refreshToken");
            }
        }
        s.profile = call(AUTH + "GetActiveProfile", m("latLng", m("latitude", 43.6532, "longitude", -79.3832)), s.accessToken, null);
        s.driverId = firstText(s.profile.json(), "driverId", "id");
        if (s.driverId == null) {
            s.driverId = firstText(s.verify.json(), "driverId", "id");
        }
        s.marketplaceId = firstText(s.profile.json(), "marketplaceId");
        return s;
    }

    private static void need(Result r, String what) {
        if (!r.ok()) {
            throw new IllegalStateException("Could not " + what + ": " + r + " " + (r.message() == null ? "" : r.message()));
        }
    }

    /** First textual value found for any of the keys, depth-first. */
    static String firstText(JsonNode n, String... keys) {
        if (n == null) {
            return null;
        }
        for (String k : keys) {
            if (n.has(k) && n.get(k).isValueNode() && !n.get(k).asText().isEmpty()) {
                return n.get(k).asText();
            }
        }
        for (JsonNode c : n) {
            String v = firstText(c, keys);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    static String camel(String snake) {
        Matcher m = Pattern.compile("_([a-z0-9])").matcher(snake);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
