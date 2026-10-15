@api @trip
Feature: API - ride.v1.trip.TripService
  Trip lifecycle endpoints. Callers: ride_trip_lifecycle_handler, ride_service_bloc,
  core/isolates/trip_stream, rating views, session_list_view, notification_detail_view.
  Mutating calls send request_id (idempotency) and etag (optimistic concurrency).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session
    And the driver has accepted trip "<trip id>"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @smoke @blocked
  Scenario: Full trip lifecycle
    When I call "ride.v1.trip.TripService/MarkArriving" with trip_id, request_id and etag
    Then the response code is "ok"
    And the trip status is "arriving"
    When I call "ride.v1.trip.TripService/MarkArrived" with trip_id, request_id and etag
    Then the trip status is "arrived"
    When I call "ride.v1.trip.TripService/StartTrip" with:
      | field          | value     |
      | trip_id        | <trip id> |
      | request_id     | <uuid>    |
      | waypoint_index | 0         |
      | etag           | <etag>    |
    Then the trip status is "in_progress"
    When I call "ride.v1.trip.TripService/CompleteTrip" with trip_id, request_id and etag
    Then the trip status is "completed"
    When I call "ride.v1.trip.TripService/SubmitRating" with:
      | field      | value     |
      | trip_id    | <trip id> |
      | value      | 5         |
      | request_id | <uuid>    |
    Then the response code is "ok"

  # BLOCKED: is a bidirectional gRPC stream; the dev backend answers a unary JSON call with HTTP 415, so it cannot be driven from this client.
  @stream @blocked
  Scenario: StreamTrip pushes trip updates
    When I open the bidi stream "ride.v1.trip.TripService/StreamTrip" for "<trip id>"
    And the rider changes the destination
    Then the stream receives an update with the new destination

  # BLOCKED: is a bidirectional gRPC stream; the dev backend answers a unary JSON call with HTTP 415, so it cannot be driven from this client.
  @stream @blocked
  Scenario: StreamTrip closes when the trip ends
    Given an open "StreamTrip" stream for "<trip id>"
    When the trip is completed
    Then the stream sends the final trip state
    And the stream is closed

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @blocked
  Scenario: GetActiveTrip restores the current trip
    When I call "ride.v1.trip.TripService/GetActiveTrip" with:
      | field   | value       |
      | user_id | <driver id> |
    Then the response code is "ok"
    And the response trip id is "<trip id>"

  Scenario: GetActiveTrip with no active trip
    Given the driver has no active trip
    When I call "ride.v1.trip.TripService/GetActiveTrip" for the driver
    Then the response code is "not_found" or the trip is empty

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @blocked
  Scenario: GetTrip returns a trip
    When I call "ride.v1.trip.TripService/GetTrip" with:
      | field   | value     |
      | trip_id | <trip id> |
    Then the response code is "ok"

  Scenario: ListTrips pages through past trips
    When I call "ride.v1.trip.TripService/ListTrips" with a status filter "completed"
    Then the response code is "ok"
    And a "pagination_token" is returned when there are more trips

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @cancel @blocked
  Scenario: CancelTrip by the driver
    When I call "ride.v1.trip.TripService/CancelTrip" with:
      | field       | value         |
      | trip_id     | <trip id>     |
      | reason_code | <reason code> |
      | details     | Rider kept me waiting |
      | request_id  | <uuid>        |
      | etag        | <etag>        |
    Then the response code is "ok"
    And the trip status is "driver_canceled"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @no-show @blocked
  Scenario: ReportNoShow after waiting at pickup
    Given the trip status is "arrived"
    And the wait time has passed
    When I call "ride.v1.trip.TripService/ReportNoShow" with:
      | field         | value     |
      | trip_id       | <trip id> |
      | rider_no_show | true      |
      | request_id    | <uuid>    |
      | etag          | <etag>    |
    Then the response code is "ok"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @no-show @negative @blocked
  Scenario: ReportNoShow before the wait time ends
    Given the trip status is "arrived"
    When I call "ride.v1.trip.TripService/ReportNoShow" immediately
    Then the response code is "failed_precondition"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @blocked
  Scenario: EndTripEarly during a trip
    Given the trip status is "in_progress"
    When I call "ride.v1.trip.TripService/EndTripEarly" with trip_id, request_id, etag and a reason_code
    Then the response code is "ok"
    And the trip is completed at the current location

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @blocked
  Scenario: UpdateTrip with an update mask
    When I call "ride.v1.trip.TripService/UpdateTrip" with a trip and update_mask "notes"
    Then the response code is "ok"
    And only the masked fields are changed

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @negative @concurrency @blocked
  Scenario: Stale etag is rejected
    Given the trip was updated by the rider after I read it
    When I call "ride.v1.trip.TripService/StartTrip" with the old etag
    Then the response code is "aborted" or "failed_precondition"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @idempotency @blocked
  Scenario: Repeating CompleteTrip with the same request_id
    Given I completed the trip with request_id "<uuid>"
    When I call "ride.v1.trip.TripService/CompleteTrip" again with the same request_id
    Then the response code is "ok"
    And the rider is charged once

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @negative @blocked
  Scenario Outline: Lifecycle calls in the wrong order are rejected
    Given the trip status is "<status>"
    When I call "ride.v1.trip.TripService/<method>" for the trip
    Then the response code is "failed_precondition"

    Examples:
      | status    | method       |
      | accepted  | StartTrip    |
      | accepted  | CompleteTrip |
      | completed | MarkArrived  |
      | completed | CancelTrip   |

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @negative @blocked
  Scenario: Another driver cannot change the trip
    Given an authenticated session for a different driver
    When I call "ride.v1.trip.TripService/MarkArrived" for "<trip id>"
    Then the response code is "permission_denied"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @rating @negative @blocked
  Scenario Outline: SubmitRating validates the value
    Given the trip is completed
    When I call "ride.v1.trip.TripService/SubmitRating" with value <value>
    Then the response code is "invalid_argument"

    Examples:
      | value |
      | 0     |
      | 6     |

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @not-used-in-ui @blocked
  Scenario Outline: Stop, destination and tip endpoints
    When I call "ride.v1.trip.TripService/<method>" for an in-progress trip
    Then the response code is "ok"

    Examples:
      | method            |
      | AddStop           |
      | RemoveStop        |
      | ChangeDestination |
      | AddTip            |
