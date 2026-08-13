@rider @registration @auth
Feature: Rider Registration
  As a new user
  I want to create a rider account on HOVR
  So that I can book rides in supported Canadian markets


  Background:
    Given the HOVR rider app is installed and launched
    And the app has loaded the splash screen
      # --- PHONE NUMBER ENTRY ---
  Scenario: Navigate from landing page to phone number entry
    Given I am on the landing page
    When I tap the "Get Started" button
    Then I should see the phone number entry screen

  Scenario: Enter a valid Canadian phone number
    Given I am on the phone number entry screen
    When I select country code "+1"
    And I enter phone number "4165551234"
    And I tap "Continue"
    Then an OTP should be requested via the IAM service
    And I should be navigated to the OTP verification screen

  # CONFIRMED 2026-08-13: the country code selector can't actually be cleared/removed on the
  # real app — it's always locked to a selection (+1 by default), so "no country code selected"
  # isn't a reachable state. Reinterpreted as the closest real equivalent: leaving the number
  # field itself empty (was "Enter phone number without country code" / "validation error for
  # missing country code").
  Scenario: Leave the phone number field empty
    Given I am on the phone number entry screen
    When I tap "Continue" without entering a phone number
    Then I should see a validation error "enter your phone number"

  Scenario: Enter an invalid phone number format
    Given I am on the phone number entry screen
    When I select country code "+1"
    And I enter phone number "123"
    And I tap "Continue"
    Then I should see a validation error "couldn't use this number"

  Scenario: Enter phone number from unsupported region
    Given I am on the phone number entry screen
    When I select country code "+1"
    And I enter a phone number registered to an unsupported region
    And I tap "Continue"
    Then I should be navigated to the "Not Available In Your Region" screen
    And I should see a message indicating HOVR is not yet available in my area

  # --- OTP VERIFICATION ---

  Scenario: Verify with correct OTP
    Given I am on the OTP verification screen
    And an OTP has been sent to my phone "+14165551234"
    When I enter the correct 6-digit OTP code
    Then the OTP should be verified successfully via the IAM service
    And I should be navigated to the profile setup flow

  Scenario: Verify with incorrect OTP
    Given I am on the OTP verification screen
    When I enter an incorrect OTP code "000000"
    Then I should see an error message "Invalid verification code"
    And I should remain on the OTP verification screen

  Scenario: Resend OTP code
    Given I am on the OTP verification screen
    When I tap "Resend Code"
    Then a new OTP should be sent to my phone number
    And I should see a confirmation that the code was resent

  Scenario: OTP code expires
    Given I am on the OTP verification screen
    And the OTP was sent more than 5 minutes ago
    When I enter the expired OTP code
    Then I should see an error message indicating the code has expired
    And I should be prompted to request a new code

  # --- PROFILE SETUP ---

  Scenario: Enter rider first and last name
    Given I have verified my phone number
    And I am on the rider name entry screen
    When I enter first name "kerry"
    And I enter last name "kim"
    And I tap "Continue"
    Then I should be navigated to the email entry screen

  Scenario: Enter name with empty first name
    Given I am on the rider name entry screen
    When I leave the first name field empty
    And I enter last name "kim"
    And I tap "Continue"
    Then I should see a validation error for the first name field

  Scenario: Enter rider email address
    Given I am on the rider email entry screen
    When I enter email "kerry.kim@abcd.com"
    And I tap "Continue"
    Then I should be navigated to the referral code screen

  Scenario: Enter invalid email format
    Given I am on the rider email entry screen
    When I enter email "not-an-email"
    And I tap "Continue"
    Then I should see a validation error "Please enter a valid email address"

  # --- REFERRAL CODE ---

  Scenario: Skip referral code entry
    Given I am on the "Do you have a referral code?" screen
    When I tap "Skip" or "No"
    Then I should be navigated to the terms and policy screen

  Scenario: Enter a valid referral code
    Given I am on the "Do you have a referral code?" screen
    When I enter referral code "HOVR2026"
    And I tap "Redeem Code"
    Then the referral code should be validated
    And I should see a success confirmation
    And I should be navigated to the terms and policy screen

  Scenario: Enter an invalid referral code
    Given I am on the "Do you have a referral code?" screen
    When I enter referral code "INVALID123"
    And I tap "Redeem Code"
    Then I should see an error "Invalid referral code"

  # --- TERMS AND POLICY ---

  Scenario: Accept terms and conditions
    Given I am on the terms and policy screen
    When I review the terms of service
    And I tap "Agree and continue"
    Then I should be navigated to the payment method screen

  Scenario: Decline terms and conditions
    Given I am on the terms and policy screen
    When I tap "Decline" or navigate back
    Then I should not be able to proceed with registration

  # --- PAYMENT METHOD (DURING REGISTRATION) ---

  Scenario: Add payment method during registration
    Given I am on the payment method selector screen during registration
    When I select "Credit or debit card"
    And I enter valid card details
    And I tap "Save"
    Then the payment method should be saved via Adyen
    And I should be navigated to the welcome screen

  Scenario: Skip payment method during registration
    Given I am on the payment method selector screen during registration
    When I tap "Skip"
    Then I should be navigated to the welcome screen
    And my account should be created without a payment method

  # --- COMPLETE REGISTRATION ---

  Scenario: Complete full registration flow
    Given I am on the welcome screen
    Then I should see "Welcome to HOVR"
    And my rider profile should be created with:
      | field          | value                  |
      | firstName      | Dippi                   |
      | lastName       | panwar                    |
      | email          | panwar.d@gmail.com   |
      | phone          | +1709-866-7507           |
    And I should be navigated to the ride home page

  Scenario: Returning user login
    Given I have an existing rider account with phone "+14165551234"
    When I enter my phone number and verify with OTP
    Then I should be authenticated
    And I should be navigated directly to the ride home page
    And I should not see the profile setup flow

  # --- EDGE CASES ---

  Scenario: Registration with network timeout during OTP request
    Given I am on the phone number entry screen
    And there is no network connectivity
    When I enter a valid phone number and tap "Continue"
    Then I should see a network error message
    And I should be able to retry when connectivity is restored

  Scenario: App killed during registration flow
    Given I have completed OTP verification
    And I am on the name entry screen
    When the app is killed and relaunched
    Then I should be able to resume registration from where I left off

  Scenario: Duplicate phone number registration
    Given a rider account already exists for phone "+14165551234"
    When a new user enters phone "+14165551234" and verifies OTP
    Then they should be logged into the existing account
    And they should not see the registration flow
