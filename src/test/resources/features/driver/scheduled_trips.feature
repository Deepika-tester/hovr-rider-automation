@scheduled-trips
Feature: Scheduled trips
  As a driver
  I want to accept rides booked in advance
  So that I can plan my schedule

  Background:
    Given the driver is logged in
    And the driver is approved for rideshare

  @smoke
  Scenario: Driver views scheduled rides
    When the driver opens "Scheduled trips"
    Then the text "Scheduled Rides" is shown
    And the text "Browse available and accepted scheduled rides for your schedule." is shown
    And the rides for today are listed

  Scenario: No scheduled rides for the selected day
    Given there are no scheduled rides for tomorrow
    When the driver opens "Scheduled trips"
    And the driver selects tomorrow
    Then the text "No scheduled rides for this day." is shown
    When the driver taps "Today"
    Then today is selected

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @blocked
  Scenario Outline: Driver filters scheduled trips
    Given the driver has scheduled trips with different statuses
    When the driver opens "Scheduled trips"
    And the driver opens "Filter trips"
    And the driver selects "<filter>"
    Then only trips with status "<filter>" are listed

    Examples:
      | filter    |
      | Pending   |
      | Accepted  |
      | Cancelled |

  Scenario: Filter with no results
    Given the driver has no cancelled trips today
    When the driver filters scheduled trips by "Cancelled"
    Then the text "No trips match this filter for this day." is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @smoke @blocked
  Scenario: Driver accepts a scheduled trip offer
    Given a new scheduled trip offer is available
    When the driver opens the scheduled trip offer
    Then the text "New scheduled trip" is shown
    And the "YOU'LL EARN (EST.)" amount is shown
    And the "Respond in" countdown is shown
    When the driver taps "Accept Trip"
    Then the snackbar "Scheduled trip accepted" is shown
    And the trip is listed under "Accepted rides"

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @blocked
  Scenario: Driver declines a scheduled trip offer
    Given a new scheduled trip offer is available
    When the driver opens the scheduled trip offer
    And the driver taps "Decline"
    Then the offer is removed from the list

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @offer @negative @blocked
  Scenario: Accepting a scheduled trip fails
    Given the accept offer request returns an error
    When the driver accepts a scheduled trip offer
    Then the text "Could not accept scheduled trip" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @accepted @blocked
  Scenario: Accepted trip details before pickup window
    Given the driver has an accepted scheduled trip 3 hours from now
    When the driver opens the accepted trip
    Then the text "Available closer to pickup" is shown
    And the text "What unlocks then" is shown
    And the unlock features are listed:
      | feature                          |
      | Directions to the pickup location |
      | Exact pickup address             |
      | Start trip controls              |

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @accepted @blocked
  Scenario: Accepted trip fare breakdown
    Given the driver has an accepted scheduled trip
    When the driver opens the accepted trip
    Then "Trip fare", "Platform fee*" and "You'll earn (est.)" are shown
    And the text "*HOVR retains this amount alone." is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @accepted @smoke @blocked
  Scenario: Driver starts an accepted scheduled trip
    Given the driver has an accepted scheduled trip inside the pickup window
    When the driver opens the accepted trip
    And the driver taps "Start trip"
    Then the text "Drive to pickup spot" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @accepted @blocked
  Scenario: Driver gets directions to the pickup
    Given the driver has an accepted scheduled trip inside the pickup window
    When the driver opens the accepted trip
    And the driver taps "Get Directions to Pickup"
    Then navigation to the pickup is opened

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @accepted @cancel @blocked
  Scenario: Driver cancels an accepted scheduled trip
    Given the driver has an accepted scheduled trip
    When the driver opens the accepted trip
    And the driver taps "Cancel this trip"
    Then the text "Are you sure you want to cancel this scheduled trip?" is shown
    When the driver confirms
    Then the snackbar "Scheduled trip cancelled" is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @accepted @negative @blocked
  Scenario: Starting a scheduled trip fails
    Given the start trip request returns an error
    When the driver starts an accepted scheduled trip
    Then the text "Could not start trip. Try again." is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @accepted @blocked
  Scenario: No accepted rides for the day
    Given the driver has no accepted scheduled rides today
    When the driver opens "Accepted rides"
    Then the text "No accepted scheduled rides for this day." is shown
