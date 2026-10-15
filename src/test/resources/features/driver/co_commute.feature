@co-commute @neighbours
Feature: Co-Commute (Neighbours) for drivers
  As a Hovr driver
  I want to share my daily commute with neighbours going the same way
  So that I can earn from seats on trips I am already making

  Background:
    Given the driver is logged in
    And the driver is on the home screen

  # ---------------------------------------------------------------- Eligibility

  @eligibility @smoke
  Scenario: Eligible driver opens Co-Commute
    Given the driver is eligible for Co-Commute
    When the driver opens "Co-Commute" from the drawer
    Then the "Nearby Rides" tab is shown
    And the "My trips" tab is shown
    And the "My booking" tab is shown
    And the "History" tab is shown

  @eligibility
  Scenario: Ineligible driver sees blocking requirements
    Given the driver is not eligible for Co-Commute
    And the driver has pending Co-Commute requirements
    When the driver opens "Co-Commute" from the drawer
    Then the text "Not eligible for Co-Commute" is shown
    And the text "Complete these requirements before posting a commute." is shown
    And the pending requirements are listed
    And the "Start onboarding" button is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @eligibility @negative @blocked
  Scenario: Eligibility check fails
    Given the eligibility check returns an error
    When the driver opens "Co-Commute" from the drawer
    Then the text "Could not check Co-Commute eligibility." is shown
    When the driver taps "Try again"
    Then the eligibility check is retried

  # ------------------------------------------------------------- Post commute

  # BLOCKED: would change or consume data (it posts a real commute and holds a payment) in a way that cannot be undone from here; not run without approval.
  @post-commute @smoke @blocked
  Scenario: Driver posts a commute with "Leave at"
    Given the driver is eligible for Co-Commute
    And the driver is on the "My trips" tab
    When the driver taps "Post your own commute"
    Then the text "Where are you heading?" is shown
    When the driver searches destination "Work"
    And the driver selects the first search result
    And the driver confirms the pickup location
    Then the text "Choose a time" is shown
    When the driver selects "Leave at"
    And the driver picks a time 45 minutes from now
    And the driver taps "Confirm and continue"
    Then the snackbar "Commute posted." is shown
    And the posted commute appears under "My trips" with status "Open"

  # BLOCKED: would change or consume data (it posts a real commute) in a way that cannot be undone from here; not run without approval.
  @post-commute @blocked
  Scenario: Driver posts a commute with "Arrive by"
    Given the driver has selected origin and destination
    And the commute duration is 25 minutes
    When the driver selects "Arrive by"
    And the driver picks a time 90 minutes from now
    Then the summary shows "Arrive by" the selected time
    And the departure time is 25 minutes before the selected time
    When the driver taps "Confirm and continue"
    Then the snackbar "Commute posted." is shown

  @post-commute
  Scenario: Driver sets destination on the map
    Given the driver is on the "Where are you heading?" screen
    When the driver taps "Set location on map"
    And the driver moves the map pin to a new location
    And the driver taps "Use this location"
    Then the selected location is used as the destination

  @post-commute
  Scenario: Recent places are shown on the destination screen
    Given the driver has previously searched destinations
    When the driver opens the "Where are you heading?" screen
    Then the "SAVED & RECENT" section is shown
    And at most 8 recent places are listed

  # BLOCKED: would change or consume data (valid times post a real commute) in a way that cannot be undone from here; not run without approval.
  @post-commute @validation @blocked
  Scenario Outline: Pickup time validation
    Given the driver has selected origin and destination
    When the driver picks a time <minutes> minutes from now
    And the driver taps "Confirm and continue"
    Then <outcome>

    Examples:
      | minutes | outcome                                       |
      | 10      | the dialog "Too soon" is shown                |
      | 29      | the dialog "Too soon" is shown                |
      | 30      | the snackbar "Commute posted." is shown       |
      | 120     | the snackbar "Commute posted." is shown       |

  @post-commute @validation
  Scenario: Time picker does not allow more than 14 days ahead
    Given the driver is on the "Choose a time" screen
    Then the latest selectable date is 14 days from today
    And the minutes are selectable in 5-minute steps

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @post-commute @negative @blocked
  Scenario: Posting a commute fails
    Given the post commute request returns an error
    And the driver has selected origin, destination and a valid time
    When the driver taps "Confirm and continue"
    Then the text "Could not post your commute. Please try again." is shown

  # --------------------------------------------------------- Manage my trips

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @my-trips @blocked
  Scenario: No posted commutes
    Given the driver has no posted commutes
    When the driver opens the "My trips" tab
    Then the text "No posted commutes yet." is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @my-trips @bookings @blocked
  Scenario: Driver approves a pending booking
    Given the driver has a posted commute with a booking "Pending approval"
    When the driver opens the posted commute
    And the driver taps "Approve" on the booking
    Then the snackbar "Booking updated." is shown
    And the booking status is "Confirmed"

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @my-trips @bookings @blocked
  Scenario: Driver declines a pending booking
    Given the driver has a posted commute with a booking "Pending approval"
    When the driver opens the posted commute
    And the driver taps "Decline" on the booking
    Then the snackbar "Booking updated." is shown
    And the booking status is "Declined"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @my-trips @bookings @blocked
  Scenario: Commute without bookings
    Given the driver has a posted commute with no bookings
    When the driver opens the posted commute
    Then the text "No bookings yet." is shown
    And the text "Fare shows once a rider books" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @my-trips @earnings @blocked
  Scenario: Driver views booking earnings and price breakdown
    Given the driver has a posted commute with a confirmed booking
    When the driver opens the posted commute
    Then the "Earnings" card is shown
    And "You earn" amount is shown
    When the driver opens the "Price breakdown"
    Then "Rider pays" amount is shown
    And "Platform fee" amount is shown
    And "You earn" equals "Rider pays" minus "Platform fee"

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @my-trips @journey @blocked
  Scenario: Driver starts the journey
    Given the driver has a posted commute with a confirmed booking
    And the departure time has arrived
    When the driver opens the posted commute
    And the driver taps "Start journey"
    Then the text "Live trip started." is shown
    And the commute status is "Departed"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @my-trips @cancel @blocked
  Scenario: Driver cancels a posted commute
    Given the driver has a posted commute with status "Open"
    When the driver opens the posted commute
    And the driver taps "Cancel trip"
    Then the snackbar "Trip cancelled." is shown
    And the commute status is "Cancelled"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @my-trips @details @blocked
  Scenario Outline: Driver copies an identifier from trip detail
    Given the driver has a posted commute with a confirmed booking
    When the driver opens the posted commute
    And the driver taps the "<label>" value
    Then the snackbar "<message>" is shown

    Examples:
      | label             | message                   |
      | Trip ID           | Trip ID copied            |
      | Booking ID        | Booking ID copied         |
      | Neighbour trip ID | Neighbour trip ID copied  |

  # ------------------------------------------------- Nearby rider requests

  @nearby
  Scenario: Driver sees nearby rider requests
    Given there are rider requests near the driver within the next 2 hours
    When the driver opens the "Nearby Rides" tab
    Then the "Nearby rider requests" list is shown

  @nearby
  Scenario: No nearby rider requests
    Given there are no rider requests near the driver
    When the driver opens the "Nearby Rides" tab
    Then the text "No nearby requests." is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @nearby @request-detail @blocked
  Scenario: Driver accepts a nearby rider request
    Given there is a rider request near the driver
    When the driver opens the rider request
    Then the text "Ride request" is shown
    And the "Route", "Time window", "Distance" and "Price breakdown" sections are shown
    When the driver taps "Accept"
    Then the snackbar "Request accepted." is shown
    And the request appears under "My booking"

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @nearby @request-detail @blocked
  Scenario: Driver contacts the rider from request detail
    Given the driver has an accepted rider request
    When the driver opens the rider request
    Then the "Call rider" action is shown
    And the "Message rider" action is shown

  # --------------------------------------------------------------- History

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @history @blocked
  Scenario Outline: Finished commutes appear in history
    Given the driver has a commute with status "<status>"
    When the driver opens the "History" tab
    Then the commute is listed with status "<status>"

    Examples:
      | status    |
      | Completed |
      | Cancelled |
      | Expired   |

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @negative @blocked
  Scenario: Commute detail fails to load
    Given the trip detail request returns an error
    When the driver opens a posted commute
    Then the text "Could not load this commute." is shown
