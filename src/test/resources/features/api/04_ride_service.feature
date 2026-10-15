@api @ride
Feature: RideService API
  Ride quotes, scheduling and nearby drivers.
  Client: RideServiceClient (ride_remote_data_source.dart, nearby_drivers_isolate_worker.dart)

  Background:
    Given I am authenticated as a rider with a default payment method

  @smoke
  Scenario: Find ride options for a valid route
    When I call "RideService/FindRide" with:
      | pickup  | 43.6453,-79.3806 |
      | dropoff | 43.6629,-79.3957 |
    Then the response status is "OK"
    And the response contains at least one ride service
    And each ride service has a fare estimate and pickup ETA

  Scenario: Find ride with pickup outside service area
    When I call "RideService/FindRide" with pickup outside the service area
    Then the response status is "FAILED_PRECONDITION" or "OUT_OF_RANGE"

  Scenario: Find ride with no drivable route
    When I call "RideService/FindRide" with a dropoff unreachable by road
    Then the response status is "NOT_FOUND"

  Scenario: Find ride with multiple stops
    When I call "RideService/FindRide" with 2 intermediate stops
    Then the fare estimate includes all stops

  # BLOCKED: would create a real ride request, scheduled trip or paid hold on the shared dev backend; held until the ride-confirming scenarios are approved.
  @blocked
  Scenario: Schedule a ride
    When I call "RideService/ScheduleRide" with a pickup time 2 hours from now
    Then the response status is "OK"
    And the response contains a scheduled trip id

  Scenario Outline: Schedule ride with invalid time
    When I call "RideService/ScheduleRide" with a pickup time <time>
    Then the response status is "INVALID_ARGUMENT"

    Examples:
      | time                   |
      | 5 minutes from now     |
      | 8 days from now        |
      | 1 hour in the past     |

  Scenario: Find nearby drivers
    When I call "RideService/FindNearbyDrivers" with my current location
    Then the response status is "OK"
    And each driver has a location and a heading
