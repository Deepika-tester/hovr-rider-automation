@api @trip
Feature: TripService API
  Trip lifecycle, live streaming, tips and ratings.
  Client: TripServiceClient (trip_feature_remote_data_source.dart, trip stream isolate)

  Background:
    Given I am authenticated as a rider with a default payment method
    And a ride quote exists for a valid route

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @smoke @blocked
  Scenario: Create a trip
    When I call "TripService/CreateTrip" with the selected ride service
    Then the response status is "OK"
    And the trip status is "SEARCHING" or equivalent

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Create a trip while another trip is active
    Given I already have an active trip
    When I call "TripService/CreateTrip"
    Then the response status is "ALREADY_EXISTS" or "FAILED_PRECONDITION"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Get active trip
    Given I have an active trip
    When I call "TripService/GetActiveTrip"
    Then the response contains that trip id

  Scenario: Get active trip when none
    Given I have no active trip
    When I call "TripService/GetActiveTrip"
    Then the response contains no trip or status "NOT_FOUND"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @smoke @blocked
  Scenario: Stream trip updates
    Given I have an active trip
    When I open the "TripService/StreamTrip" server stream
    And the driver accepts, arrives, starts and completes the trip
    Then I receive status updates in order:
      | DRIVER_ASSIGNED |
      | ARRIVING        |
      | ARRIVED         |
      | IN_PROGRESS     |
      | COMPLETED       |
    And the stream closes after completion

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Stream reconnects after network drop
    Given I am streaming an active trip
    When the connection drops for 10 seconds
    Then the client reconnects within the TripStreamLimits retry budget
    And the latest trip status is delivered

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario Outline: Cancel a trip
    Given I have an active trip in status "<status>"
    When I call "TripService/CancelTrip" with reason "<reason>"
    Then the response status is "OK"
    And the trip status becomes "CANCELED"
    And the cancellation fee is "<fee>"

    Examples:
      | status          | reason             | fee        |
      | SEARCHING       | RIDE_NOT_NEEDED    | none       |
      | DRIVER_ASSIGNED | WAIT_TOO_LONG      | per policy |

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Cancel a completed trip
    Given I have a completed trip
    When I call "TripService/CancelTrip"
    Then the response status is "FAILED_PRECONDITION"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Add and remove a stop
    Given I have a trip in progress
    When I call "TripService/AddStop" with a valid location
    Then the trip has 1 stop
    When I call "TripService/RemoveStop" for that stop
    Then the trip has 0 stops

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Change destination
    Given I have a trip in progress
    When I call "TripService/ChangeDestination" with a new dropoff
    Then the response status is "OK"
    And the fare estimate is recalculated

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Update trip
    Given I have an active trip
    When I call "TripService/UpdateTrip" with new pickup notes
    Then the response status is "OK"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Trip state transitions (driver side, test harness)
    Given I have a trip with an assigned driver
    When the driver calls "MarkArriving", "MarkArrived", "StartTrip" and "CompleteTrip" in order
    Then each call returns "OK"
    And out-of-order transitions return "FAILED_PRECONDITION"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: End trip early
    Given I have a trip in progress
    When I call "TripService/EndTripEarly"
    Then the trip status becomes "COMPLETED"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Report no-show
    Given the driver has arrived and the rider did not show up
    When "TripService/ReportNoShow" is called
    Then the trip status becomes "CANCELED"

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @smoke @blocked
  Scenario: Submit rating
    Given I have a completed trip
    When I call "TripService/SubmitRating" with 5 stars and compliment "DROVE_SAFELY"
    Then the response status is "OK"
    And GetTrip shows my rating as 5

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario Outline: Rating validation
    Given I have a completed trip
    When I call "TripService/SubmitRating" with <stars> stars
    Then the response status is "INVALID_ARGUMENT"

    Examples:
      | stars |
      | 0     |
      | 6     |

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario Outline: Add tip
    Given I have a completed trip
    When I call "TripService/AddTip" with amount <amount>
    Then the response status is "<status>"

    Examples:
      | amount | status           |
      | 5.00   | OK               |
      | 100.00 | OK               |
      | 150.00 | INVALID_ARGUMENT |
      | -1.00  | INVALID_ARGUMENT |

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: List past trips
    Given I have completed and canceled trips
    When I call "TripService/ListTrips"
    Then the response contains both completed and canceled trips
    And trips are ordered newest first

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Get trip receipt
    Given I have a completed trip
    When I call "TripService/GetTrip" with that trip id
    Then the response contains fare breakdown, fees, tax and total

  # BLOCKED: needs a real trip (and for most of these a driver acting on it); creating one would be a real ride request on the shared dev backend, and the environment has no approved driver to complete it.
  @blocked
  Scenario: Get another rider's trip
    When I call "TripService/GetTrip" with a trip id owned by another rider
    Then the response status is "PERMISSION_DENIED" or "NOT_FOUND"
