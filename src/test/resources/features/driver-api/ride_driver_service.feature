@api @ride-driver
Feature: API - ride.v1.driver.DriverService, LocationService and RideService
  Going online, offers, sessions and scheduled rides.
  Callers: home_bloc, ride_activation_handler, ride_offer_handler, driver_profile_bloc,
  scheduled_trips_cubit, scheduled_trip_offer_cubit, core/isolates/driver_stream,
  core/services/location_stream_service.dart (location isolate).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session for an approved driver

  @smoke
  Scenario: GetDriverStatus returns the online state
    When I call "ride.v1.driver.DriverService/GetDriverStatus" with:
      | field   | value       |
      | user_id | <driver id> |
    Then the response code is "ok"
    And the response contains the driver online status

  @smoke
  Scenario: ActivateDriver sets the driver online
    Given the driver is offline
    When I call "ride.v1.driver.DriverService/ActivateDriver" with:
      | field     | value             |
      | driver_id | <driver id>       |
      | position  | 43.6532,-79.3832  |
    Then the response code is "ok"
    And "GetDriverStatus" shows the driver online

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @negative @blocked
  Scenario: ActivateDriver is refused for a driver with pending documents
    Given the driver has unapproved documents
    When I call "ride.v1.driver.DriverService/ActivateDriver" for the driver
    Then the response code is "failed_precondition"

  Scenario: DeactivateDriver sets the driver offline
    Given the driver is online
    When I call "ride.v1.driver.DriverService/DeactivateDriver" with:
      | field     | value            |
      | driver_id | <driver id>      |
      | position  | 43.6532,-79.3832 |
    Then the response code is "ok"
    And "GetDriverStatus" shows the driver offline

  # BLOCKED: is a bidirectional gRPC stream; the dev backend answers a unary JSON call with HTTP 415, so it cannot be driven from this client.
  @stream @blocked
  Scenario: StreamDriver delivers a ride offer
    Given the driver is online
    And I open the bidi stream "ride.v1.driver.DriverService/StreamDriver"
    When a rider requests a ride near the driver
    Then the stream receives an offer with a trip id

  # BLOCKED: is a bidirectional gRPC stream; the dev backend answers a unary JSON call with HTTP 415, so it cannot be driven from this client.
  @stream @blocked
  Scenario: StreamDriver reconnects after the connection drops
    Given an open "StreamDriver" stream
    When the network drops for 10 seconds
    Then the client reopens the stream
    And no offer is lost

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @offer @blocked
  Scenario: AcceptOffer assigns the trip to the driver
    Given the driver received an offer for trip "<trip id>"
    When I call "ride.v1.driver.DriverService/AcceptOffer" with:
      | field      | value            |
      | driver_id  | <driver id>      |
      | offer_name | <offer name>     |
      | trip_id    | <trip id>        |
      | position   | 43.6532,-79.3832 |
    Then the response code is "ok"
    And "ride.v1.trip.TripService/GetActiveTrip" returns trip "<trip id>"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @offer @blocked
  Scenario: RejectOffer frees the trip for other drivers
    Given the driver received an offer for trip "<trip id>"
    When I call "ride.v1.driver.DriverService/RejectOffer" with:
      | field       | value            |
      | offer_name  | <offer name>     |
      | trip_id     | <trip id>        |
      | reason_code | <reason code>    |
      | position    | 43.6532,-79.3832 |
    Then the response code is "ok"

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @offer @negative @blocked
  Scenario Outline: AcceptOffer fails for an offer that is no longer valid
    Given the offer for trip "<trip id>" is <state>
    When I call "ride.v1.driver.DriverService/AcceptOffer" for that offer
    Then the response code is "<code>"

    Examples:
      | state                  | code                |
      | expired                | failed_precondition |
      | cancelled by the rider | failed_precondition |
      | accepted by another driver | already_exists  |

  # BLOCKED: is a bidirectional gRPC stream; the dev backend answers a unary JSON call with HTTP 415, so it cannot be driven from this client.
  @location @stream @blocked
  Scenario: StreamLocation sends position updates
    Given the driver is online
    When I open the bidi stream "ride.v1.location.LocationService/StreamLocation"
    And I send 5 location updates 1 second apart
    Then the server accepts all updates
    And the driver position is updated on the server

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @earnings @blocked
  Scenario: ListDriverSessions pages through sessions
    When I call "ride.v1.driver.DriverService/ListDriverSessions" with:
      | field     | value       |
      | driver_id | <driver id> |
    Then the response code is "ok"
    When I call it again with the returned "next_pagination_token"
    Then the next page of sessions is returned

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @scheduled @blocked
  Scenario: ListUpcomingScheduledRides and AcceptScheduledRide
    When I call "ride.v1.driver.DriverService/ListUpcomingScheduledRides" with:
      | field       | value       |
      | driver_id   | <driver id> |
      | active_only | true        |
    Then the response code is "ok"
    When I call "ride.v1.driver.DriverService/AcceptScheduledRide" with:
      | field      | value        |
      | request_id | <uuid>       |
      | booking_id | <booking id> |
    Then the response code is "ok"
    And the booking is listed as accepted

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @scheduled @idempotency @blocked
  Scenario: AcceptScheduledRide is idempotent on request_id
    Given I accepted booking "<booking id>" with request_id "<uuid>"
    When I call "ride.v1.driver.DriverService/AcceptScheduledRide" again with the same request_id
    Then the response code is "ok"
    And the booking is accepted only once

  @scheduled
  Scenario: ListScheduledRides near the driver
    When I call "ride.v1.ride.RideService/ListScheduledRides" with:
      | field           | value            |
      | marketplace_id  | <marketplace id> |
      | driver_location | 43.6532,-79.3832 |
    Then the response code is "ok"
