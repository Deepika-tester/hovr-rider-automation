package com.automation.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Thin RestAssured wrapper for the three backend services under test
 * (IAM, Ride, Driver). Endpoint paths passed to get/post/etc. are relative —
 * this class only owns base URI, timeouts, content type, and the bearer token.
 *
 * Base URIs and timeouts come from api.properties (ApiConfigReader), independent
 * of the Appium mobile config.
 */
public class ApiClient {

    public enum Service { IAM, RIDE, DRIVER }

    private static String baseUrlFor(Service service) {
        return switch (service) {
            case IAM -> ApiConfigReader.get("iamBaseUrl");
            case RIDE -> ApiConfigReader.get("rideBaseUrl");
            case DRIVER -> ApiConfigReader.get("driverBaseUrl");
        };
    }

    private static RequestSpecification baseSpec(Service service) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrlFor(service))
                .setContentType(ContentType.JSON)
                .setConfig(io.restassured.config.RestAssuredConfig.config()
                        .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout",
                                        (int) TimeUnit.SECONDS.toMillis(ApiConfigReader.getInt("connectTimeoutSeconds")))
                                .setParam("http.socket.timeout",
                                        (int) TimeUnit.SECONDS.toMillis(ApiConfigReader.getInt("readTimeoutSeconds")))));

        String token = ApiTestContext.get("authToken");
        if (token == null) {
            token = ApiConfigReader.get("testAuthToken", "");
        }
        if (!token.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder.build();
    }

    public static Response get(Service service, String path) {
        Response response = io.restassured.RestAssured.given(baseSpec(service)).get(path);
        ApiTestContext.setLastResponse(response);
        return response;
    }

    public static Response post(Service service, String path, Object body) {
        Response response = io.restassured.RestAssured.given(baseSpec(service)).body(body).post(path);
        ApiTestContext.setLastResponse(response);
        return response;
    }

    public static Response post(Service service, String path, Map<String, ?> body) {
        return post(service, path, (Object) body);
    }

    public static Response put(Service service, String path, Object body) {
        Response response = io.restassured.RestAssured.given(baseSpec(service)).body(body).put(path);
        ApiTestContext.setLastResponse(response);
        return response;
    }

    public static Response delete(Service service, String path) {
        Response response = io.restassured.RestAssured.given(baseSpec(service)).delete(path);
        ApiTestContext.setLastResponse(response);
        return response;
    }
}
