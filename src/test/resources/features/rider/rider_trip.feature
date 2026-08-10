@rider @trip @core
Feature: Rider Trip Lifecycle
  As a rider with a confirmed booking
  I want to track my ride from pickup to drop-off
  So that I know where my driver is and when I will arrive

  # Derived from: hovr-rider-app/lib/features/trip-feature/ and ride-booking/
  # Trip state machine: FindingRide -> MeetAtPickupSpot -> DriverWaitingAtPickupSpot ->
  #                     HeadingTowardsDestination -> Rating -> Complete
  # Streams: TripStreamResponse (real-time updates via gRPC bidirectional stream)
  # Chat: TripStreamRequest_OutboundChatMessage for in-trip messaging

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I have a valid payment method
    And I have confirmed a ride booking

  # --- FINDING RIDE STATE ---

  Scenario: Ride is in Finding Ride state
    Given my ride request has been submitted
    When the system is searching for a driver
    Then I should see the "Finding your driver" view
    And I should see a loading animation
    And the trip should have a valid trip ID

  Scenario: Cancel ride while finding driver
    Given I am in the Finding Ride state
    When I tap "Cancel Ride"
    Then I should see the cancel confirmation bottom sheet
    When I confirm the cancellation
    Then a CancelTripRequest should be sent
    And the trip status should change to TripCancelled
    And I should be returned to the home screen

  # --- DRIVER ASSIGNED / MEET AT PICKUP ---

  Scenario: Driver is assigned and heading to pickup
    Given a driver has accepted my ride request
    When the TripStreamResponse contains an assignment
    Then I should transition to the "Meet at Pickup Spot" view
    And I should see the driver's name and profile photo
    And I should see the driver's vehicle make, model, and license plate
    And I should see the driver's estimated time of arrival
    And I should see the driver's location updating on the map in real time

  Scenario: View driver info during pickup approach
    Given I am in the "Meet at Pickup Spot" state
    Then I should see the driver's star rating
    And I should see the vehicle color
    And I should be able to call the driver
    And I should be able to chat with the driver

  # --- DRIVER WAITING AT PICKUP ---

  Scenario: Driver arrives at pickup location
    Given the driver is approaching my pickup location
    When the driver marks themselves as arrived
    Then I should see the "Driver Waiting at Pickup" view
    And I should receive a notification "Your driver has arrived"
    And I should see a timer or waiting indicator

  Scenario: Send chat message to waiting driver
    Given the driver is waiting at my pickup location
    When I open the chat feature
    And I send a message "Coming down in 2 minutes"
    Then the message should be sent via TripStreamRequest
    And the driver should receive the chat message

  # --- TRIP IN PROGRESS ---

  Scenario: Trip starts when rider is picked up
    Given the driver has arrived at my pickup
    When the driver starts the trip
    Then I should transition to the "Heading Towards Destination" view
    And I should see the route to my destination on the map
    And I should see the estimated time of arrival
    And I should see the driver and car information bar

  Scenario: Real-time trip tracking
    Given the trip is in progress
    Then I should see the driver's position update in real time on the map
    And the ETA should update as the driver progresses
    And the route visualization should reflect the current position

  Scenario: Cancel trip early during ride
    Given the trip is in progress
    When I tap "End Trip Early"
    Then an EndTripEarlyRequest should be sent
    And the trip should be ended at the current location
    And I should be charged for the distance traveled

  # --- TRIP COMPLETION ---

  Scenario: Trip completed successfully
    Given the trip is in progress
    When the driver arrives at my destination
    And the driver marks the trip as complete
    Then I should transition to the Rating state
    And I should see the trip summary with the final fare
    And I should be prompted to rate the driver

  Scenario: View trip receipt after completion
    Given my trip has been completed
    When I navigate to my trips list
    And I tap on the completed trip
    Then I should see the trip receipt with:
      | field             |
      | Pickup address    |
      | Drop-off address  |
      | Trip date/time    |
      | Distance          |
      | Duration          |
      | Fare breakdown    |
      | Total charged     |
      | Payment method    |
      | Driver name       |

  # --- ACTIVE TRIP RECOVERY ---

  Scenario: Resume active trip after app restart
    Given I have an active trip in progress
    When the app is killed and relaunched
    Then a GetActiveTripEvent should be dispatched
    And I should see the active trip state restored
    And the trip tracking should resume from the current state

  Scenario: Resume finding ride state after app restart
    Given I have a trip in "Finding Ride" state
    When the app is relaunched
    Then I should see the "Finding your driver" view
    And the trip stream should reconnect

  # --- CHAT DURING TRIP ---

  Scenario: Receive chat message from driver
    Given the trip is in progress
    When the driver sends a chat message
    Then I should receive a ChatMessageReceivedState update
    And I should see a notification for the new message
    And I should be able to open the chat to read and reply

  # --- EDGE CASES ---

  Scenario: Driver cancels after being assigned
    Given a driver was assigned to my ride
    When the driver cancels the trip
    Then I should see a notification "Your driver cancelled"
    And I should either see the system automatically search for a new driver, or be returned to the home screen with the option to rebook

  Scenario: Trip stream disconnects during ride
    Given the trip is in progress
    When the trip stream connection drops
    Then the app should attempt to reconnect automatically
    And I should see a "Reconnecting..." indicator
    When the connection is restored
    Then trip tracking should resume seamlessly

  Scenario: No-show by rider
    Given the driver has arrived at the pickup location
    And the driver has waited beyond the no-show threshold
    When the driver reports a no-show
    Then the trip should be cancelled
    And I should be charged a no-show fee
    And I should see the no-show charge in my trip history
