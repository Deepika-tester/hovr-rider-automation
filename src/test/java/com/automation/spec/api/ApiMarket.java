package com.automation.spec.api;

import java.util.Map;

import static com.automation.spec.api.ConnectClient.m;

/**
 * Marketplace of an API run, chosen with -Dmarket=canada (default india). Decides the location a rider signs up from (the
 * backend assigns the marketplace from it) and the pickup/drop-off the ride and Co-Commute calls use.
 */
public final class ApiMarket {

    private ApiMarket() {}

    public static boolean canada() {
        return "canada".equalsIgnoreCase(System.getProperty("market", "india"));
    }

    public static double lat() {
        return canada() ? 43.6453 : 30.6781;
    }

    public static double lng() {
        return canada() ? -79.3806 : 76.7230;
    }

    public static Map<String, Object> pickup() {
        return m("latLng", m("latitude", lat(), "longitude", lng()));
    }

    public static Map<String, Object> dropoff() {
        return canada() ? m("latLng", m("latitude", 43.6629, "longitude", -79.3957))
                : m("latLng", m("latitude", 30.6942, "longitude", 76.8206));
    }

    public static String address() {
        return canada() ? "CN Tower, Toronto, ON, Canada" : "CP67 Mall, Mohali, Punjab, India";
    }
}
