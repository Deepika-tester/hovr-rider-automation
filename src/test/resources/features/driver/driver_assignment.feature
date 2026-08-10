@driver @assignment @new-feature
Feature: Driver Assignments (Recurring Pre-Scheduled Routes)
  As a HOVR driver seeking predictable income
  I want to apply for and manage recurring route assignments
  So that I can have guaranteed scheduled trips

  # Derived from: /home/aiciv/hovr/engineering-brain/product-features/assignments-feature.md
  # Status: Specification ready for engineering (not yet in codebase)
  # Key concepts: Assignment = recurring pre-scheduled trip with fixed fare
  # Matching: Geo-radius <= 5km, first-come-first-served, tie-breaker by rating + on-time %
  # Driver obligations: 5-min pickup window, rating >= 4.7, active insurance
  # Cancellation: >= 24h notice required, strike system (1st=14d, 2nd=60d, 3rd=permanent ban)

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app
    And I have an active membership
    And my driver rating is 4.7 or higher

  # --- BROWSE AVAILABLE ASSIGNMENTS ---

  Scenario: View available assignments feed
    Given there are available assignments near my location
    When I navigate to the "Available Assignments" tab
    Then I should see a list of assignments sorted by distance from my location
    And each assignment should display:
      | field                |
      | Pickup address       |
      | Drop-off address     |
      | Scheduled time       |
      | Frequency            |
      | Fixed fare amount    |
      | Distance from me     |

  Scenario: Filter assignments by frequency
    Given I am on the available assignments feed
    When I filter by "Weekdays"
    Then I should only see assignments with weekday frequency

  Scenario: No assignments available in my area
    Given there are no available assignments within 5km of my location
    When I navigate to "Available Assignments"
    Then I should see an empty state
    And I should see a message about checking back later

  # --- APPLY FOR ASSIGNMENT ---

  Scenario: Apply for an available assignment
    Given I see an assignment "Daily 7:00 AM - Union Station to Bay St"
    When I tap "Apply"
    Then I should see the Assignment Agreement details including:
      | field               |
      | Schedule (days/times)|
      | Fixed fare           |
      | Start date           |
      | Performance standards|
    When I e-sign the Assignment Agreement
    Then I should see a confirmation message
    And the assignment should appear in my "My Assignments" list
    And I should receive a calendar sync invitation

  Scenario: Apply when not meeting rating requirement
    Given my driver rating is 4.5 (below 4.7 threshold)
    When I try to apply for an assignment
    Then I should see an error "Your rating must be 4.7 or higher for Assignments"
    And I should not be able to apply

  Scenario: Apply for an assignment that is already taken
    Given another driver has already been assigned the route
    When I try to apply
    Then I should see "This assignment has been filled"

  # --- MANAGE MY ASSIGNMENTS ---

  Scenario: View my active assignments
    Given I have accepted assignments
    When I navigate to "My Assignments"
    Then I should see my active assignments with:
      | field              |
      | Route details      |
      | Next occurrence    |
      | Fixed fare         |
      | On-time percentage |

  Scenario: Complete an assignment trip
    Given I have an assignment scheduled for today at 7:00 AM
    When I arrive at the pickup within the 5-minute window
    And the rider enters the vehicle
    And I complete the trip
    Then I should earn the fixed assignment fare
    And my on-time percentage should be updated

  # --- CANCELLATION & COVERAGE ---

  Scenario: Cancel assignment with 24+ hours notice
    Given I have an assignment tomorrow
    When I tap "Request Coverage" on the assignment
    And I confirm the cancellation more than 24 hours before
    Then HOVR should attempt to find a coverage driver
    And I should see the coverage request status
    And no strike should be issued

  Scenario: Cancel assignment with less than 24 hours notice
    Given I have an assignment in 4 hours
    When I try to cancel
    Then I should see a warning about the short notice
    And I should be informed that a strike may be issued
    When I confirm the cancellation
    Then a strike should be recorded against my account

  Scenario: View strike history
    Given I have 1 active strike
    When I view my assignment dashboard
    Then I should see my strike count
    And I should see the suspension dates if applicable

  # --- STRIKE SYSTEM ---

  Scenario: First strike results in 14-day suspension
    Given I have no previous strikes
    When I receive a strike for no-show
    Then I should see "Assignment suspension: 14 days"
    And I should not be able to apply for assignments during suspension
    And I should still be able to accept regular on-demand rides

  Scenario: Second strike results in 60-day suspension
    Given I have 1 previous strike
    When I receive a second strike
    Then I should see "Assignment suspension: 60 days"

  Scenario: Third strike results in permanent ban
    Given I have 2 previous strikes
    When I receive a third strike
    Then I should see "Permanently removed from Assignment Program"
    And I should no longer see the Assignments tab

  # --- ASSIGNMENT TRIP FLOW ---

  Scenario: Arrive on time for assignment
    Given I have an assignment at 7:00 AM
    When I arrive at the pickup at 6:58 AM (within 5-min window)
    Then the arrival should be marked as on-time
    And my on-time percentage should improve

  Scenario: Arrive late for assignment
    Given I have an assignment at 7:00 AM
    When I arrive at the pickup at 7:08 AM (outside 5-min window)
    Then the arrival should be marked as late
    And my on-time percentage should decrease
    And if my rolling 30-day on-time drops below 90%, I should see a coaching alert

  Scenario: Rider no-show on assignment
    Given I arrived on time for my assignment
    And the rider does not appear within the wait period
    When I report a rider no-show
    Then the rider should be charged in full (per assignment terms)
    And I should receive the full assignment fare
    And this should not count as a strike against me

  # --- ASSIGNMENT LOOPS (HOVR Loop) ---

  Scenario: View stacked assignment loop
    Given I have morning assignments at 7:00 AM and 8:00 AM
    And corresponding evening return assignments
    When I view my schedule
    Then I should see them grouped as a "HOVR Loop"
    And I should see the total earnings for the full loop

  # --- RIDER COVERAGE ---

  Scenario: Rider gets coverage driver when original cancels
    Given the rider has an active assignment
    And the assigned driver cancelled with 24+ hour notice
    When HOVR finds a coverage driver
    Then the rider should be notified of the driver change
    And the trip should proceed with the coverage driver

  # --- EDGE CASES ---

  Scenario: Assignment fare remains fixed regardless of traffic
    Given I have a fixed-fare assignment at $35
    When traffic causes the trip to take 20 minutes longer than usual
    Then my fare should still be the fixed $35
    And surge pricing should NOT apply

  Scenario: Insurance expires while assignment is active
    Given I have active assignments
    When my insurance document expires
    Then my active assignments should be flagged
    And I should be notified to update my insurance
    And I should not be able to start assignment trips until insurance is updated

  Scenario: Assignment terminated for abandonment
    Given I abandon an assignment without notice
    When HOVR reviews the abandonment
    Then I should receive an immediate suspension notice
    And my active assignments should be reassigned
