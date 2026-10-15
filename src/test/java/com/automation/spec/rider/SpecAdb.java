package com.automation.spec.rider;

import com.automation.utils.ConfigReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Tiny adb helper for emulator-only setup the spec steps need (location, permissions). */
public final class SpecAdb {

    private SpecAdb() {}

    private static String udid() {
        return ConfigReader.get("udid", "emulator-5554");
    }

    public static void shell(String... args) {
        List<String> cmd = new ArrayList<>(List.of("adb", "-s", udid(), "shell"));
        cmd.addAll(List.of(args));
        run(cmd);
    }

    /** Emulator only; adb takes longitude first. */
    private static boolean moved;

    public static boolean moved() {
        return moved;
    }

    public static void geoFix(double lat, double lng) {
        moved = true;
        run(List.of("adb", "-s", udid(), "emu", "geo", "fix", String.valueOf(lng), String.valueOf(lat)));
    }

    /** Puts the emulator back at the Mohali test location the rest of the suite assumes. */
    public static void geoHome() {
        moved = false;
        run(java.util.List.of("adb", "-s", udid(), "emu", "geo", "fix", ConfigReader.get("emulatorGeoLng", "76.7230"), ConfigReader.get("emulatorGeoLat", "30.6781")));
    }

    private static void run(List<String> cmd) {
        try {
            new ProcessBuilder(cmd).redirectErrorStream(true).start().waitFor(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            // best-effort, like the rest of the adb helpers
        }
    }

    public static void swipe(int x1, int y1, int x2, int y2, int ms) {
        shell("input", "swipe", String.valueOf(x1), String.valueOf(y1), String.valueOf(x2), String.valueOf(y2), String.valueOf(ms));
    }

    private static boolean locationOff;

    /** 0 = off, 3 = high accuracy. */
    public static void setLocationMode(int mode) {
        locationOff = mode == 0;
        shell("settings", "put", "secure", "location_mode", String.valueOf(mode));
    }

    public static boolean locationOff() {
        return locationOff;
    }
}
