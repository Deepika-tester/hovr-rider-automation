package com.automation.spec.api;

import static com.automation.spec.api.ConnectClient.m;

/** Default request bodies for calls the feature files make without spelling the body out. */
final class ApiCommonCalls {

    private ApiCommonCalls() {}

    static void callNoBody(String rpc) {
        String method = rpc.contains("/") ? rpc.substring(rpc.indexOf('/') + 1) : rpc;
        Object body = switch (method) {
            case "GetRiderProfile", "GetActiveProfile", "ListPaymentMethods", "ListSavedPlaces", "CreateAdyenPaymentSession",
                 "GetWallet", "GetReferralProfile", "ListReferralReward", "ListActivity", "ListNotifications",
                 "ListBroadcasts", "ListOpenTickets", "GetActiveTrip", "ListTrips", "ListMyNeighbourBookings",
                 "ListMyNeighbourRequests", "MakeMaskedCall", "SetupMapping" -> m();
            case "UnregisterDeviceToken" -> m("fcmToken", ApiWorld.memo.get("fcmToken"));
            default -> m();
        };
        ApiWorld.call(rpc, body);
    }
}
