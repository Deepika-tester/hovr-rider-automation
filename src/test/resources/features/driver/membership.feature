@membership
Feature: Driver membership
  As a driver
  I want to see and subscribe to membership benefits
  So that I can access partner perks

  Background:
    Given the driver is logged in

  @smoke
  Scenario: Driver views membership benefits
    When the driver opens "Membership" from the drawer
    Then the text "MEMBERSHIP" is shown
    And the text "Member Benefits include:" is shown
    And the benefits are listed:
      | benefit          |
      | Health Insurance |
      | Fitness Access   |
      | Phone Savings    |
      | More coming soon |

  Scenario: Driver opens membership options
    When the driver opens "Membership" from the drawer
    And the driver taps "See membership options"
    Then the membership subscribe page is shown

  # BLOCKED: would change or consume data (it takes a payment) in a way that cannot be undone from here; not run without approval.
  @blocked
  Scenario: Driver subscribes to a membership
    Given the driver is on the membership subscribe page
    When the driver selects a membership plan
    And the driver completes the payment
    Then the membership is active
    And the "Back to HOVR" button is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @blocked
  Scenario: Pending driver can access perks while waiting
    Given the driver documents are in review
    When the driver opens "Membership" from the drawer
    Then the text "Access perks while you wait for approval" is shown
