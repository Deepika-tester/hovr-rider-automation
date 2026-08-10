@rider @booking @core
Feature: Rider Ride Booking
  As a registered rider
  I want to book a ride to my destination
  So that I can travel conveniently within the GTA or Ottawa

  # Derived from: hovr-rider-app/lib/features/ride-booking/ and home-view/
  # Key blocs: RideBookingBloc, PlaceSearchBloc, PickupLocationCubit, TripPlannerBloc
  # Services: Ride service (ca.ride.api.prod.ridehovr.com), IAM service
  # States: RideBookingInitial -> Loading -> Success (FindRideResponse) -> NearbyDrivers
  # Trip model: CreatedTripModel (pickup LatLng+address, dropoff LatLng+address, optional stops)

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I have a valid payment method on file
    And I am located in a supported marketplace

  # --- DESTINATION SEARCH ---

  Scenario: Search for a destination from home screen
    Given I am on the ride home screen
    When I tap the "Where to?" search field
    Then I should see the plan your trip view
    And I should see my saved places if any exist
    And I should see my recent places if any exist

  Scenario: Enter a destination address
    Given I am on the plan your trip view
    When I type "100 Queen St W, Toronto" in the destination field
    Then I should see a list of address suggestions from Google Places API
    And the suggestions should be relevant to my search query

  Scenario: Select a destination from suggestions
    Given I have searched for "100 Queen St W, Toronto"
    And I see address suggestions
    When I tap on "100 Queen St W, Toronto, ON M5H 2N2"
    Then the destination should be set with latitude and longitude
    And I should be navigated to the route view with the map showing the route

  Scenario: Use a saved place as destination
    Given I have a saved place "Work" at "200 Bay St, Toronto"
    And I am on the ride home screen
    When I tap on my saved place "Work"
    Then the destination should be set to "200 Bay St, Toronto"
    And I should see the route on the map

  Scenario: Set pickup location manually
    Given I am on the set pickup location view
    When I drag the map pin to a new location
    And I tap "Confirm Pickup"
    Then my pickup location should be updated via reverse geocoding
    And the pickup address should reflect the new position

  # --- RIDE TYPE SELECTION ---

  Scenario: View available ride types with fare estimates
    Given I have set pickup at "Union Station, Toronto"
    And I have set destination at "Pearson Airport, Mississauga"
    When the route is calculated
    Then I should see the "Choose a Ride" bottom sheet
    And I should see ride type "HOVR" with an estimated fare
    And I should see ride type "HOVR XL" with an estimated fare
    And HOVR XL fare should be higher than HOVR fare
    And I should see the estimated arrival time for each ride type

  Scenario: Select HOVR ride type
    Given I see available ride types
    And "HOVR" is displayed with fare "$25.50"
    When I tap on "HOVR"
    Then "HOVR" should be highlighted as the selected ride type
    And the fare displayed should be "$25.50"

  Scenario: Select HOVR XL ride type
    Given I see available ride types
    When I tap on "HOVR XL"
    Then "HOVR XL" should be highlighted as the selected ride type
    And I should see the updated fare for HOVR XL

  Scenario: View fare breakdown
    Given I have selected ride type "HOVR"
    When I tap "View Breakdown"
    Then I should see the fare breakdown including:
      | component     |
      | Base fare     |
      | Distance      |
      | Time          |
      | Service fee   |
      | Total         |

  # --- CONFIRM AND BOOK ---

  Scenario: Confirm ride booking
    Given I have selected ride type "HOVR"
    And my payment method shows "Visa ...4242"
    When I tap "Confirm Ride"
    Then a FindRideRequest should be sent to the ride service
    And I should see "Finding your driver..." screen
    And the ride status should be "PENDING"

  Scenario: View payment method before confirming
    Given I am on the choose a ride bottom sheet
    Then I should see my default payment method card brand and last four digits
    When I tap on the payment method
    Then I should be navigated to change payment method

  Scenario: Confirm ride without payment method
    Given I do not have a payment method on file
    When I try to confirm a ride
    Then I should see a prompt to add a payment method
    And I should not be able to proceed until a payment method is added

  # --- FINDING DRIVER ---

  Scenario: Successfully find a nearby driver
    Given I have confirmed a ride booking
    And I am on the "Finding your driver" screen
    When a driver accepts the ride request
    Then I should see the driver's information
    And I should see the driver's estimated arrival time
    And I should be navigated to the "Meet at pickup spot" view

  Scenario: No drivers available
    Given I have confirmed a ride booking
    And there are no available drivers in my area
    When the search timeout is reached
    Then I should see a message "No drivers available at this time"
    And I should be given the option to try again or cancel

  Scenario: Cancel ride while searching for driver
    Given I am on the "Finding your driver" screen
    When I tap "Cancel"
    Then I should see the cancel confirmation bottom sheet
    When I confirm the cancellation
    Then the ride request should be cancelled
    And I should be returned to the home screen

  # --- NEARBY DRIVERS ---

  Scenario: See nearby drivers on map before booking
    Given I am on the ride home screen
    And there are active drivers in my area
    When the app polls for nearby drivers
    Then I should see driver icons on the map
    And the driver positions should update in real time

  Scenario: No nearby drivers visible
    Given I am on the ride home screen
    And there are no active drivers within the search radius
    Then I should not see any driver icons on the map

  # --- EDGE CASES ---

  Scenario: Booking a ride with a stop
    Given I have set pickup at "Union Station, Toronto"
    And I have set destination at "Pearson Airport, Mississauga"
    When I add a stop at "Yorkdale Mall, Toronto"
    And I confirm the ride
    Then the trip should be created with the stop included
    And the fare should reflect the additional stop

  Scenario: Network disconnection during booking
    Given I have selected a ride type and tapped "Confirm Ride"
    And the network disconnects before the request completes
    Then I should see a network error message
    And I should be able to retry the booking when connectivity returns

  Scenario: Ride booking with FindRideRequest error
    Given I have entered valid pickup and destination
    When I tap "Confirm Ride"
    And the ride service returns an error
    Then I should see an error message from RideBookingErrorState
    And I should remain on the route view to retry

  Scenario: Change destination after selecting ride type
    Given I have selected ride type "HOVR"
    When I tap the destination address to change it
    And I enter a new destination "CN Tower, Toronto"
    Then the fare should be recalculated
    And I should see updated ride type options
