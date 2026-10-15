package com.automation.spec.rider;

import io.cucumber.java.Before;

/**
 * Scenarios tagged @auth begin with "the app is freshly installed". Clearing the app's data while an Appium session is
 * already attached tends to take the UiAutomator2 server down with it, so the wipe is done here instead -- before
 * Hooks.setUp (order 10000) opens the session -- and the step itself becomes a no-op.
 */
public class SpecHooks {

    private static final String APP = "production.hovr.customer";

    @Before(value = "@auth or @fresh-install or @keep-guide", order = 1)
    public void freshInstall() {
        SpecAdb.shell("am", "force-stop", APP);
        SpecAdb.shell("pm", "clear", APP);
        for (String p : new String[] {"ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "POST_NOTIFICATIONS", "CAMERA"}) {
            SpecAdb.shell("pm", "grant", APP, "android.permission." + p);
        }
        SpecAdb.geoHome();
    }

    /**
     * A language switch re-localises every screen, and the navigation helpers look for English labels. Wiping the app
     * after each @language example puts the next scenario back on a fresh (English) rider.
     */
    @io.cucumber.java.After(value = "@language", order = 100)
    public void wipeAfterLanguageChange() {
        SpecAdb.shell("am", "force-stop", APP);
        SpecAdb.shell("pm", "clear", APP);
        for (String p : new String[] {"ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "POST_NOTIFICATIONS", "CAMERA"}) {
            SpecAdb.shell("pm", "grant", APP, "android.permission." + p);
        }
    }

    /** The notification permission has to be missing BEFORE sign-up for the app to ask for it at the end of onboarding. */
    @Before(value = "@no-notifications", order = 2)
    public void revokeNotificationPermission() {
        SpecAdb.shell("pm", "revoke", APP, "android.permission.POST_NOTIFICATIONS");
    }

    /** @keep-guide scenarios want the brand-new rider's swipe guide left showing, so sign-up must not dismiss it. */
    @Before(value = "@keep-guide", order = 20001)
    public void keepSwipeGuide() {
        SpecContext.keepGuide = true;
    }
}
