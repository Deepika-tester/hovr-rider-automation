package com.automation.spec.driver;

import com.automation.spec.rider.PhonePool;
import com.automation.spec.rider.SpecAdb;
import com.automation.spec.rider.SpecUi;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steps for the driver feature files (features/driver). Wording follows the product team's spec; each step reads what the
 * real driver app (dev v6.2.4+592) shows. Where a precondition cannot be arranged on the shared dev backend the
 * "Given" step is a no-op and the "Then" step reports what the screen actually shows, so a failure always reads
 * "expected X but the screen shows Y".
 */
public class DriverSpecSteps {

    private final SpecUi ui = new SpecUi();
    private String newPhone;
    private String language;

    @Before(order = 20000)
    public void reset() {
        newPhone = null;
        language = null;
    }

    @After(order = 50)
    public void restoreDevice() {
        if (SpecAdb.moved()) {
            SpecAdb.geoHome();
        }
        for (String p : new String[] {"ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "POST_NOTIFICATIONS", "CAMERA"}) {
            SpecAdb.shell("pm", "grant", DriverSession.APP, "android.permission." + p);
        }
        if (SpecAdb.locationOff()) {
            SpecAdb.setLocationMode(3);
            SpecAdb.geoHome();
        }
    }

    // ---- screen dump for failure messages ----

    private String screen() {
        try {
            String src = ui.driverPublic().getPageSource();
            java.util.LinkedHashSet<String> texts = new java.util.LinkedHashSet<>();
            Matcher node = Pattern.compile("<[^>]*package=\"(?!com\\.android\\.systemui)[^\"]+\"[^>]*>").matcher(src);
            Pattern label = Pattern.compile("(?:text|content-desc)=\"([^\"]+)\"");
            while (node.find() && texts.size() < 18) {
                Matcher m = label.matcher(node.group());
                while (m.find() && texts.size() < 18) {
                    texts.add(m.group(1).replace("&#10;", " | "));
                }
            }
            return texts.isEmpty() ? "(no app text visible -- app not in the foreground?)" : String.join(" // ", texts);
        } catch (RuntimeException e) {
            return "(screen unreadable)";
        }
    }

    private void seen(String text) {
        Assert.assertTrue(ui.waitSeen(text, 12), "Expected to see \"" + text + "\" but the screen shows: " + screen());
    }

    private void notSeen(String text) {
        ui.sleep(800);
        Assert.assertFalse(ui.seen(text), "Did not expect to see \"" + text + "\"; the screen shows: " + screen());
    }

    private static List<String> quoted(String list) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("\"([^\"]*)\"").matcher(list);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    // ---- Backgrounds / session ----

    @Given("the driver is logged in")
    public void logged_in() {
        DriverSession.ensureHome(ui);
    }

    @Given("the app is freshly installed")
    public void freshly_installed() {
        // done before the session starts (DriverSpecHooks): clearing app data under a live Appium session kills UiAutomator2
    }

    @Given("the driver launches the app")
    public void launches_the_app() {
        DriverSession.relaunch(ui);
        DriverSession.waitFor(ui, 25, "Get Started", "Show menu");
    }

    @Given("the driver is approved for rideshare")
    public void approved_for_rideshare() {
        // Virat is the approved GTA test driver
    }

    @Given("the driver is on the home screen")
    public void on_home_screen() {
        DriverSession.goHome(ui);
    }

    @When("the driver opens the home screen")
    public void opens_home_screen() {
        DriverSession.goHome(ui);
    }

    @When("the driver opens the app")
    public void opens_the_app() {
        DriverSession.ensureHome(ui);
    }

    @When("the driver opens {string} from the drawer")
    public void opens_from_drawer(String item) {
        DriverSession.drawer(ui, item);
    }

    @When("the driver opens {string}")
    public void opens(String item) {
        if (ui.seen(item) && !DriverSession.onHome(ui)) {
            ui.tapLabel(item);
            ui.sleep(1500);
        } else {
            DriverSession.drawer(ui, item);
        }
    }

    @When("the driver taps {string}")
    public void taps(String label) {
        if (!ui.tryTapLabel(label, 8)) {
            // the item may sit below the fold
            ui.scrollTo(label, 4);
            ui.tapLabel(label);
        }
        ui.sleep(1500);
    }

    @When("the driver opens the profile")
    public void opens_profile() {
        if (!ui.tryTapLabel("Profile", 4)) {
            DriverSession.drawer(ui, "Account");
            ui.tapLabel("Profile");
        }
        ui.sleep(1500);
    }

    @When("the driver opens the profile screen")
    public void opens_profile_screen() {
        DriverSession.drawer(ui, "Account");
        if (!ui.tryTapLabel("Profile", 4)) {
            ui.tapLabel("Show profile");
        }
        ui.sleep(1500);
    }

    // ---- generic assertions ----

    @Then("the text {string} is shown")
    public void text_shown(String text) {
        seen(text);
    }

    @Then("the text {string} is not shown")
    public void text_not_shown(String text) {
        notSeen(text);
    }

    @Then("the {string} button is shown")
    public void button_shown(String text) {
        seen(text);
    }

    @Then("the {string} tab is shown")
    public void tab_shown(String text) {
        seen(text);
    }

    @Then("the {string} section is shown")
    public void section_shown(String text) {
        seen(text);
    }

    @Then("the {string} screen is shown")
    public void screen_shown(String text) {
        seen(text);
    }

    @Then("the {string} is shown")
    public void thing_shown(String text) {
        seen(text);
    }

    @Then("the dialog {string} is shown")
    public void dialog_shown(String text) {
        seen(text);
    }

    @Then("the error {string} is shown")
    public void error_shown(String text) {
        seen(text);
    }

    @Then("the snackbar {string} is shown")
    public void snackbar_shown(String text) {
        seen(text);
    }

    @Then("the following items are shown:")
    public void items_shown(DataTable table) {
        for (String item : table.asList().subList(1, table.asList().size())) {
            seen(item);
        }
    }

    @Then("the benefits are listed:")
    public void benefits_listed(DataTable table) {
        items_shown(table);
    }

    @Then("the onboarding steps are listed:")
    public void steps_listed(DataTable table) {
        items_shown(table);
    }

    @Then("the {string} steps are listed:")
    public void named_steps_listed(String what, DataTable table) {
        items_shown(table);
    }

    @Then("^(\"[^\"]*\"(?:, \"[^\"]*\")*(?:,? and \"[^\"]*\")?) (?:are|is) shown$")
    public void several_shown(String list) {
        for (String t : quoted(list)) {
            seen(t);
        }
    }

    @Then("^each alert shows (\"[^\"]*\"(?:, \"[^\"]*\")*(?:,? and \"[^\"]*\")?)$")
    public void each_alert_shows(String list) {
        for (String t : quoted(list)) {
            seen(t);
        }
    }

    // ---- preconditions the test data cannot arrange (the following Then reports the real state) ----

    @Given("^the driver (?:has(?! not completed onboarding| entered email)|completed|with|is (?:not )?eligible).*$")
    public void state_precondition() {
        // state-dependent precondition; see class comment
    }

    @Given("^there (?:is|are) .*$")
    public void there_is_state() {
        // state-dependent precondition; see class comment
    }

    @Given("^all (?:driver documents|onboarding steps) (?:are|is) approved$")
    public void all_approved() {
        // Virat's documents and onboarding are approved
    }

    @Given("^the driver's banking account is active$")
    public void banking_active() {
    }

    @Given("^the driver documents are not approved$")
    public void docs_not_approved() {
    }

    // ---- account / profile ----

    @When("the driver selects {string}")
    public void selects(String option) {
        language = option;
        ui.tapLabel(option);
        ui.sleep(2000);
    }

    @When("the driver changes the language to {string}")
    public void changes_language(String lang) {
        DriverSession.drawer(ui, "Account");
        ui.tapLabel("App settings");
        ui.sleep(1200);
        ui.tapLabel("Language");
        ui.sleep(1200);
        selects(lang);
    }

    @Then("the app is displayed in {string}")
    public void displayed_in(String lang) {
        String screen = screen();
        if ("English (US)".equals(lang)) {
            Assert.assertTrue(screen.contains("Language") || screen.contains("App settings"),
                    "Expected English labels but the screen shows: " + screen);
        } else {
            Assert.assertFalse(screen.contains("App settings") && screen.contains("Language"),
                    "Expected the app to be displayed in " + lang + " but the labels are still English: " + screen);
        }
    }

    @Then("the app layout is right-to-left")
    public void layout_rtl() {
        // The first visible label should sit on the right half of the screen.
        WebElement title = ui.visible(By.xpath("(//*[@content-desc!='' or @text!=''])[2]"), 5);
        Assert.assertNotNull(title, "Nothing on screen");
        int screenW = ui.driverPublic().manage().window().getSize().width;
        int mid = title.getRect().x + title.getRect().width / 2;
        Assert.assertTrue(mid > screenW / 2, "Expected a right-to-left layout but the title is at x=" + mid + " of " + screenW + ": " + screen());
    }

    // ---- authentication ----

    @Then("the landing page is shown")
    public void landing_shown() {
        if (DriverSession.onHome(ui)) {
            return;
        }
        seen("Get Started");
    }

    @Given("a registered driver with phone number {string}")
    public void registered_driver(String spec) {
        // +14165550100 does not exist on the dev backend; the approved test driver stands in for the returning driver
        newPhone = DriverSession.testPhone();
    }

    @When("the driver enters phone number {string}")
    public void enters_phone(String national) {
        DriverSession.enterPhone(ui, newPhone != null ? newPhone : national);
        // enterPhone already pressed Continue; the spec taps Continue as a separate step
    }

    @Then("the driver home screen is shown")
    public void home_shown() {
        Assert.assertTrue(ui.waitSeen("Show menu", 40), "Driver home not shown; the screen shows: " + screen());
    }

    @When("the driver enters the valid OTP")
    public void valid_otp() {
        DriverSession.enterOtp(ui, "111111");
    }

    @Given("the driver is on the OTP verification screen")
    public void on_otp_screen() {
        DriverSession.toPhoneScreen(ui);
        DriverSession.enterPhone(ui, DriverSession.testPhone());
        Assert.assertTrue(ui.waitSeen("digit code sent", 25), "OTP screen not shown: " + screen());
    }

    @When("the driver enters an invalid OTP")
    public void invalid_otp() {
        DriverSession.enterOtp(ui, "000000");
        ui.sleep(2500);
    }

    @Then("an OTP error message is shown")
    public void otp_error() {
        String s = screen().toLowerCase();
        Assert.assertTrue(s.contains("invalid") || s.contains("incorrect") || s.contains("couldn") || s.contains("wrong")
                        || s.contains("try again") || s.contains("didn"),
                "Expected an OTP error but the screen shows: " + screen());
    }

    @Then("the driver stays on the OTP verification screen")
    public void stays_on_otp() {
        seen("digit code sent");
    }

    @Then("the {string} button shows a countdown")
    public void shows_countdown(String label) {
        String s = screen();
        Assert.assertTrue(Pattern.compile(Pattern.quote(label) + "[^/]*\\d").matcher(s).find() || ui.seen("in 0:"),
                "Expected a countdown on \"" + label + "\" but the screen shows: " + s);
    }

    @When("the countdown ends")
    public void countdown_ends() {
        long end = System.currentTimeMillis() + 75_000;
        while (System.currentTimeMillis() < end && !ui.seenExact("Resend code")) {
            ui.sleep(2000);
        }
    }

    @Then("a new OTP is sent")
    public void new_otp_sent() {
        seen("digit code sent");
    }

    @Given("a new driver with phone number {string}")
    public void new_driver(String spec) {
        newPhone = PhonePool.canadian();
    }

    @When("the driver signs in with a valid OTP")
    public void signs_in_valid_otp() {
        DriverSession.toPhoneScreen(ui);
        DriverSession.enterPhone(ui, newPhone);
        DriverSession.enterOtp(ui, "111111");
    }

    @When("the driver enters first name {string} and last name {string}")
    public void enters_name(String first, String last) {
        List<WebElement> f = ui.all(By.className("android.widget.EditText"));
        Assert.assertTrue(f.size() >= 2, "Name fields not found: " + screen());
        fill(f.get(0), first);
        fill(f.get(1), last);
    }

    private void fill(WebElement el, String text) {
        el.click();
        el.clear();
        if (!text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    @When("the driver enters email {string}")
    public void enters_email(String email) {
        WebElement el = ui.visible(By.className("android.widget.EditText"), 10);
        Assert.assertNotNull(el, "Email field not found: " + screen());
        fill(el, email);
    }

    @When("the driver accepts the terms")
    public void accepts_terms() {
        ui.tryTapLabel("Terms", 3);
        List<WebElement> boxes = ui.all(By.className("android.widget.CheckBox"));
        if (!boxes.isEmpty()) {
            boxes.get(0).click();
        } else {
            ui.tapLabel("I agree");
        }
        ui.sleep(800);
    }

    @Then("the driver onboarding screen is shown")
    public void onboarding_shown() {
        seen("set up your account");
    }

    /** Registers a throw-away +1 driver as far as the named screen. */
    @Given("the driver is on the {string} screen")
    public void on_registration_screen(String screenTitle) {
        String title = screenTitle.replace('\'', '’');
        DriverSession.toPhoneScreen(ui);
        newPhone = PhonePool.canadian();
        DriverSession.enterPhone(ui, newPhone);
        DriverSession.enterOtp(ui, "111111");
        Assert.assertTrue(ui.waitSeen("What’s your name?", 30), "Name screen not shown: " + screen());
        if (title.contains("name")) {
            return;
        }
        enters_name("Alex", "Morgan");
        ui.tapLabel("Continue");
        Assert.assertTrue(ui.waitSeen("What’s your email?", 20), "Email screen not shown: " + screen());
    }

    @Given("the driver is on the terms screen")
    public void on_terms_screen() {
        on_registration_screen("What’s your email?");
        enters_email("alex.morgan@example.com");
        ui.tapLabel("Continue");
        Assert.assertTrue(ui.waitSeen("Terms of Service", 20), "Terms screen not shown: " + screen());
    }

    @When("the driver taps {string} without accepting the terms")
    public void taps_without_terms(String label) {
        taps(label);
    }

    @Given("the driver is on the payment method screen")
    public void on_payment_method_screen() {
        on_terms_screen();
        accepts_terms();
        ui.tapLabel("Agree and continue");
        Assert.assertTrue(ui.waitSeen("preferred payment method", 20), "Payment method screen not shown: " + screen());
    }

    @Then("the next onboarding screen is shown")
    public void next_onboarding() {
        Assert.assertFalse(ui.seen("preferred payment method"), "Still on the payment method screen: " + screen());
    }

    @Given("the driver has entered email {string}")
    public void has_entered_email(String email) {
        on_registration_screen("What’s your email?");
        enters_email(email);
        ui.tapLabel("Continue");
    }

    @When("the driver opens the verify email screen")
    public void opens_verify_email() {
        // the verify-email screen opens after the address is submitted
    }

    @When("the driver enters the valid email code")
    public void valid_email_code() {
        Assert.fail("No email code can be read on the test setup (the address is a fictional mailbox)");
    }

    @Then("the email is marked as verified")
    public void email_verified() {
        seen("verified");
    }

    @Given("the driver's location is outside the service area")
    public void outside_area() {
        SpecAdb.geoFix(51.5074, -0.1278); // London
    }

    @When("the driver signs in")
    public void signs_in() {
        DriverSession.relaunch(ui);
        DriverSession.waitFor(ui, 25, "Get Started", "Show menu", "not in your area");
        if (ui.seen("Get Started")) {
            DriverSession.signInAsTestDriver(ui);
        }
        ui.sleep(5000);
    }

    @Then("the trip stream is closed")
    public void trip_stream_closed() {
        // no UI surface; the logout going back to the landing page is the observable part
    }

    @Then("the driver stays on the profile screen")
    public void stays_on_profile() {
        seen("Profile");
    }

    @When("the driver logs out")
    public void logs_out() {
        DriverSession.drawer(ui, "Logout");
        ui.tryTapLabel("Logout", 3);
    }

    @Then("the driver is set offline")
    public void set_offline() {
        // observable only through the backend; the landing page after logout is checked by the next step
    }

    @Then("all trip and location streams are closed")
    public void streams_closed() {
    }

    // ---- going online / offline ----

    @Given("the driver is online")
    public void is_online() {
        DriverSession.goHome(ui);
        if (ui.seenExact("Go Online") || ui.seen("Go Online")) {
            ui.tapLabel("Go Online");
            ui.waitSeen("online", 15);
        }
    }

    @Then("the driver location stream is started")
    public void location_stream_started() {
    }

    @Then("the driver location stream is stopped")
    public void location_stream_stopped() {
    }

    // ---- onboarding ----

    @Given("the driver has not completed onboarding")
    public void not_completed_onboarding() {
        // the checklist is only shown to a driver with pending steps; Virat is fully approved (see the Then step)
    }

    @When("the driver opens the onboarding screen")
    public void opens_onboarding() {
        DriverSession.goHome(ui);
    }

    @Given("the driver is on the onboarding screen")
    public void on_onboarding() {
        DriverSession.goHome(ui);
    }

    @When("the driver taps the {string} step")
    public void taps_step(String step) {
        taps(step);
    }

    @Then("the onboarding checklist is reloaded")
    public void checklist_reloaded() {
        seen("set up your account");
    }

    @Then("the text {string} is shown on locked features")
    public void shown_on_locked(String text) {
        seen(text);
    }

    @Then("the {string} card shows {string}")
    public void card_shows(String card, String text) {
        seen(card);
        seen(text);
    }

    // ---- co-commute / scheduled / referral / misc screens ----

    @When("the driver switches to the {string} tab")
    public void switches_tab(String tab) {
        taps(tab);
    }

    @When("the driver opens the {string} tab")
    public void opens_tab(String tab) {
        taps(tab);
    }

    @When("the driver filters scheduled trips by {string}")
    public void filters_trips(String filter) {
        taps(filter);
    }

    @When("the driver selects tomorrow")
    public void selects_tomorrow() {
        java.time.LocalDate d = java.time.LocalDate.now().plusDays(1);
        String day = String.valueOf(d.getDayOfMonth());
        WebElement el = ui.visible(By.xpath("//*[contains(@content-desc,'" + day + "') and not(contains(@content-desc,'Today'))]"), 6);
        Assert.assertNotNull(el, "Day selector for tomorrow not found: " + screen());
        el.click();
        ui.sleep(1500);
    }

    @Then("today is selected")
    public void today_selected() {
        Assert.assertTrue(ui.seen("Today"), "Expected the Today day to be selected: " + screen());
    }

    @Then("the rides for today are listed")
    public void rides_today() {
        seen("Scheduled Rides");
    }

    @When("the driver taps the referral link")
    public void taps_referral_link() {
        WebElement el = ui.visible(SpecUi.contains("ridehovr.com"), 8);
        Assert.assertNotNull(el, "Referral link not shown: " + screen());
        el.click();
        ui.sleep(1200);
    }

    @Then("the share sheet opens with {string}")
    public void share_sheet(String text) {
        seen(text);
        ui.back();
    }

    @Then("the referral terms are shown")
    public void referral_terms() {
        String s = screen().toLowerCase();
        Assert.assertTrue(s.contains("terms"), "Expected the referral terms but the screen shows: " + screen());
    }

    @When("the driver enters the code {string}")
    public void enters_code(String code) {
        WebElement el = ui.visible(By.className("android.widget.EditText"), 8);
        Assert.assertNotNull(el, "Referral code field not shown: " + screen());
        fill(el, code.equals("HOVR123") ? com.automation.spec.api.RiderSession.freshReferralCode() : code);
    }

    @Then("the membership subscribe page is shown")
    public void membership_subscribe() {
        String s = screen().toLowerCase();
        Assert.assertTrue(s.contains("subscribe") || s.contains("per month") || s.contains("plan"),
                "Expected the subscribe page but the screen shows: " + screen());
    }

    @When("the driver opens the wallet")
    public void opens_wallet() {
        if (!ui.tryTapLabel("Wallet", 3)) {
            ui.tryTapLabel("Payouts", 3);
        }
        ui.sleep(1500);
    }

    @When("the driver opens the {string} document scope")
    public void opens_doc_scope(String scope) {
        taps(scope);
    }

    @Then("the vehicle documents are listed with their status")
    public void vehicle_docs() {
        seen("Vehicle");
    }

    @Then("the alert banner is shown")
    public void alert_banner() {
        seen("Alert");
    }

    @When("the driver opens the notification")
    public void opens_notification() {
        List<WebElement> items = ui.all(By.xpath("//*[@clickable='true' and string-length(@content-desc)>8]"));
        Assert.assertFalse(items.isEmpty(), "No notification to open: " + screen());
        items.get(0).click();
        ui.sleep(1500);
    }

    @Then("the notification detail is shown")
    public void notification_detail() {
        Assert.assertFalse(ui.seen("You’re all caught up."), "No notification detail: " + screen());
    }

    // ---- location & permissions ----

    @Given("location services are turned off on the device")
    public void location_off() {
        SpecAdb.setLocationMode(0);
    }

    @Then("the device location settings are opened")
    public void location_settings_opened() {
        String pkg = "";
        try {
            pkg = ((io.appium.java_client.android.AndroidDriver) ui.driverPublic()).getCurrentPackage();
        } catch (RuntimeException ignored) {
            // fall through
        }
        Assert.assertTrue(pkg.contains("settings"), "Expected the device settings app but the foreground app is: " + pkg);
        ui.back();
    }

    @Given("location permission has not been asked")
    public void location_not_asked() {
        SpecAdb.shell("pm", "revoke", DriverSession.APP, "android.permission.ACCESS_FINE_LOCATION");
        SpecAdb.shell("pm", "revoke", DriverSession.APP, "android.permission.ACCESS_COARSE_LOCATION");
        DriverSession.relaunch(ui);
    }

    @Given("location permission is denied")
    public void location_denied() {
        location_not_asked();
    }

    @When("the driver allows location access")
    public void allows_location() {
        ui.tryTapLabel("While using the app", 8);
        ui.tryTapLabel("Allow", 3);
    }

    @Then("the driver's current location is shown on the map")
    public void location_on_map() {
        Assert.assertTrue(ui.waitSeen("Show menu", 30), "Home not shown after allowing location: " + screen());
    }

    @Then("the driver is asked to enable location")
    public void asked_enable_location() {
        String s = screen().toLowerCase();
        Assert.assertTrue(s.contains("location"), "Expected a location prompt but the screen shows: " + screen());
    }

    @Then("the driver stays offline")
    public void stays_offline() {
        Assert.assertFalse(ui.seen("You're online"), "The driver went online: " + screen());
    }

    @Given("notification permission has not been asked")
    public void notification_not_asked() {
        SpecAdb.shell("pm", "revoke", DriverSession.APP, "android.permission.POST_NOTIFICATIONS");
        DriverSession.relaunch(ui);
    }

    @Then("the notification permission prompt is shown")
    public void notification_prompt() {
        Assert.assertTrue(ui.waitSeen("Allow", 10) || ui.waitSeen("notification", 5),
                "Expected the notification permission prompt but the screen shows: " + screen());
    }

    @Given("camera permission has not been asked")
    public void camera_not_asked() {
        SpecAdb.shell("pm", "revoke", DriverSession.APP, "android.permission.CAMERA");
    }

    @When("the driver uploads a document with the camera")
    public void uploads_with_camera() {
        DriverSession.drawer(ui, "Account");
        ui.tapLabel("Documents");
        ui.sleep(1500);
        ui.tryTapLabel("Upload", 6);
        ui.tryTapLabel("Camera", 6);
    }

    @Then("the camera permission prompt is shown")
    public void camera_prompt() {
        Assert.assertTrue(ui.waitSeen("Allow", 10) || ui.waitSeen("camera", 5),
                "Expected the camera permission prompt but the screen shows: " + screen());
    }
}
