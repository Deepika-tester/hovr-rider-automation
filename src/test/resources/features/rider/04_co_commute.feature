@co-commute
Feature: Co-Commute
  As a rider
  I want to share commutes with neighbours
  So that I can split gas costs

  Background:
    Given I am signed in with a default payment method
    And location permission is granted
    And I am on the "Co-Commute" tab

  @smoke
  Scenario: View nearby journeys
    Then I should see "Nearby journeys"
    And each journey shows leave time and seats left

  Scenario: Nearby journeys without location
    Given location permission is denied
    Then I should see "Turn on location to find nearby journeys."

  Scenario: Empty nearby journeys
    Given there are no nearby journeys
    Then I should see "No nearby journeys right now. Pull to refresh."

  Scenario: Search journeys
    When I tap "Search journeys"
    And I select a pickup and dropoff location
    Then I see journeys matching my route

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @smoke @blocked
  Scenario: Book a seat
    Given a journey with seats available is listed
    When I open the journey
    And I tap "Book a seat"
    Then I should see "Seat booked. About $1 is held, not charged."
    And the booking appears in "My bookings"

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Seat already taken
    Given another rider books the last seat before me
    When I tap "Book a seat"
    Then I should see "That seat was just taken. Pull to refresh and pick another journey."

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Card declined on booking
    Given my default card will be declined
    When I tap "Book a seat"
    Then I should see "Your card was declined. Update your payment method and try again."

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Journey full
    Given a journey has no seats left
    Then it shows "Full"
    And "Book a seat" is unavailable

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Booking requires default payment method
    Given I have no default payment method
    When I tap "Book a seat"
    Then I should see "Set a default payment method to continue."

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Cancel a booking
    Given I have a confirmed booking
    When I open the booking details
    And I tap "Cancel booking"
    And I tap "Yes, cancel"
    Then I should see "Booking canceled"

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Post a commute request
    When I tap "Plan a Co-Commute"
    And I set pickup, dropoff and a leave time
    And I tap "Post commute"
    Then I should see "Your commute is posted"
    And it appears in "My requests" with status "Open"

  Scenario: Post without leave time
    Given I am planning a co-commute
    When I continue without a leave time
    Then I should see "Choose a leave time"

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Cancel a posted commute
    Given I have an open commute request
    When I tap "Cancel request"
    Then I should see "Cancel this commute?"
    When I tap "Yes, cancel"
    Then I should see "Your commute request was canceled"

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: View booking details
    Given I have a booking
    When I open "Booking details"
    Then I should see status, driver, price breakdown and booking ID
    And the platform fee shows "Held, not charged"

  # BLOCKED: booking a seat or posting a commute holds a payment and creates a real booking/request on the shared dev backend; held until the ride-confirming scenarios are approved. (In the earlier UI run posting a commute also timed out on the server.)
  @blocked
  Scenario: Contact driver from booking
    Given I have a confirmed booking
    When I tap "Call"
    Then the phone app opens with the driver's number
