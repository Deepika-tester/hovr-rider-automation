@trips
Feature: Trips history and scheduled rides
  As a rider
  I want to see upcoming and past trips
  So that I can manage rides and get receipts

  Background:
    Given I am signed in as a rider
    And I open the "Trips" tab

  Scenario: No upcoming trips
    Given I have no scheduled trips
    Then I should see "No upcoming trips"
    And I should see "Schedule a ride"

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @blocked
  Scenario: View upcoming scheduled trip
    Given I have a scheduled trip for tomorrow
    Then I should see "UPCOMING RIDE"
    When I tap "View details"
    Then I should see "Your ride is reserved"
    And I should see "Fare estimate" and "Payment method"

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @blocked
  Scenario: Cancel scheduled ride
    Given I am viewing a scheduled trip more than 1 hour before pickup
    When I tap "Cancel ride"
    Then the scheduled trip is cancelled with no fee

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @blocked
  Scenario Outline: Filter past trips
    Given I have past trips with different statuses
    When I select the "<filter>" filter
    Then I only see trips matching "<filter>"

    Examples:
      | filter    |
      | All       |
      | Completed |
      | Canceled  |

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @smoke @blocked
  Scenario: Open trip receipt
    Given I have a completed past trip
    When I open the trip
    Then I should see "Trip receipt"
    And I should see "Fare breakdown" and "Total fare"
    And I should see the route map

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @blocked
  Scenario: Get help from receipt
    Given I am viewing a trip receipt
    When I tap "Get help with this trip"
    Then I should see the help screen for that trip

  # BLOCKED: needs a scheduled or completed trip; that needs a driver to accept and finish a ride, and the dev environment has no approved driver (the only driver it can create stays Pending).
  @blocked
  Scenario: Rebook a past trip
    Given I am viewing a completed trip
    When I tap "Rebook"
    Then pickup and drop-off are prefilled from that trip
