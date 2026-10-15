package com.automation.spec.rider;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Steps for features/rider/03_ride_booking.feature -- only the part that can run safely: planning a route and looking at
 * the ride options. Nothing here confirms a pickup (that creates a real ride request), and only the first option, HOVR,
 * is ever selected.
 */
public class BookingSpecSteps {

    private final SpecUi ui = new SpecUi();

    private static final By DROPOFF_FIELD = By.xpath("(//android.widget.EditText)[2]");
    private static final By SUGGESTION_ROW = By.xpath(
            "//*[@clickable='true' and contains(@content-desc,'\n') and not(contains(@content-desc,'Tab '))]");
    private static final By HOVR_ROW = By.xpath("//*[@clickable='true' and starts-with(@content-desc,'HOVR')]");

    @Given("I am signed in with a default payment method")
    public void i_am_signed_in_with_a_default_payment_method() {
        SpecSession.ensureSignedIn(ui);
        SpecSession.ensureDefaultPayment(ui);
    }

    private void reachChooseARide() {
        SpecSession.goRide(ui);
        ui.tapLabel("Where to?");
        ui.waitSeen("Plan your trip", 15);
        ui.typeIntoPublic(DROPOFF_FIELD, SpecMarket.destination());
        WebElement row = ui.visible(SUGGESTION_ROW, 15);
        Assert.assertNotNull(row, "No suggestion for the destination");
        row.click();
        Assert.assertTrue(ui.waitSeen("Choose a ride", 40), "The Choose a ride screen did not open");
    }

    @Given("I have selected a pickup and a destination")
    public void i_have_selected_a_pickup_and_a_destination() {
        reachChooseARide();
    }

    @Given("I am on the choose a ride screen")
    public void i_am_on_the_choose_a_ride_screen() {
        if (!ui.seen("Choose a ride")) {
            reachChooseARide();
        }
    }

    @Then("each ride option shows a price and {string} ETA")
    public void each_ride_option_shows_a_price_and_eta(String eta) {
        Assert.assertNotNull(ui.visible(HOVR_ROW, 15), "No ride option is listed");
        for (WebElement row : ui.all(HOVR_ROW)) {
            String d = row.getAttribute("content-desc");
            Assert.assertTrue(d.matches("(?s).*[$₹]\\s?\\d.*"), "A ride option has no price: " + d);
            Assert.assertTrue(d.contains(eta), "A ride option shows no \"" + eta + "\" ETA; it reads: " + d.replace("\n", " | "));
        }
    }

    @When("I select a ride type")
    public void i_select_a_ride_type() {
        // HOVR is the first (and for this test the only) option that is ever used.
        ui.visible(HOVR_ROW, 15).click();
        ui.sleep(1500);
    }

    @Then("the {string} button shows the selected ride type")
    public void the_button_shows_the_selected_ride_type(String button) {
        Assert.assertTrue(ui.waitSeen(button + " HOVR", 10), "The " + button + " button does not name the selected ride type");
    }

    @When("I open the fare breakdown for a ride type")
    public void i_open_the_fare_breakdown_for_a_ride_type() {
        WebElement row = ui.visible(HOVR_ROW, 15);
        Assert.assertNotNull(row, "No ride option is listed");
        ui.tapRightEdgeOf(row, 80); // the info icon sits at the right end of the row
        ui.sleep(2000);
    }
}
