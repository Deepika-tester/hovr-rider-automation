package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Host;
import com.automation.spec.api.ConnectClient.Result;

import java.util.HashMap;
import java.util.Map;

/** Per-scenario state for the API steps (the cucumber glue classes cannot see each other's fields). */
public final class ApiWorld {

    private ApiWorld() {}

    public static Result last;
    public static RiderSession me;
    public static String tokenOverride;          // null = use the rider's real token, "" = send no token
    public static String verifyPhone;            // fresh +91 number used where the spec uses a Canadian example
    public static String verifyDeviceToken;
    public static String refreshOverride;
    public static final Map<String, Object> memo = new HashMap<>();
    private static Dpop scenarioDpop;

    /** One DPoP key per scenario, shared by the challenge and the verify call that belongs to it. */
    public static Dpop memoDpop() {
        if (scenarioDpop == null) {
            scenarioDpop = new Dpop();
        }
        return scenarioDpop;
    }

    public static void reset() {
        last = null;
        me = null;
        tokenOverride = null;
        verifyPhone = null;
        verifyDeviceToken = null;
        refreshOverride = null;
        memo.clear();
        scenarioDpop = null;
    }

    /** "AuthService/CreateVerificationChallenge" or a bare method name -> where it lives and its full path. */
    public record Rpc(Host host, String path) {}

    public static Rpc rpc(String name) {
        String svc = name.contains("/") ? name.substring(0, name.indexOf('/')) : null;
        String method = name.contains("/") ? name.substring(name.indexOf('/') + 1) : name;
        if (svc == null) {
            svc = switch (method) {
                case "CreateAdyenPaymentSession", "ListPaymentMethods", "SetDefaultPaymentMethod", "DeletePaymentMethod" -> "RiderProfileService";
                case "ListOpenTickets", "CreateTicket", "TicketArticle", "ListTicketArticles" -> "HelpdeskService";
                default -> throw new IllegalArgumentException("Unknown RPC " + name);
            };
        }
        return switch (svc) {
            case "AuthService" -> new Rpc(Host.IAM, "iam.auth.v1.AuthService/" + method);
            case "RiderProfileService" -> new Rpc(Host.IAM, "iam.rider.v1.RiderProfileService/" + method);
            case "WalletService" -> new Rpc(Host.IAM, "iam.wallet.v1.WalletService/" + method);
            case "ReferralService" -> new Rpc(Host.IAM, "iam.referral.v1.ReferralService/" + method);
            case "ActivityService" -> new Rpc(Host.IAM, "iam.activity.v1.ActivityService/" + method);
            case "NotificationSettingsService" -> new Rpc(Host.IAM, "iam.notification.v1.NotificationSettingsService/" + method);
            case "MaskedCallingService" -> new Rpc(Host.IAM, "iam.twilio.v1.MaskedCallingService/" + method);
            case "RideService" -> new Rpc(Host.RIDE, "ride.v1.ride.RideService/" + method);
            case "TripService" -> new Rpc(Host.RIDE, "ride.v1.trip.TripService/" + method);
            case "NeighbourRiderService" -> new Rpc(Host.RIDE, "neighbour.v1.rider.NeighbourRiderService/" + method);
            case "InboxService" -> new Rpc(Host.NOTIFICATION, "notification.v1.inbox.InboxService/" + method);
            case "HelpdeskService" -> new Rpc(Host.NOTIFICATION, "notification.v1.helpdesk.HelpdeskService/" + method);
            default -> throw new IllegalArgumentException("Unknown service in " + name);
        };
    }

    public static Result call(String name, Object body) {
        Rpc r = rpc(name);
        String token = tokenOverride != null ? tokenOverride : (me == null ? null : me.accessToken);
        last = ConnectClient.call(r.host(), r.path(), body, token, null);
        return last;
    }
}
