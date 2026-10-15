@api @neighbour @co-commute
Feature: API - Co-Commute (Neighbour) services
  neighbour.v1.eligibility.NeighbourEligibilityService and neighbour.v1.driver.NeighbourDriverService.
  Callers: lib/features/neighbours-feature/presentation/cubit/*.

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session
    And the Co-Commute marketplace id "<neighbour marketplace id>"

  @eligibility @smoke
  Scenario: CheckNeighbourEligibility for an eligible driver
    Given the driver meets all Co-Commute requirements
    When I call "neighbour.v1.eligibility.NeighbourEligibilityService/CheckNeighbourEligibility" with:
      | field                    | value                      |
      | driver_id                | <driver id>                |
      | current_marketplace_id   | <marketplace id>           |
      | neighbour_marketplace_id | <neighbour marketplace id> |
    Then the response code is "ok"
    And the driver is eligible with no blocking reasons

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @eligibility @blocked
  Scenario: CheckNeighbourEligibility for an ineligible driver
    Given the driver is missing a Co-Commute requirement
    When I call "CheckNeighbourEligibility" for the driver
    Then the driver is not eligible
    And the response lists the blocking reasons

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @post @smoke @blocked
  Scenario: PostNeighbourTrip creates an open commute
    When I call "neighbour.v1.driver.NeighbourDriverService/PostNeighbourTrip" with:
      | field                 | value                      |
      | request_id            | <uuid>                     |
      | marketplace_id        | <neighbour marketplace id> |
      | origin                | 43.6532,-79.3832           |
      | destination           | 43.7615,-79.4111           |
      | depart_at             | now + 45 minutes           |
      | depart_window_seconds | 900                        |
    Then the response code is "ok"
    And the trip status is "open"
    And "ListMyNeighbourTrips" returns the new trip

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @post @negative @blocked
  Scenario Outline: PostNeighbourTrip validates the departure time
    When I call "PostNeighbourTrip" with depart_at "<depart_at>"
    Then the response code is "invalid_argument"

    Examples:
      | depart_at         |
      | now + 10 minutes  |
      | now + 15 days     |
      | now - 1 hour      |

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @post @idempotency @blocked
  Scenario: PostNeighbourTrip is idempotent on request_id
    Given I posted a commute with request_id "<uuid>"
    When I call "PostNeighbourTrip" again with the same request_id
    Then only one commute exists

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @post @negative @blocked
  Scenario: Ineligible driver cannot post a commute
    Given the driver is not eligible for Co-Commute
    When I call "PostNeighbourTrip" with a valid commute
    Then the response code is "permission_denied" or "failed_precondition"

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @blocked
  Scenario: UpdateNeighbourTrip changes the departure window
    Given the driver has an open commute "<neighbour trip id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/UpdateNeighbourTrip" with:
      | field                 | value               |
      | neighbour_trip_id     | <neighbour trip id> |
      | depart_window_seconds | 1200                |
    Then the response code is "ok"

  @my-trips
  Scenario: ListMyNeighbourTrips returns upcoming commutes
    When I call "neighbour.v1.driver.NeighbourDriverService/ListMyNeighbourTrips" with:
      | field         | value |
      | upcoming_only | true  |
      | page_size     | 20    |
    Then the response code is "ok"

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @bookings @blocked
  Scenario: ListBookingsOnMyTrip returns pending bookings
    Given the commute "<neighbour trip id>" has a pending booking
    When I call "neighbour.v1.driver.NeighbourDriverService/ListBookingsOnMyTrip" with:
      | field             | value               |
      | neighbour_trip_id | <neighbour trip id> |
      | pending_only      | true                |
    Then the response code is "ok"
    And the booking includes the rider price, platform fee and driver earnings

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @bookings @blocked
  Scenario: ApproveNeighbourBooking confirms the booking
    Given a pending booking "<booking id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/ApproveNeighbourBooking" with:
      | field                | value        |
      | neighbour_booking_id | <booking id> |
    Then the response code is "ok"
    And the booking status is "confirmed"

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @bookings @blocked
  Scenario: DeclineNeighbourBooking with a reason
    Given a pending booking "<booking id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/DeclineNeighbourBooking" with:
      | field                | value        |
      | neighbour_booking_id | <booking id> |
      | reason               | Route change |
    Then the response code is "ok"
    And the booking status is "declined"

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @bookings @negative @blocked
  Scenario: Approving an already declined booking
    Given a declined booking "<booking id>"
    When I call "ApproveNeighbourBooking" for it
    Then the response code is "failed_precondition"

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @journey @blocked
  Scenario: StartNeighbourJourney starts a live trip
    Given a confirmed booking "<booking id>" on commute "<neighbour trip id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/StartNeighbourJourney" with:
      | field                | value               |
      | request_id           | <uuid>              |
      | neighbour_booking_id | <booking id>        |
      | neighbour_trip_id    | <neighbour trip id> |
    Then the response code is "ok"
    And the response contains a live trip id
    And "ride.v1.trip.TripService/GetActiveTrip" returns that trip

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @cancel @blocked
  Scenario: CancelNeighbourTrip cancels the commute
    Given the driver has an open commute "<neighbour trip id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/CancelNeighbourTrip" with:
      | field             | value               |
      | neighbour_trip_id | <neighbour trip id> |
      | reason            | Plans changed       |
    Then the response code is "ok"
    And the trip status is "canceled"
    And all bookings on it are cancelled

  @nearby
  Scenario: ListNeighbourRequests near the driver
    When I call "neighbour.v1.driver.NeighbourDriverService/ListNeighbourRequests" with:
      | field           | value                      |
      | marketplace_id  | <neighbour marketplace id> |
      | driver_location | 43.6532,-79.3832           |
      | within_seconds  | 7200                       |
      | page_size       | 20                         |
    Then the response code is "ok"
    And only requests within the next 2 hours are returned

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @nearby @blocked
  Scenario: AcceptNeighbourRequest books the rider
    Given an open rider request "<neighbour request id>"
    When I call "neighbour.v1.driver.NeighbourDriverService/AcceptNeighbourRequest" with:
      | field                | value                  |
      | request_id           | <uuid>                 |
      | neighbour_request_id | <neighbour request id> |
    Then the response code is "ok"
    And "ListMyNeighbourBookings" returns the booking

  # BLOCKED: would post or change a real Co-Commute listing or booking on the shared dev backend; held with the other ride-confirming scenarios.
  @nearby @negative @blocked
  Scenario: AcceptNeighbourRequest for a request taken by another driver
    Given the rider request was accepted by another driver
    When I call "AcceptNeighbourRequest" for it
    Then the response code is "failed_precondition" or "already_exists"

  @my-bookings
  Scenario: ListMyNeighbourBookings returns the driver's bookings
    When I call "neighbour.v1.driver.NeighbourDriverService/ListMyNeighbourBookings" with:
      | field         | value |
      | upcoming_only | false |
      | page_size     | 20    |
    Then the response code is "ok"

  @pagination
  Scenario Outline: List endpoints page with page_token
    Given more than 20 items exist for "<method>"
    When I call "neighbour.v1.driver.NeighbourDriverService/<method>" with page_size 20
    Then 20 items and a next page token are returned
    When I call it again with that page token
    Then the next items are returned without duplicates

    Examples:
      | method                  |
      | ListMyNeighbourTrips    |
      | ListMyNeighbourBookings |
      | ListNeighbourRequests   |
