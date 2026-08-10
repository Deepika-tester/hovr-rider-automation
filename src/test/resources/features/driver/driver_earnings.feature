@driver @earnings
Feature: Driver Earnings Dashboard
  As a HOVR driver
  I want to view my earnings and trip history
  So that I can track my income and verify 100% fare retention

  # Derived from: hovr-driver-app/lib/features/home-view/presentation/pages/drawer-screens/earning/
  # Pages: earnings_screen.dart, trip_detail_screen.dart, trip_detail_pickup_dropoff.dart
  # Widgets: session_list_item_view.dart, session_list_view.dart
  # HOVR model: $20/month membership, driver keeps 100% of fare

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app
    And I have an active membership

  # --- EARNINGS OVERVIEW ---

  Scenario: View daily earnings summary
    Given I have completed 8 trips today
    When I navigate to the earnings screen
    Then I should see my total daily earnings
    And I should see the number of trips completed
    And I should see total online hours

  Scenario: View earnings with no trips today
    Given I have not completed any trips today
    When I navigate to the earnings screen
    Then I should see "$0.00" for today's earnings
    And I should see "0 trips" completed

  Scenario: View weekly earnings summary
    Given I have driven multiple days this week
    When I navigate to the earnings screen
    And I select the weekly view
    Then I should see my total weekly earnings
    And I should see a breakdown by day

  # --- TRIP DETAILS ---

  Scenario: View individual trip details
    Given I am on the earnings screen
    When I tap on a specific trip in the session list
    Then I should see the trip detail screen with:
      | field             |
      | Trip date/time    |
      | Pickup address    |
      | Drop-off address  |
      | Trip duration     |
      | Trip distance     |
      | Fare amount       |
      | Tip amount        |
      | Total earned      |

  Scenario: View trip pickup and drop-off on map
    Given I am viewing a trip detail
    When I tap on the route
    Then I should see the trip_detail_pickup_dropoff view
    And I should see the pickup and drop-off locations on a map

  # --- SESSION LIST ---

  Scenario: View driving sessions
    Given I have driven today
    When I view the earnings screen
    Then I should see a list of driving sessions
    And each session should show:
      | field           |
      | Start time      |
      | End time        |
      | Trips count     |
      | Total earned    |

  # --- 100% FARE RETENTION ---

  Scenario: Verify driver keeps 100% of fare
    Given I completed a trip with fare "$25.00"
    When I view the trip details in earnings
    Then I should see the full fare of "$25.00" credited to my account
    And there should be no commission or service fee deducted
    And the fare shown should match what the rider was charged

  Scenario: Verify tip is fully received
    Given I completed a trip and received a $5.00 tip
    When I view the trip details
    Then I should see the tip of "$5.00" in addition to the fare
    And the tip should not have any deductions

  # --- PAYOUT ---

  Scenario: View payout status
    Given I have earnings pending payout
    When I navigate to banking details
    Then I should see my pending payout amount
    And I should see my next payout date
    And I should see my payout history

  # --- EDGE CASES ---

  Scenario: Earnings screen loading error
    Given I navigate to the earnings screen
    When the earnings data fails to load
    Then I should see an error message
    And I should be able to retry loading

  Scenario: View earnings for a date range with no activity
    Given I select a date range where I did not drive
    Then I should see an empty earnings view
    And I should see "$0.00" total
