package com.automation.spec.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Minimal ConnectRPC client (JSON encoding, unary calls) for the HOVR dev backend.
 *
 * The rider app speaks gRPC/ConnectRPC with protobuf; the same services also answer Connect's JSON encoding, which is
 * what the suite uses so request/response bodies stay readable in the report. Status names in the feature files are
 * gRPC codes, so every response is mapped to one: HTTP 200 is "OK", Connect error bodies carry their lower-case code
 * ("invalid_argument" -> INVALID_ARGUMENT), and anything that is not a Connect error at all (a proxy's 502 page, a 415)
 * is reported as HTTP_<n> -- those are findings in their own right.
 */
public final class ConnectClient {

    private ConnectClient() {}

    public enum Host {
        IAM("iam.api.dev.ridehovr.com"),
        RIDE("ca.ride.api.dev.ridehovr.com"),
        NOTIFICATION("ca.notification.api.dev.ridehovr.com");

        public final String name;

        Host(String name) {
            this.name = name;
        }
    }

    public record Result(int http, String status, String message, JsonNode json, String raw, long millis) {
        public boolean ok() {
            return http == 200;
        }

        public JsonNode at(String path) {
            JsonNode n = json;
            for (String part : path.split("\\.")) {
                if (n == null) {
                    return null;
                }
                n = n.get(part);
            }
            return n;
        }

        public String text(String path) {
            JsonNode n = at(path);
            return n == null || n.isNull() ? null : n.asText();
        }

        @Override
        public String toString() {
            return status + " (HTTP " + http + ")" + (message == null ? "" : " - " + message);
        }
    }

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public static ObjectMapper mapper() {
        return JSON;
    }

    /** Calls `service/method` with a JSON body; `bearer` and `dpop` are optional. */
    public static Result call(Host host, String rpc, Object body, String bearer, Dpop dpop) {
        return callHost(host.name, rpc, body, bearer, dpop);
    }

    /** Same as call(), for a host that is not in the Host enum (the driver backend). */
    public static Result callHost(String hostName, String rpc, Object body, String bearer, Dpop dpop) {
        String url = "https://" + hostName + "/" + rpc;
        long t0 = System.currentTimeMillis();
        try {
            HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("Connect-Protocol-Version", "1")
                    .POST(HttpRequest.BodyPublishers.ofString(body instanceof String s ? s : JSON.writeValueAsString(body)));
            if (bearer != null && !bearer.isEmpty()) {
                req.header("Authorization", "Bearer " + bearer);
            }
            if (dpop != null) {
                req.header("DPoP", dpop.proof(url, null));
            }
            HttpResponse<String> res = HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
            return parse(res.statusCode(), res.body(), System.currentTimeMillis() - t0);
        } catch (Exception e) {
            return new Result(0, "CLIENT_ERROR", e.toString(), null, String.valueOf(e), System.currentTimeMillis() - t0);
        }
    }

    private static Result parse(int http, String raw, long millis) {
        JsonNode json = null;
        try {
            json = raw == null || raw.isBlank() ? null : JSON.readTree(raw);
        } catch (Exception ignored) {
            // not JSON (an HTML error page, for instance)
        }
        if (http == 200) {
            return new Result(http, "OK", null, json == null ? JSON.createObjectNode() : json, raw, millis);
        }
        String code = json != null && json.has("code") ? json.get("code").asText() : null;
        String message = json != null && json.has("message") ? json.get("message").asText() : null;
        String status = code != null ? code.toUpperCase() : "HTTP_" + http;
        return new Result(http, status, message, json, raw, millis);
    }

    /** Convenience for building bodies inline. */
    public static Map<String, Object> m(Object... kv) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            out.put((String) kv[i], kv[i + 1]);
        }
        return out;
    }
}
