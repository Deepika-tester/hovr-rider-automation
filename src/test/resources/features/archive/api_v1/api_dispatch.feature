@api @dispatch @backend
Feature: API Dispatch and Driver Matching
  As the HOVR platform
  I want to dispatch ride requests to the nearest available drivers
  So that riders get matched quickly and efficiently

  # Derived from: ride-service (Go) - ride_service/ride.go, find_nearby_drivers.go
  # Infrastructure: geoindex (Redis-backed spatial index), realtime (Centrifuge WebSocket),
  #                 DriverStreamResponse_DispatchOffer
  # Matching: Location-based using geoindex, streamed via Centrifuge pub/sub
  # Services: ca.ride.api.prod.ridehovr.com (ride service), location service

  Background:
    Given the ride service is running
    And the geoindex is initialized with Redis
    And the realtime WebSocket server is running

  # --- FIND NEARBY DRIVERS ---

  Scenario: Find nearby drivers for a rider location
    Given there are 5 online drivers within 5km of coordinates (43.6532, -79.3832)
    When a FindNearbyDriversRequest is sent for location (43.6532, -79.3832)
    Then the response should contain up to 5 nearby drivers
    And each driver should include their current location coordinates
    And drivers should be sorted by distance from the rider

  Scenario: No drivers available in area
    Given there are no online drivers within the search radius of (45.4215, -75.6972)
    When a FindNearbyDriversRequest is sent
    Then the response should return an empty list
    And no error should be thrown

  Scenario: Find nearby drivers across marketplace boundary
    Given a rider is on the edge of the GTA marketplace boundary
    When a FindNearbyDriversRequest is sent
    Then only drivers within the GTA marketplace should be returned
    And drivers from adjacent marketplaces should not appear

  # --- RIDE REQUEST DISPATCH ---

  Scenario: Dispatch ride to nearest available driver
    Given rider requests a ride from (43.6532, -79.3832) to (43.6426, -79.3871)
    When a FindRideRequest is submitted
    Then the dispatch engine should identify the nearest available driver
    And a DispatchOffer should be sent to that driver via the driver stream
    And the offer should include:
      | field            |
      | Pickup location  |
      | Drop-off location|
      | Estimated fare   |
      | Distance         |

  Scenario: Driver accepts dispatched offer
    Given a DispatchOffer has been sent to a driver
    When the driver sends an AcceptOfferRequest
    Then the system should confirm the acceptance
    And a trip should be created linking the rider and driver
    And the rider should be notified of the driver assignment

  Scenario: Driver rejects dispatched offer
    Given a DispatchOffer has been sent to a driver
    When the driver sends a RejectOfferRequest
    Then the offer should be marked as rejected
    And the dispatch engine should attempt to match the next nearest driver

  Scenario: Dispatch offer expires without response
    Given a DispatchOffer has been sent to a driver
    When the offer timeout period elapses
    Then the offer should expire
    And the dispatch engine should cascade to the next nearest driver

  Scenario: All nearby drivers reject or timeout
    Given a ride request has been dispatched
    And all nearby drivers have either rejected or timed out
    Then the rider should be notified that no driver is available
    And the ride request should be cancelled or queued for retry

  # --- LOCATION STREAMING ---

  Scenario: Driver location is updated in geoindex
    Given a driver is online with ID "driver-123"
    When the driver streams their location update (43.6540, -79.3840)
    Then the geoindex should update the driver's position
    And subsequent FindNearbyDrivers queries should reflect the new position

  Scenario: Driver goes offline and is removed from geoindex
    Given a driver "driver-123" is online and in the geoindex
    When the driver sends a DeactivateDriverRequest
    Then the driver should be removed from the geoindex
    And the driver should no longer appear in FindNearbyDrivers results

  # --- TRIP CREATION ---

  Scenario: Create trip after driver accepts
    Given a driver has accepted a ride offer
    When a CreateTripRequest is processed
    Then a trip should be created with:
      | field            | value          |
      | status           | CREATED        |
      | pickup_location  | rider_location |
      | dropoff_location | destination    |
      | driver_id        | assigned_driver|
      | rider_id         | requesting_rider|
    And a TripStream should be established between rider and driver

  Scenario: Cancel trip before pickup
    Given a trip has been created but the driver has not arrived
    When a CancelTripRequest is sent
    Then the trip status should change to CANCELLED
    And both rider and driver should be notified
    And the driver should return to the available pool

  # --- REAL-TIME COMMUNICATION ---

  Scenario: Trip stream provides real-time updates
    Given an active trip between rider and driver
    When either party sends a TripStreamRequest
    Then the counterparty should receive a TripStreamResponse
    And location updates should propagate in real time

  Scenario: WebSocket connection recovery
    Given the real-time WebSocket connection drops
    When the client reconnects
    Then the Centrifuge server should resume the channel subscription
    And any missed messages should be recovered via Redis history

  # --- ETA CALCULATION ---

  Scenario: Calculate driver ETA to pickup
    Given a driver is 3.5km from the pickup location
    When the trip is created
    Then the system should calculate an estimated time of arrival
    And the ETA should account for current traffic conditions

  # --- EDGE CASES ---

  Scenario: Concurrent ride requests for same driver
    Given driver "driver-123" is the nearest to two simultaneous ride requests
    When both FindRideRequests are processed
    Then only one should be dispatched to driver "driver-123"
    And the other should cascade to the next nearest driver

  Scenario: Driver location stale (no recent update)
    Given driver "driver-123" has not sent a location update in 5 minutes
    When a FindNearbyDrivers query runs
    Then the stale driver should either be excluded or flagged

  Scenario: Invalid coordinates in FindRideRequest
    Given a FindRideRequest with coordinates (0.0, 0.0)
    When the request is processed
    Then the service should return a validation error
    And no dispatch should be attempted
