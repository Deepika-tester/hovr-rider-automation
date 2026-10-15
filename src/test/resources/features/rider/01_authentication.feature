@auth
Feature: Rider authentication and onboarding
  As a new or returning rider
  I want to sign in with my phone number
  So that I can book rides with HOVR

  Background:
    Given the app is freshly installed
    And the device has an internet connection
    And I launch the HOVR Rider app

  @smoke
  Scenario: Splash screen leads to the landing page
    When the splash screen finishes loading
    Then I should see the landing screen
    And I should see the "Get Started" button

  Scenario: Navigate from landing to phone number entry
    Given I am on the landing screen
    When I tap "Get Started"
    Then I should see "Enter your phone number"
    And I should see "We’ll text you a code to verify it."

  Scenario Outline: Phone number validation
    Given I am on the phone number screen
    When I enter phone number "<phone>"
    And I tap "Continue"
    Then I should see "<result>"

    Examples:
      | phone        | result                 |
      | 4165550123   | OTP verification page  |
      | 12345        | Invalid phone number.  |
      |              | Invalid phone number.  |

  @smoke
  Scenario: Verify OTP successfully
    Given I submitted a valid phone number "4165550123"
    And I am on the OTP verification page
    Then I should see "Enter the 6-digit code sent to you at +1 4165550123."
    When I enter the valid 6-digit code
    Then I should be signed in

  Scenario: Enter an invalid OTP
    Given I am on the OTP verification page
    When I enter the code "000000"
    Then I should see "Invalid verification code. Please try again."

  Scenario: Resend code countdown
    Given I am on the OTP verification page
    Then the "Resend code" button shows a countdown timer
    When the countdown reaches zero
    And I tap "Resend code"
    Then a new code is sent
    And the countdown restarts

  # BLOCKED: needs the daily OTP request limit exhausted (dozens of real SMS requests); not triggered on the shared dev backend.
  @blocked
  Scenario: Too many OTP requests
    Given I have requested OTP the maximum number of times today
    When I tap "Resend code"
    Then I should see "Too many OTP requests for today. Try again later."

  Scenario: Change phone number from OTP screen
    Given I am on the OTP verification page
    When I tap "I have a new phone number"
    Then I should return to the phone number screen

  Scenario: New rider completes profile
    Given I verified OTP as a new rider
    When I enter first name "Jane" and last name "Doe"
    And I tap "Continue"
    Then I should see "What’s your email?"
    When I enter email "jane@example.com"
    And I tap "Continue"
    Then I should see the Terms of Service screen

  Scenario Outline: Name validation
    Given I am on the name entry screen
    When I enter first name "<first>" and last name "<last>"
    And I tap "Continue"
    Then I should see "<message>"

    Examples:
      | first | last | message                  |
      |       | Doe  | Enter your first name    |
      | Jane  |      | Enter your last name     |
      | J@ne1 | Doe  | First name is not valid  |

  Scenario: Empty email is rejected
    Given I am on the email entry screen
    When I tap "Continue" without entering an email
    Then I should see "Email field is empty"

  Scenario: Terms must be accepted
    Given I am on the Terms of Service screen
    When I tap "Agree and continue" without checking the agreement box
    Then I should see "You must agree to the Terms of Service to continue."
    When I check the agreement box
    And I tap "Agree and continue"
    Then I should see "Do you have a referral code?"

  Scenario: Skip referral code during onboarding
    Given I am on the referral code onboarding screen
    When I tap "Skip"
    Then I should see "Welcome to HOVR"
    And I should see "Ride now" and "I’ll ride later"

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @india-only-skip
  Scenario: Redeem referral code during onboarding
    Given I am on the referral code onboarding screen
    When I enter referral code "FRIEND5"
    And I tap "Redeem Code"
    Then I should see "Referral code redeemed successfully!"

  Scenario: Rider outside service region
    Given my current location is outside the HOVR service area
    When I finish onboarding
    Then I should see "We’re not in your area... yet."
    When I tap "Get Updates"
    Then I am subscribed to launch notifications

  @no-notifications
  Scenario: Notification permission prompt
    Given I completed onboarding
    And notification permission has not been granted
    Then I should see the notification permission screen
    When I deny the permission
    Then I should see "You have not allowed notification permission."
    And I should see "Open Settings"
