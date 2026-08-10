package com.automation.pages;

import org.openqa.selenium.By;

/**
 * Landing -> PhoneNumber -> OTP -> Name -> Email -> Referral -> Terms -> Payment -> Welcome
 * See BasePage javadoc re: Flutter accessibility-label locators — verify all "VERIFY:" labels
 * against the live app with Appium Inspector before relying on this in CI.
 */
class LandingPage extends BasePage {
    // VERIFY: confirm the actual Semantics label Flutter assigns this button
    private final By getStartedButton = byText("Get Started");

    public void tapGetStarted() {
        tap(getStartedButton);
    }
}

class PhoneNumberPage extends BasePage {
    private final By countryCodeSelector = byAccessibilityId("country_code_selector"); // VERIFY
    private final By phoneNumberField = byAccessibilityId("phone_number_field");        // VERIFY
    private final By continueButton = byText("Continue");

    public void selectCountryCode(String code) {
        tap(countryCodeSelector);
        tapText(code); // assumes a picker list item matching the code text, e.g. "+1"
    }

    public void enterPhoneNumber(String phoneNumber) {
        typeInto(phoneNumberField, phoneNumber);
    }

    public void tapContinue() {
        tap(continueButton);
    }

    public boolean isDisplayedScreen() {
        return isDisplayed(phoneNumberField);
    }
}

class OtpVerificationPage extends BasePage {
    private final By otpField = byAccessibilityId("otp_input_field"); // VERIFY - may be 6 separate digit fields
    private final By resendCodeButton = byText("Resend Code");
    private final By errorMessage = byAccessibilityId("otp_error_message"); // VERIFY

    public void enterOtp(String code) {
        typeInto(otpField, code);
    }

    public void tapResendCode() {
        tap(resendCodeButton);
    }

    public boolean isDisplayedScreen() {
        return isDisplayed(otpField);
    }

    public String getErrorText() {
        return waitFor(errorMessage).getText();
    }
}

class NameEntryPage extends BasePage {
    private final By firstNameField = byAccessibilityId("first_name_field"); // VERIFY
    private final By lastNameField = byAccessibilityId("last_name_field");   // VERIFY
    private final By continueButton = byText("Continue");

    public void enterFirstName(String firstName) {
        typeInto(firstNameField, firstName);
    }

    public void enterLastName(String lastName) {
        typeInto(lastNameField, lastName);
    }

    public void clearFirstName() {
        waitFor(firstNameField).clear();
    }

    public void tapContinue() {
        tap(continueButton);
    }
}

class EmailEntryPage extends BasePage {
    private final By emailField = byAccessibilityId("email_field"); // VERIFY
    private final By continueButton = byText("Continue");

    public void enterEmail(String email) {
        typeInto(emailField, email);
    }

    public void tapContinue() {
        tap(continueButton);
    }
}

class ReferralCodePage extends BasePage {
    private final By referralField = byAccessibilityId("referral_code_field"); // VERIFY
    private final By applyButton = byText("Apply");
    private final By skipButton = byText("Skip");

    public void enterReferralCode(String code) {
        typeInto(referralField, code);
    }

    public void tapApply() {
        tap(applyButton);
    }

    public void tapSkip() {
        tap(skipButton);
    }
}

class TermsAndPolicyPage extends BasePage {
    private final By acceptButton = byText("I Accept");
    private final By declineButton = byText("Decline");

    public void tapAccept() {
        tap(acceptButton);
    }

    public void tapDecline() {
        tap(declineButton);
    }
}

class PaymentMethodSelectorPage extends BasePage {
    private final By addCardOption = byText("Add Credit/Debit Card");
    private final By skipForNowButton = byText("Skip for now");
    private final By saveButton = byText("Save");

    public void selectAddCard() {
        tap(addCardOption);
    }

    public void tapSkipForNow() {
        tap(skipForNowButton);
    }

    public void tapSave() {
        tap(saveButton);
    }
}

class WelcomePage extends BasePage {
    private final By welcomeHeading = byTextContains("Welcome to HOVR");

    public boolean isDisplayedScreen() {
        return isDisplayed(welcomeHeading);
    }
}