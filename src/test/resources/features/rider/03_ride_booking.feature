@ride-booking
Feature: Ride booking
  As a rider
  I want to choose a ride type and request a driver
  So that I can get to my destination

  Background:
    Given I am signed in with a default payment method
    And I have selected a pickup and a destination

  @smoke
  Scenario: Choose a ride type
    Then I should see "Choose a ride"
    And each ride option shows a price and "Pickup in" ETA
    When I select a ride type
    Then the "Confirm" button shows the selected ride type

  Scenario: View fare breakdown
    Given I am on the choose a ride screen
    When I open the fare breakdown for a ride type
    Then I should see "Fare", "Tax" and "Est. Total"

  # BLOCKED: needs a route for which no ride service is offered; the app's place search only offers places inside the marketplace, so such a route cannot be chosen from the UI (the API reports 'pickup in not supported area' for it -- see the API suite).
  @blocked
  Scenario: No ride services available
    Given no ride services are available for this route
    Then I should see "No ride services available"

  # BLOCKED: a rider can never have zero payment methods: Google Pay is always listed in the wallet and cannot be removed, so the 'no payment methods' state cannot be set up.
  @blocked
  Scenario: Booking without a payment method
    Given I have no payment methods
    When I tap confirm on a ride type
    Then I should see "Please add a payment method to continue booking your ride."

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Confirm pickup spot
    Given I confirmed a ride type
    Then I should see "Set your pickup spot"
    When I drag the map to place the pin
    And I tap "Confirm pickup"
    Then I should see "Finding your driver"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @smoke @blocked
  Scenario: Driver is found
    Given I am on the finding your driver screen
    When a driver accepts the trip
    Then I should see "Driver is on the way"
    And I should see the driver's vehicle on the map

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: No driver found
    Given I am on the finding your driver screen
    When no driver accepts within the search window
    Then I should see "No driver found nearby you!"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Booking request timeout
    Given the booking request does not complete in time
    Then I should see "Ride booking request timeout!"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Cancel while finding a driver
    Given I am on the finding your driver screen
    When I tap "Cancel trip"
    Then I should see "Cancel trip?"
    When I tap "Yes, cancel"
    Then I should see "Trip cancelled by you!"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario Outline: Cancel after driver assigned with a reason
    Given a driver is assigned to my trip
    When I tap "Cancel trip"
    Then I should see "Why do you want to cancel?"
    When I select "<reason>"
    And I tap "Yes, cancel"
    Then the trip is cancelled

    Examples:
      | reason                   |
      | Wait time too long       |
      | Driver asked me to cancel|
      | Ride no longer needed    |

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Keep the trip
    Given the cancel trip dialog is open
    When I tap "Keep trip"
    Then the trip continues

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Driver arrives at pickup
    Given the driver is on the way
    When the driver arrives at pickup
    Then I should see "Driver has arrived"
    And I should see "Meet at pickup spot"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Trip starts
    Given the driver has arrived
    When the driver starts the trip
    Then I should see "Heading to destination"
    And I should see the drop-off ETA

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Driver cancels the trip
    Given a driver is assigned to my trip
    When the driver cancels
    Then I should see "Trip cancelled by driver!"
    And I should see "Rebook"

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Trip completes and rating
    Given my trip is in progress
    When the trip completes
    Then I should see "How was your ride with <driver name>?"
    When I give 5 stars
    And I select compliment "Drove safely"
    And I tap "Done"
    Then the rating is submitted

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Low rating with complaint
    Given the rating screen is shown
    When I give 2 stars
    Then I should see "What went wrong?"
    When I select "Late pickup"
    And I enter comment "Driver was 15 minutes late"
    And I tap "Done"
    Then the complaint is submitted

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario Outline: Tipping the driver
    Given the rating screen is shown
    When I tap "Add a tip"
    And I enter custom amount "<amount>"
    Then I should see "<result>"

    Examples:
      | amount | result                              |
      | 5      | Save · $5.00                        |
      | 150    | The maximum tip amount is $100.     |

  # BLOCKED: confirming the pickup creates a real ride request on the shared dev backend (and most of what follows needs a driver to accept); held until the ride-confirming scenarios are approved. Only the HOVR option is ever used, with Mohali addresses.
  @blocked
  Scenario: Chat with driver
    Given a driver is assigned to my trip
    When I open the message screen
    And I send "I'm at the front door"
    Then the message appears in the chat
