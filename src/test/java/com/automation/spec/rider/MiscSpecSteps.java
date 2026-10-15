package com.automation.spec.rider;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Steps for the small features: 08_wallet, 09_refer_and_earn, 10_notifications_inbox, 11_help_support and 05_trips.
 * Preconditions the dev data already satisfies for a brand-new rider ("my balance is zero", "I have no messages") are
 * no-ops; preconditions it cannot be put into are on @blocked scenarios and never run.
 */
public class MiscSpecSteps {

    private final SpecUi ui = new SpecUi();

    private static final By TEXT_FIELDS = By.xpath("//android.widget.EditText");

    // ---- wallet / inbox / trips: empty-state preconditions ----

    @Given("my balance is zero")
    public void my_balance_is_zero() {
        // A new rider has no balance.
    }

    @Given("I have no messages")
    public void i_have_no_messages() {
        // A new rider has an empty inbox.
    }

    @Given("I have no scheduled trips")
    public void i_have_no_scheduled_trips() {
        // A new rider has no trips.
    }

    // ---- refer and earn ----

    @When("I tap the referral link")
    public void i_tap_the_referral_link() {
        // The row is one accessibility node; the copy control is the icon at its right-hand end.
        WebElement row = ui.visible(SpecUi.contains("REFERRAL LINK"), 15);
        Assert.assertNotNull(row, "The referral link is not on screen");
        ui.tapRightEdgeOf(row, 110);
    }

    @Then("the system share sheet opens with my invite link")
    public void the_system_share_sheet_opens() {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            String pkg = ((io.appium.java_client.android.AndroidDriver) ui.driverPublic()).getCurrentPackage();
            if (!"production.hovr.customer".equals(pkg)) {
                return; // the chooser belongs to the system, not to the app
            }
            ui.sleep(500);
        }
        Assert.fail("The system share sheet did not open");
    }

    @Given("I am on the redeem referral code screen")
    public void i_am_on_the_redeem_referral_code_screen() {
        SpecSession.open(ui, "Refer and earn");
        ui.tapLabel("Enter a referral code");
        Assert.assertTrue(ui.visible(TEXT_FIELDS, 15) != null, "Referral code screen not shown");
    }

    @When("I tap {string} without a code")
    public void i_tap_without_a_code(String label) {
        ui.tapLabel(label);
        ui.sleep(1200);
    }

    @Given("I have not referred anyone")
    public void i_have_not_referred_anyone() {
        // A new rider has not referred anyone.
    }

    // ---- help and support ----

    private void typeInto(int index, String value) {
        java.util.List<WebElement> fields = ui.all(TEXT_FIELDS);
        Assert.assertTrue(fields.size() > index, "Expected at least " + (index + 1) + " text fields, found " + fields.size());
        WebElement f = fields.get(index);
        f.click();
        f.clear();
        if (!value.isEmpty()) {
            f.sendKeys(value);
        }
    }

    @When("I search {string}")
    public void i_search(String term) {
        typeInto(0, term);
        ui.sleep(1500);
    }

    @Given("I have no support tickets")
    public void i_have_no_support_tickets() {
        // A new rider has none.
    }

    @When("I enter subject {string}")
    public void i_enter_subject(String subject) {
        typeInto(0, subject);
    }

    @When("I enter description {string}")
    public void i_enter_description(String description) {
        typeInto(1, description);
    }

    @Given("I am on the create ticket screen")
    public void i_am_on_the_create_ticket_screen() {
        SpecSession.open(ui, "Help");
        ui.tapLabel("New ticket");
        Assert.assertTrue(ui.waitSeen("New ticket", 15));
    }

    @When("I enter subject {string} and description {string}")
    public void i_enter_subject_and_description(String subject, String description) {
        typeInto(0, subject);
        typeInto(1, description);
    }

    @Given("I have an open ticket")
    public void i_have_an_open_ticket() {
        SpecSession.open(ui, "Help");
        ui.sleep(2000);
        if (ui.seen("No open tickets")) {
            ui.tapLabel("New ticket");
            typeInto(0, "Automation ticket " + (System.currentTimeMillis() % 100000));
            typeInto(1, "Created by the automated suite to test replying.");
            ui.tapLabel("Create ticket");
            ui.waitSeen("Support tickets", 20);
            ui.sleep(2000);
        }
    }

    @When("I open it")
    public void i_open_it() {
        By row = By.xpath("//android.widget.Button[starts-with(@content-desc,'Ticket:')]");
        WebElement el = ui.visible(row, 20);
        Assert.assertNotNull(el, "No ticket is listed");
        el.click();
        ui.sleep(2500);
    }

    @When("I send reply {string}")
    public void i_send_reply(String reply) {
        typeInto(ui.all(TEXT_FIELDS).size() - 1, reply);
        ui.tapLabel("Send reply");
    }

    @Then("the reply appears in the conversation")
    public void the_reply_appears_in_the_conversation() {
        Assert.assertTrue(ui.waitSeen("Any update?", 15), "The reply is not shown in the conversation");
    }
}
