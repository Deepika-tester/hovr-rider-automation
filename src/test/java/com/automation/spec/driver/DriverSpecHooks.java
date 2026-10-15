package com.automation.spec.driver;

import com.automation.spec.rider.SpecAdb;
import io.cucumber.java.After;
import io.cucumber.java.Before;

/** App-data wipes happen before the Appium session starts (a wipe under a live session takes UiAutomator2 down). */
public class DriverSpecHooks {

    private static void wipe() {
        SpecAdb.shell("am", "force-stop", DriverSession.APP);
        SpecAdb.shell("pm", "clear", DriverSession.APP);
        for (String p : new String[] {"ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "ACCESS_BACKGROUND_LOCATION", "POST_NOTIFICATIONS", "CAMERA"}) {
            SpecAdb.shell("pm", "grant", DriverSession.APP, "android.permission." + p);
        }
    }

    @Before(value = "@authentication", order = 1)
    public void freshInstall() {
        wipe();
        SpecAdb.geoHome();
    }

    @After(value = "@language", order = 100)
    public void wipeAfterLanguage() {
        wipe();
    }
}
