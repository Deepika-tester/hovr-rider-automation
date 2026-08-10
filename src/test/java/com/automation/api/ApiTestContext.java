package com.automation.api;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-scenario scratch space shared across API step-definition classes.
 *
 * cucumber-java has no built-in dependency injection configured in this project
 * (no PicoContainer/Spring glue module), so step classes can't just share instance
 * state via constructor injection the way a fully wired DI setup would. A
 * ThreadLocal bag — mirroring the ThreadLocal pattern DriverManager already uses
 * for the Appium driver — is the simplest way to pass values (phone number, OTP,
 * auth token, last response) between Given/When/Then steps that may live in
 * different classes. ApiHooks clears it after every scenario.
 */
public class ApiTestContext {

    private static final ThreadLocal<Map<String, Object>> ATTRIBUTES =
            ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value) {
        ATTRIBUTES.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) ATTRIBUTES.get().get(key);
    }

    public static void setLastResponse(Response response) {
        put("lastResponse", response);
    }

    public static Response getLastResponse() {
        Response response = get("lastResponse");
        if (response == null) {
            throw new IllegalStateException(
                    "No API response recorded yet — a When step must call the API before this assertion runs.");
        }
        return response;
    }

    public static void clear() {
        ATTRIBUTES.get().clear();
    }
}
