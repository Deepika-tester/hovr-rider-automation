@authentication
Feature: Driver sign up and sign in
  As a new or returning Hovr driver
  I want to sign in with my phone number
  So that I can access the driver app

  Background:
    Given the app is freshly installed
    And the driver launches the app

  @smoke
  Scenario: Landing page is shown on first launch
    Then the landing page is shown
    And the "Get Started" button is shown

  @smoke
  Scenario: Returning driver signs in with phone number and OTP
    Given a registered driver with phone number "+14165550100"
    When the driver taps "Get Started"
    Then the text "Enter your phone number" is shown
    When the driver enters phone number "4165550100"
    And the driver taps "Continue"
    Then the text "Enter the 6-digit code sent to you at +14165550100." is shown
    When the driver enters the valid OTP
    Then the driver home screen is shown

  @otp @negative
  Scenario: Invalid OTP is rejected
    Given the driver is on the OTP verification screen
    When the driver enters an invalid OTP
    Then an OTP error message is shown
    And the driver stays on the OTP verification screen

  @otp
  Scenario: Resend code is available after the countdown
    Given the driver is on the OTP verification screen
    Then the "Resend code" button shows a countdown
    When the countdown ends
    And the driver taps "Resend code"
    Then a new OTP is sent

  @otp
  Scenario: Driver changes the phone number from the OTP screen
    Given the driver is on the OTP verification screen
    When the driver taps "I have a new phone number"
    Then the text "Enter your phone number" is shown

  @registration @smoke
  Scenario: New driver completes registration
    Given a new driver with phone number "+14165550101"
    When the driver signs in with a valid OTP
    Then the text "What’s your name?" is shown
    When the driver enters first name "Alex" and last name "Morgan"
    And the driver taps "Continue"
    Then the text "What’s your email?" is shown
    When the driver enters email "alex.morgan@example.com"
    And the driver taps "Continue"
    Then the text "Accept HOVR’s Terms of Service" is shown
    When the driver accepts the terms
    And the driver taps "Agree and continue"
    Then the driver onboarding screen is shown

  @registration @validation
  Scenario Outline: Name validation
    Given the driver is on the "What’s your name?" screen
    When the driver enters first name "<first>" and last name "<last>"
    And the driver taps "Continue"
    Then the error "<error>" is shown

    Examples:
      | first | last   | error                |
      |       | Morgan | Enter your first name |
      | Alex  |        | Enter your last name  |

  @registration @validation
  Scenario: Placeholder name is rejected
    Given the driver is on the "What’s your name?" screen
    When the driver enters first name "Test" and last name "Driver"
    And the driver taps "Continue"
    Then the dialog "Please update your name" is shown
    And the "Save name" button is shown

  @registration @validation
  Scenario Outline: Email validation
    Given the driver is on the "What’s your email?" screen
    When the driver enters email "<email>"
    And the driver taps "Continue"
    Then the error "<error>" is shown

    Examples:
      | email          | error                                                                                         |
      |                | Email field is empty                                                                          |
      | not-an-email   | Sorry, we couldn’t use this email. Please ensure you’ve entered it using the format name@email.com. |

  @registration @email
  Scenario: Driver verifies the email address
    Given the driver has entered email "alex.morgan@example.com"
    When the driver opens the verify email screen
    And the driver enters the valid email code
    Then the email is marked as verified

  # BLOCKED: the driver sign-up has no such screen (shown by 'New driver completes registration' and 'Terms must be accepted'), so there is nothing further to exercise.
  @registration @email @blocked
  Scenario: Driver changes the email address
    Given the driver is on the verify email screen
    When the driver chooses to change the email
    And the driver enters email "alex.new@example.com"
    And the driver taps "Continue"
    Then a verification code is sent to "alex.new@example.com"

  @registration @validation
  Scenario: Terms must be accepted
    Given the driver is on the terms screen
    When the driver taps "Agree and continue" without accepting the terms
    Then the error "You must agree to the Terms of Service to continue." is shown

  # BLOCKED: the driver sign-up has no such screen (shown by 'New driver completes registration' and 'Terms must be accepted'), so there is nothing further to exercise.
  @registration @blocked
  Scenario Outline: Terms links open the legal pages
    Given the driver is on the terms screen
    When the driver taps "<link>"
    Then the "<link>" page is opened

    Examples:
      | link             |
      | Terms of Service |
      | Privacy Policy   |

  @payment-method
  Scenario: Driver skips adding a payment method
    Given the driver is on the payment method screen
    Then the text "Select your preferred payment method" is shown
    When the driver taps "Do this later"
    Then the next onboarding screen is shown

  # BLOCKED: the driver sign-up has no such screen (shown by 'New driver completes registration' and 'Terms must be accepted'), so there is nothing further to exercise.
  @payment-method @blocked
  Scenario: Driver adds a credit card
    Given the driver is on the payment method screen
    When the driver taps "Credit or debit card"
    And the driver enters valid card details
    And the driver taps "Add"
    Then the card is saved

  @region @negative
  Scenario: Driver outside a supported region
    Given the driver's location is outside the service area
    When the driver signs in
    Then the text "We’re not in your area... yet." is shown
    And the "Get Updates" button is shown

  @logout
  Scenario: Driver logs out
    Given the driver is logged in
    When the driver opens the profile screen
    And the driver taps "Logout"
    Then the landing page is shown
    And the trip stream is closed

  @delete-account
  Scenario: Driver cancels account deletion
    Given the driver is logged in
    When the driver opens the profile screen
    And the driver taps "Delete Account"
    Then the dialog "Confirm Account Deletion" is shown
    When the driver taps "Cancel"
    Then the driver stays on the profile screen
