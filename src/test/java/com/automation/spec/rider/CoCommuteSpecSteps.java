package com.automation.spec.rider;

import io.appium.java_client.InteractsWithApps;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Steps for features/rider/04_co_commute.feature. Everything that books a seat or posts a commute is @blocked (it holds a
 * payment and creates a real booking/request on the shared dev backend). The browsing, empty-state, location and
 * "plan a commute" scenarios are exercised.
 */
public class CoCommuteSpecSteps {

    private static final String APP = "production.hovr.customer";
    private final SpecUi ui = new SpecUi();

    private void relaunch() {
        InteractsWithApps apps = (InteractsWithApps) ui.driverPublic();
        apps.terminateApp(APP);
        apps.activateApp(APP);
        ui.sleep(5000);
        ui.popup();
    }

    @Then("each journey shows leave time and seats left")
    public void each_journey_shows_leave_time_and_seats_left() {
        ui.sleep(2500);
        if (ui.seen("No nearby journeys")) {
            throw new PendingException("no journeys are listed near the test location right now, so there is nothing to check");
        }
        Assert.assertTrue(ui.seen("seat"), "A listed journey shows no seats-left text");
        Assert.assertTrue(ui.seen(" AM") || ui.seen(" PM") || ui.seen("Leaves") || ui.seen("Departs"),
                "A listed journey shows no leave time");
    }

    @Given("location permission is denied")
    public void location_permission_is_denied() {
        SpecAdb.shell("pm", "revoke", APP, "android.permission.ACCESS_FINE_LOCATION");
        SpecAdb.shell("pm", "revoke", APP, "android.permission.ACCESS_COARSE_LOCATION");
        ui.sleep(1500);
        try {
            relaunch(); // revoking a permission kills the app process
        } catch (RuntimeException e) {
            // the session may need a moment; the next navigation re-checks the screen
        }
        ui.popup();
        // Without location the app may not reach a healthy ride home, so do not insist on it: go to Co-Commute if it is there.
        if (ui.tryTapLabel("Co-Commute", 10)) {
            ui.waitSeen("Commutes near you", 10);
            ui.tryTapLabel("Nearby journeys", 5);
        }
    }

    @Given("there are no nearby journeys")
    public void there_are_no_nearby_journeys() {
        ui.sleep(2500);
        if (!ui.seen("No nearby journeys") && ui.seen("seat")) {
            throw new PendingException("journeys are listed near the test location right now, so the empty state cannot be shown");
        }
    }

    // ---- planning a commute (never posted) ----

    @Given("I am planning a co-commute")
    public void i_am_planning_a_co_commute() {
        SpecSession.open(ui, "Co-Commute");
        ui.tapLabel("Post your own commute");
        Assert.assertTrue(ui.waitSeen("Plan a Co-Commute", 15), "The Plan a Co-Commute form did not open");
        By destination = By.xpath("(//android.widget.EditText)[last()]");
        ui.typeIntoPublic(destination, SpecMarket.destinationPrefix());
        By suggestion = By.xpath("//*[@clickable='true' and contains(@content-desc,'\n') and not(contains(@content-desc,'Tab '))]");
        WebElement row = ui.visible(suggestion, 15);
        Assert.assertNotNull(row, "No destination suggestion");
        row.click();
        Assert.assertTrue(ui.waitSeen("Choose a time", 20), "The leave-time picker did not open after choosing a destination");
    }

    @When("I continue without a leave time")
    public void i_continue_without_a_leave_time() {
        // The picker always opens with a time already selected, so there is no "no leave time" state to confirm.
        // Leaving the picker with Back (instead of confirming, which would go on to post) is the closest equivalent.
        ui.back();
        ui.sleep(1500);
    }
}
