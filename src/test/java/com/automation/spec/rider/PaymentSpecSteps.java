package com.automation.spec.rider;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

/**
 * Steps for features/rider/07_payment.feature. Cards are entered in the Adyen drop-in dialog with Adyen's published
 * sandbox test numbers (dev build, throw-away test rider). The dialog is a secure window: it exposes its fields to
 * Appium but cannot be screenshotted.
 */
public class PaymentSpecSteps {

    private final SpecUi ui = new SpecUi();

    private static final By CARD_NUMBER = By.xpath("//android.widget.EditText[@text='Card number' or contains(@text,'1111') or contains(@text,'4444')]");
    private static final By EXPIRY = By.xpath("//android.widget.EditText[contains(@content-desc,'Expiry date')]");
    private static final By CVC = By.xpath("//android.widget.EditText[contains(@content-desc,'Security code')]");
    private static final By CARD_ROWS = By.xpath("//android.widget.ImageView[contains(@content-desc,'****')]");
    // The default card's row is not clickable; every other card's row is, and its "..." menu sits at the row's right end.
    private static final By CARD_ROWS_WITH_MENU = By.xpath("//android.widget.ImageView[@clickable='true' and contains(@content-desc,'****')]");

    private int cardsBefore;

    private void enterCard(String number, String expiry, String cvc) {
        ui.visible(CARD_NUMBER, 25);
        ui.typeIntoPublic(CARD_NUMBER, number);
        ui.typeIntoPublic(EXPIRY, expiry);
        ui.typeIntoPublic(CVC, cvc);
    }

    private void openAddCardDialog() {
        SpecSession.open(ui, "Payment Methods");
        ui.tapLabel("Add payment method");
        ui.tapLabel("Credit or debit card");
        Assert.assertNotNull(ui.visible(CARD_NUMBER, 30), "The card entry dialog did not open");
    }

    private int cardCount() {
        return ui.all(CARD_ROWS).size();
    }

    private void addCard(String number) {
        openAddCardDialog();
        enterCard(number, "0330", "737");
        ui.tapLabel("CONFIRM PREAUTHORIZATION");
        Assert.assertTrue(ui.waitSeen("Add payment method", 45), "Did not return to the payment methods list after adding a card");
    }

    private void ensureCards(int wanted) {
        SpecSession.open(ui, "Payment Methods");
        String[] numbers = {"4111111111111111", "5555444433331111", "4212345678901237"};
        for (int i = cardCount(); i < wanted; i++) {
            addCard(numbers[i]);
        }
        Assert.assertTrue(cardCount() >= wanted, "Could not get " + wanted + " saved card(s) onto the account");
    }

    @Given("I have no saved cards")
    public void i_have_no_saved_cards() {
        // True for a freshly registered rider; nothing to do.
    }

    @When("I enter valid card details")
    public void i_enter_valid_card_details() {
        enterCard("4111111111111111", "0330", "737");
    }

    @When("I add a card with invalid details")
    public void i_add_a_card_with_invalid_details() {
        openAddCardDialog();
        enterCard("4111111111111112", "0120", "000"); // fails the Luhn check and is expired
        ui.tapLabel("CONFIRM PREAUTHORIZATION");
    }

    @Given("I have two saved cards")
    public void i_have_two_saved_cards() {
        ensureCards(2);
    }

    @Given("I have a saved card")
    public void i_have_a_saved_card() {
        ensureCards(1);
    }

    /** Opens the "..." menu of the first card that has one (i.e. a card that is not the default). */
    private void openCardMenu() {
        cardsBefore = cardCount();
        List<WebElement> rows = ui.all(CARD_ROWS_WITH_MENU);
        if (rows.isEmpty() && ui.seen("Google Pay")) {
            ui.tapLabel("Google Pay"); // every card is the default; make Google Pay the default so a card gets a menu
            ui.sleep(3000);
            rows = ui.all(CARD_ROWS_WITH_MENU);
        }
        Assert.assertFalse(rows.isEmpty(), "No card has an options menu (the default card has none)");
        ui.tapRightEdgeOf(rows.get(0), 110);
        ui.sleep(1500);
    }

    @When("I open the second card")
    public void i_open_the_second_card() {
        openCardMenu(); // the card that is not the default
    }

    @When("I open the card")
    public void i_open_the_card() {
        openCardMenu();
    }

    @Then("the second card is marked {string}")
    public void the_second_card_is_marked(String mark) {
        String expected = SpecAliases.text(mark);
        ui.sleep(2500);
        boolean marked = ui.all(CARD_ROWS).stream().anyMatch(r -> {
            String d = r.getAttribute("content-desc");
            return d != null && d.contains(expected);
        });
        Assert.assertTrue(marked, "No card row is marked \"" + expected + "\"");
    }

    @Then("the card is removed from the list")
    public void the_card_is_removed_from_the_list() {
        ui.sleep(2500);
        Assert.assertTrue(ui.waitSeen("Add payment method", 15));
        Assert.assertEquals(cardCount(), cardsBefore - 1, "The card is still listed");
    }

    @Given("I am on a {string} device")
    public void i_am_on_a_device(String platform) {
        if (!"Android".equalsIgnoreCase(platform)) {
            throw new io.cucumber.java.PendingException("not applicable: this run is on an Android emulator, not " + platform);
        }
    }
}
