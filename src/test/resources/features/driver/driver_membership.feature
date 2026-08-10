@driver @membership
Feature: Driver Membership Management
  As a HOVR driver
  I want to manage my $20/month membership
  So that I can keep 100% of my ride fares

  # Derived from: hovr-driver-app/lib/features/membership-feature/
  # Events: GetPaymentURLEvent (generates Adyen payment URL for subscription)
  # Pages: membership_screen.dart, manage_membership_screen.dart,
  #         membership_initial_screen.dart, membership_benefits.dart,
  #         membership_subscribe_page.dart, in_app_webview.dart
  # Model: $20 CAD/month flat fee, driver keeps 100% of fare
  # Note: Many membership events are commented out (Create, Cancel, Reactivate, Get, Change, List)
  #       indicating the feature is partially implemented via payment URL webview

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app

  # --- VIEW MEMBERSHIP ---

  Scenario: View membership benefits before subscribing
    Given I do not have an active membership
    When I navigate to the membership screen
    Then I should see the membership initial screen
    And I should see the benefits of membership:
      | benefit                              |
      | Keep 100% of your ride fares         |
      | No commission on any trip            |
      | Only $20/month flat fee              |
      | Cancel anytime                       |

  Scenario: View membership benefits page
    Given I am on the membership screen
    When I tap "View Benefits"
    Then I should see the membership benefits page
    And I should see a comparison with competitor commission rates

  # --- SUBSCRIBE ---

  Scenario: Subscribe to membership
    Given I am on the membership subscribe page
    When I tap "Subscribe - $20/month"
    Then a GetPaymentURLEvent should be dispatched
    And I should receive a payment URL
    And the in-app webview should open with the Adyen payment page
    When I complete the payment in the webview
    Then my membership should become active
    And I should see a confirmation message

  Scenario: Payment fails during subscription
    Given I am completing the membership payment
    When the payment is declined
    Then I should see a payment error in the webview
    And I should be able to retry with a different payment method
    And my membership should remain inactive

  # --- MANAGE MEMBERSHIP ---

  Scenario: View active membership details
    Given I have an active membership
    When I navigate to "Manage Membership"
    Then I should see:
      | field             |
      | Status            |
      | Monthly fee       |
      | Next billing date |
      | Payment method    |

  Scenario: Cancel membership
    Given I have an active membership
    When I navigate to "Manage Membership"
    And I tap "Cancel Membership"
    And I confirm the cancellation
    Then my membership should be marked for cancellation
    And I should see the date when my membership will end
    And I should retain access until the billing period ends

  Scenario: Reactivate cancelled membership
    Given my membership is set to cancel at the end of the billing period
    When I tap "Reactivate Membership"
    Then my membership should be reactivated
    And the cancellation should be reversed

  # --- BILLING ---

  Scenario: Membership auto-renewal
    Given I have an active membership
    When the billing date arrives
    Then my payment method should be charged $20 CAD
    And my membership should renew for another month

  Scenario: Payment method fails on renewal
    Given I have an active membership
    When the renewal charge fails
    Then I should receive a notification about the failed payment
    And I should have a grace period to update my payment method
    And I should not be able to go online if payment remains failed

  # --- MEMBERSHIP REQUIRED ---

  Scenario: Cannot go online without membership
    Given I am an approved driver without an active membership
    When I try to go online
    Then I should be prompted to subscribe to a membership
    And I should not be able to accept rides

  # --- EDGE CASES ---

  Scenario: Webview fails to load payment page
    Given I tap "Subscribe"
    When the payment URL fails to load in the webview
    Then I should see an error message
    And I should be able to retry

  Scenario: App backgrounded during payment
    Given I am completing payment in the webview
    When the app goes to the background and returns
    Then the webview should still be active
    And I should be able to complete the payment
