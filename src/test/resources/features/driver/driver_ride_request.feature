@driver @riderequest @core
Feature: Driver Ride Request Handling
  As an online HOVR driver
  I want to receive and respond to ride requests
  So that I can accept or decline trips

  # Derived from: hovr-driver-app/lib/features/driver-ride-feature/
  # Events: DriverOfferEvent, AcceptOfferEvent, RejectOfferEvent, DriverOfferExpiredEvent
  # States: DriverOfferState (DispatchOffer), AcceptOfferState, RejectOfferState, DriverOfferExpiredState
  # UI: found_ride_bottom_sheet.dart
  # Dispatch: Offer sent via DriverStreamResponse_DispatchOffer (contains pickup, dropoff, fare, distance)

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app
    And I am online and available for rides

  # --- RECEIVE RIDE REQUEST ---

  Scenario: Receive a ride request via dispatch
    Given I am online with an active driver stream
    When a rider requests a ride near my location
    Then I should receive a DriverOfferState with the dispatch offer
    And I should see the "Found Ride" bottom sheet
    And I should see:
      | field              |
      | Pickup address     |
      | Drop-off address   |
      | Estimated fare     |
      | Estimated distance |
      | Estimated time     |
    And I should hear a notification sound
    And I should feel haptic feedback

  Scenario: View ride request details
    Given I have received a ride request
    Then I should see the rider's pickup location on the map
    And I should see the route preview
    And I should see a countdown timer for the offer

  # --- ACCEPT RIDE ---

  Scenario: Accept a ride request
    Given I have received a ride request
    When I tap "Accept"
    Then an AcceptOfferRequest should be sent
    And I should receive an AcceptOfferState response
    And I should transition to the navigation-to-pickup state
    And the timer should stop

  Scenario: Accept ride request via haptic swipe
    Given I have received a ride request
    When I swipe to accept
    Then the ride should be accepted
    And I should begin navigating to the pickup location

  # --- DECLINE RIDE ---

  Scenario: Decline a ride request
    Given I have received a ride request
    When I tap "Decline"
    Then I should see the decline ride bottom sheet
    And I should see decline reason options
    When I select a reason and confirm
    Then a RejectOfferRequest should be sent
    And I should return to the online waiting state
    And I should be available for the next ride request

  Scenario: Decline ride without reason
    Given I have received a ride request
    When I tap "Decline"
    And I skip selecting a reason
    Then the ride should still be rejected
    And I should return to the online waiting state

  # --- OFFER TIMEOUT ---

  Scenario: Ride request times out
    Given I have received a ride request
    And the offer countdown timer is running
    When the timer reaches zero without my response
    Then a DriverOfferExpiredEvent should be received
    And I should see the DriverOfferExpiredState
    And the ride request should be automatically declined
    And I should return to the online waiting state

  # --- MULTIPLE REQUESTS ---

  Scenario: Receive ride request while viewing previous
    Given I have received a ride request that I have not yet responded to
    When the offer expires
    And a new ride request comes in
    Then I should see the new ride request details
    And the previous request should be cleared

  Scenario: No ride requests received
    Given I am online for 30 minutes
    And no ride requests are dispatched to me
    Then I should remain on the online waiting screen
    And the driver stream should continue with heartbeats
    And my location should continue streaming

  # --- EDGE CASES ---

  Scenario: Network drop during ride acceptance
    Given I have received a ride request
    When I tap "Accept"
    And the network drops before the response is sent
    Then the app should queue the acceptance and retry when connectivity returns, or the offer should expire and I should be notified

  Scenario: Receive ride request while app is backgrounded
    Given I am online
    And the app is in the background
    When a ride request is dispatched to me
    Then I should receive a push notification
    And the notification should bring me to the ride request screen

  Scenario: Rapid consecutive ride declines
    Given I decline 3 ride requests in a row
    Then I should still remain online
    And I should continue receiving ride requests
    But the dispatch algorithm may prioritize other drivers
