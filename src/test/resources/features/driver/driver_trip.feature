@driver @trip @core
Feature: Driver Trip Lifecycle
  As a HOVR driver who has accepted a ride
  I want to complete the trip from pickup to drop-off
  So that I can earn my fare

  # Derived from: hovr-driver-app/lib/features/trip-feature/ and driver-ride-feature/
  # Trip state machine (driver side):
  #   AcceptOffer -> NavigationTowardsPickUp -> MarkArriving -> DriverArrivedAtPickup ->
  #   DriverWaiting -> StartTrip -> HeadingTowardsDropOff -> CompleteTrip -> SubmitRating
  # Events: NavigationTowardsPickUpEvent, MarkArrivingEvent, MarkArrivedEvent,
  #         DriverWaitingEvent, StartTripEvent, HeadingTowardsDropOffEvent,
  #         CompleteTripEvent, ReportNoShowEvent, SubmitRatingEvent
  # Chat: TripStreamRequest_OutboundChatMessage
  # UI: navigation_using_google_map_view.dart, driver_reached_at_pickup_view.dart,
  #     driver_waiting_bottom_sheet.dart, heading_destination_bottom_sheet.dart,
  #     give_rating_view.dart

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app
    And I have accepted a ride request

  # --- NAVIGATE TO PICKUP ---

  Scenario: Navigate to rider pickup location
    Given I have accepted a ride
    When the navigation view opens
    Then I should see turn-by-turn navigation to the pickup location
    And the NavigationTowardsPickUpState should be active
    And I should see the rider's pickup address
    And I should see the estimated time to pickup

  Scenario: Open navigation in external app
    Given I am navigating to the pickup
    When I tap the external navigation button
    Then I should see options to open in:
      | app             |
      | Google Maps      |
      | Apple Maps       |
      | Waze             |
    When I select an app
    Then the navigation should open in the selected app

  Scenario: Mark as arriving
    Given I am close to the pickup location
    When I trigger the arriving event
    Then a MarkArrivingEvent should be dispatched
    And the rider should be notified that I am arriving

  # --- ARRIVE AT PICKUP ---

  Scenario: Mark as arrived at pickup
    Given I am at the rider's pickup location
    When I tap "I've Arrived"
    Then a MarkArrivedEvent should be dispatched
    And I should see the DriverArrivedAtPickupState
    And the rider should receive a notification "Your driver has arrived"
    And I should transition to the waiting view

  Scenario: Wait for rider at pickup
    Given I have arrived at the pickup location
    Then I should see the driver waiting bottom sheet
    And I should see a wait timer
    And I should see the rider's name
    And I should have options to:
      | action        |
      | Call rider     |
      | Chat with rider|
      | Report no-show |

  # --- CHAT WITH RIDER ---

  Scenario: Send chat message to rider
    Given I am waiting for the rider at pickup
    When I open the chat feature
    And I send a message "I'm at the front entrance"
    Then the message should be sent via TripStreamRequest
    And the rider should receive the message

  Scenario: Receive chat message from rider
    Given I am waiting for the rider
    When the rider sends a chat message "Coming down now"
    Then I should receive a ChatMessageReceivedState
    And I should see the message in the chat view

  # --- REPORT NO-SHOW ---

  Scenario: Report rider no-show
    Given I have been waiting at the pickup for the required time
    When I tap "Report No-Show"
    Then a ReportNoShowEvent should be dispatched
    And I should see the ReportNoShowState
    And the trip should be cancelled with a no-show fee charged to the rider
    And I should return to the online waiting state

  Scenario: Cannot report no-show before minimum wait
    Given I just arrived at the pickup
    When I try to report a no-show
    Then the option should be disabled
    And I should see the remaining wait time before I can report

  # --- START TRIP ---

  Scenario: Start trip when rider is in the car
    Given the rider has entered my vehicle
    When I swipe to start the trip
    Then a StartTripEvent should be dispatched
    And I should see the StartTripState
    And I should transition to the navigation towards drop-off view
    And the trip meter should begin

  Scenario: Confirm rider identity before starting
    Given the rider has approached my vehicle
    Then I should verify the rider's identity
    When I confirm the rider matches the booking
    And I swipe to start
    Then the trip should begin

  # --- NAVIGATE TO DROP-OFF ---

  Scenario: Navigate to rider drop-off location
    Given the trip has started
    When the heading destination view loads
    Then I should see turn-by-turn navigation to the drop-off
    And I should see the HeadingDestinationState
    And I should see:
      | field                |
      | Drop-off address     |
      | Estimated time       |
      | Estimated distance   |
      | Current fare         |

  Scenario: Trip stream updates during ride
    Given the trip is in progress
    Then the TripStreamUpdate should provide real-time updates
    And the fare should update as distance and time accrue
    And my location should be visible to the rider

  # --- COMPLETE TRIP ---

  Scenario: Complete trip at destination
    Given I have arrived at the drop-off location
    When I tap "Complete Trip"
    Then a CompleteTripEvent should be dispatched
    And the trip should be marked as completed
    And I should see the final fare amount
    And I should transition to the rating view

  Scenario: End trip early
    Given the trip is in progress
    And the rider requests to be dropped off at a different location
    When I confirm the early end
    Then the trip should end at the current location
    And the fare should be calculated for the actual distance traveled

  # --- RATE RIDER ---

  Scenario: Rate rider after trip completion
    Given the trip is completed
    And I am on the rating screen
    When I select a 5-star rating for the rider
    And I tap "Submit"
    Then a SubmitRatingEvent should be dispatched
    And I should see the SubmitRatingState
    And I should return to the online waiting state

  Scenario: Rate rider with compliments
    Given I am on the rating screen
    When I select a 5-star rating
    And I add compliment "Polite rider"
    And I submit
    Then the rating with compliment should be recorded

  Scenario: Rate rider with complaints
    Given I am on the rating screen
    When I select a 2-star rating
    And I see the complaint options
    When I select a complaint reason
    And I submit
    Then the rating with complaint should be recorded

  # --- ACTIVE TRIP RECOVERY ---

  Scenario: Resume trip after app restart
    Given I have an active trip in progress
    When the app is restarted
    Then the app should check for active trips via GetActiveTripEvent
    And the active trip should be restored
    And I should be returned to the correct trip state

  Scenario: Check for last trip on app launch
    Given I launch the app
    When a CheckLastTripEvent is dispatched
    Then the app should check if there is an incomplete trip
    And restore to the appropriate state if one exists

  # --- LIST TRIPS ---

  Scenario: View completed trips
    Given I have completed trips today
    When a ListTripsEvent is dispatched
    Then I should see my completed trips list

  # --- EDGE CASES ---

  Scenario: GPS lost during navigation
    Given I am navigating to the pickup location
    When GPS signal is lost
    Then I should see a warning about GPS unavailability
    And the last known position should be displayed

  Scenario: Trip stream disconnects mid-ride
    Given the trip is in progress
    When the trip stream disconnects
    Then the app should attempt to reconnect
    And trip data should be preserved locally
    When the connection is restored
    Then the trip should continue seamlessly

  Scenario: Rider cancels after driver starts navigating
    Given I am navigating to the pickup
    When the rider cancels the trip
    Then I should receive a cancellation notification
    And I should see the TripCancelled state
    And I should return to the online waiting state
    And I should receive any applicable cancellation fee
