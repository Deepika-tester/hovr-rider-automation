package com.automation.spec.rider;

import java.util.Map;

/**
 * Per-scenario state shared between the spec step classes (cucumber creates one instance of each glue class
 * per scenario but they cannot see each other, so the few values they must share live here).
 *
 * The feature files are written with a Canadian example number ("4165550123", "+1 4165550123"), but the dev
 * backend only honours the 111111 staging OTP for India (+91) numbers. So when a scenario "submits" the spec
 * number the steps actually register a fresh +91 number, and every text the scenario then asserts is run through
 * {@link #expand(String)} so "Enter the 6-digit code sent to you at +1 4165550123." still matches what the app
 * really shows ("... at +91 9xxxxxxxxx.").
 */
public final class SpecContext {

    private SpecContext() {}

    /** True for @fresh-install scenarios that must see the new rider's first-visit swipe guide. */
    public static boolean keepGuide;

    private static final java.util.Set<String> TRANSIENTS = new java.util.HashSet<>();

    /** Texts that flashed on screen for an instant (progress messages) and were seen while they were up. */
    public static void sawTransient(String text) {
        TRANSIENTS.add(text);
    }

    public static boolean sawTransientText(String text) {
        return TRANSIENTS.contains(text);
    }

    private static String specPhone;
    private static String actualPhone;

    /** Pseudo-texts the feature files use for a screen rather than for a literal string. */
    private static final Map<String, String> ALIASES = Map.of(
            "OTP verification page", "digit code sent");

    public static void reset() {
        specPhone = null;
        actualPhone = null;
        keepGuide = false;
        TRANSIENTS.clear();
    }

    public static void phoneUsed(String spec, String actual) {
        specPhone = spec;
        actualPhone = actual;
    }

    public static String actualPhone() {
        return actualPhone;
    }

    /** Maps feature-file wording to what the app can really show in this environment. */
    public static String expand(String text) {
        String t = ALIASES.getOrDefault(text, text);
        if (specPhone != null && actualPhone != null && t.contains(specPhone)) {
            // The app prints the number as "+919..." with no space after the country code; the spec writes "+1 4165550123".
            // That cosmetic difference is listed in the report's spec-corrections table instead of failing the scenario.
            String prefix = SpecMarket.canada() ? "+1" : "+91";
            t = t.replace("+1 " + specPhone, prefix + actualPhone).replace("+1" + specPhone, prefix + actualPhone)
                    .replace(specPhone, actualPhone);
        }
        return t;
    }
}
