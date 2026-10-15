@api @co-commute
Feature: NeighbourRiderService API (Co-Commute)
  Client: NeighbourRiderServiceClient (co_commute_feature_remote_data_source.dart)

  Background:
    Given I am authenticated as a rider with a default payment method

  @smoke
  Scenario: Search neighbour trips
    When I call "NeighbourRiderService/SearchNeighbourTrips" with my location
    Then the response status is "OK"
    And each journey has leave time, seats left and price

  Scenario: Get neighbour trip
    Given a neighbour journey exists
    When I call "NeighbourRiderService/GetNeighbourTrip" with its id
    Then the response contains driver, route and seats

  Scenario: Get platform fee
    When I call "NeighbourRiderService/GetNeighbourPlatformFee"
    Then the response contains the held fee amount

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @smoke @blocked
  Scenario: Book a seat
    Given a neighbour journey with 2 seats left
    When I call "NeighbourRiderService/BookNeighbourSeat"
    Then the response status is "OK"
    And the booking status is "PENDING_APPROVAL" or "CONFIRMED"
    And the platform fee is held, not captured
    And the journey has 1 seat left

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Book a full journey
    Given a neighbour journey with 0 seats left
    When I call "NeighbourRiderService/BookNeighbourSeat"
    Then the response status is "FAILED_PRECONDITION" or "RESOURCE_EXHAUSTED"

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Concurrent booking of the last seat
    Given a neighbour journey with 1 seat left
    When two riders call "BookNeighbourSeat" at the same time
    Then exactly one booking succeeds
    And the other fails with a "seat taken" error

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Book with declined card
    Given my default card will be declined
    When I call "NeighbourRiderService/BookNeighbourSeat"
    Then the response indicates a declined payment
    And no booking is created

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Cancel a booking
    Given I have a confirmed neighbour booking
    When I call "NeighbourRiderService/CancelNeighbourBooking"
    Then the booking status becomes "CANCELED"
    And the held fee is released

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: List my bookings
    Given I have 2 neighbour bookings
    When I call "NeighbourRiderService/ListMyNeighbourBookings"
    Then the response contains 2 bookings

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Post a commute request
    When I call "NeighbourRiderService/PostNeighbourRequest" with pickup, dropoff, leave time and seats 1
    Then the response status is "OK"
    And the request status is "OPEN"

  Scenario: Post a commute request without leave time
    When I call "NeighbourRiderService/PostNeighbourRequest" without leave time
    Then the response status is "INVALID_ARGUMENT"

  # BLOCKED: needs a real Co-Commute booking or posted request (a held payment); not created on the shared dev backend. (Posting a commute also timed out on the server in the UI run.)
  @blocked
  Scenario: Cancel a commute request
    Given I have an open commute request
    When I call "NeighbourRiderService/CancelNeighbourRequest"
    Then the request status becomes "CANCELED"
    And ListMyNeighbourRequests shows it as canceled
