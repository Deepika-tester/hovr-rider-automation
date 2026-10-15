package com.automation.spec.rider;

import java.util.HashMap;
import java.util.Map;

/** Tiny scenario-scoped scratchpad for steps that set a precondition now and act on it later. */
final class ApiNote {

    private ApiNote() {}

    private static final Map<String, String> NOTES = new HashMap<>();

    static void set(String key, String value) {
        NOTES.put(key, value);
    }

    static String get(String key, String fallback) {
        return NOTES.getOrDefault(key, fallback);
    }

    static void clear() {
        NOTES.clear();
    }
}
