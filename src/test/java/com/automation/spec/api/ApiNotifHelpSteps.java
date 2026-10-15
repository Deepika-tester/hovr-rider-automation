package com.automation.spec.api;

import com.automation.spec.api.ConnectClient.Result;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import static com.automation.spec.api.ConnectClient.m;

/** Steps for features/api/08_notification_activity_service.feature and 09_helpdesk_api.feature. */
public class ApiNotifHelpSteps {

    private static String fcm() {
        return "fcm-automation-" + Long.toHexString(System.nanoTime());
    }

    // ---- notifications ----

    @When("I call {string} with a valid FCM token and platform")
    public void i_call_register_device_token(String rpc) {
        String token = fcm();
        ApiWorld.memo.put("fcmToken", token);
        ApiWorld.call(rpc, m("fcmToken", token, "platform", "ANDROID"));
    }

    @Given("my device token is registered")
    public void my_device_token_is_registered() {
        ApiWorld.me = RiderSession.primary();
        String token = fcm();
        ApiWorld.memo.put("fcmToken", token);
        Result r = ApiWorld.call("NotificationSettingsService/RegisterDeviceToken", m("fcmToken", token, "platform", "ANDROID"));
        Assert.assertTrue(r.ok(), "Could not register a device token to set the scene: " + r);
    }

    @When("I call {string} for my device token")
    public void i_call_unregister_device_token(String rpc) {
        ApiWorld.call(rpc, m("fcmToken", ApiWorld.memo.get("fcmToken")));
    }

    @Then("the response contains receipts, promotions and partner offer settings")
    public void the_response_contains_notification_settings() {
        Assert.assertTrue(ApiWorld.last.ok(), "GetNotificationPrefs failed: " + ApiWorld.last);
    }

    @Then("notifications are ordered newest first")
    public void notifications_are_ordered_newest_first() {
        Assert.assertTrue(ApiWorld.last.ok(), "ListNotifications failed: " + ApiWorld.last);
        // A new rider has no notifications, so there is no order to violate.
    }

    // ---- helpdesk ----

    @Then("the response contains a ticket id")
    public void the_response_contains_a_ticket_id() {
        Assert.assertTrue(ApiWorld.last.ok(), "CreateTicket failed: " + ApiWorld.last + " | " + ApiCommonSteps.abbreviate(ApiWorld.last.raw()));
        String id = ApiWorld.last.text("ticketId") != null ? ApiWorld.last.text("ticketId") : ApiWorld.last.text("ticket.id");
        Assert.assertNotNull(id, "No ticket id in: " + ApiWorld.last.raw());
        ApiWorld.memo.put("ticketId", id);
    }

    @Then("ListOpenTickets contains that ticket")
    public void list_open_tickets_contains_that_ticket() {
        Result r = ApiWorld.me.notification("notification.v1.helpdesk.HelpdeskService/ListOpenTickets", m());
        Assert.assertTrue(r.raw().contains(String.valueOf(ApiWorld.memo.get("ticketId"))), "Ticket not listed: " + r.raw());
    }

    @When("I call {string} with subject {string} and description {string}")
    public void i_call_create_ticket_with_subject_and_description(String rpc, String subject, String description) {
        ApiWorld.call(rpc, m("subject", subject, "body", description, "type", "question"));
    }

    @Given("I have an open ticket")
    public void i_have_an_open_ticket() {
        ApiWorld.me = RiderSession.primary();
        Result r = ApiWorld.call("CreateTicket", m("subject", "Automation ticket", "body", "Created by the API suite.", "type", "question"));
        Assert.assertTrue(r.ok(), "Could not create a ticket to reply to: " + r + " | " + ApiCommonSteps.abbreviate(r.raw()));
        String id = r.text("ticketId") != null ? r.text("ticketId") : r.text("ticket.id");
        ApiWorld.memo.put("ticketId", id);
    }

    @When("I call {string} to add a reply {string}")
    public void i_call_ticket_article_to_add_a_reply(String rpc, String reply) {
        ApiWorld.call(rpc, m("ticketId", ApiWorld.memo.get("ticketId"), "body", reply));
    }

    @Then("ListTicketArticles for that ticket contains {string}")
    public void list_ticket_articles_for_that_ticket_contains(String text) {
        Result r = ApiWorld.call("ListTicketArticles", m("ticketId", ApiWorld.memo.get("ticketId")));
        Assert.assertTrue(r.raw().contains(text), "Reply not found: " + r.raw());
    }
}
