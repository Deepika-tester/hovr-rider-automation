@driver @online @core
Feature: Driver Go Online/Offline
  As an approved HOVR driver
  I want to toggle my availability status
  So that I can receive ride requests when I am ready to drive

  # Derived from: hovr-driver-app/lib/features/driver-ride-feature/
  # Events: ActivateDriverEvent, DeactivateDriverEvent, DriverStreamEvent
  # States: ActiveDriverState, DeactivateDriverState, DriverIsActiveAlreadyState,
  #         DriverStreamState, DriverStatusState, DriverHeartbeatState
  # UI: initial_status_bottom_sheet.dart, driver_status_bottom_sheet.dart
  # Stream: DriverStreamResponse (bidirectional gRPC stream with heartbeat)
  # Location: LocationServiceBloc for real-time location streaming

  Background:
    Given I am an approved HOVR driver
    And I have completed all onboarding requirements
    And I have an active membership
    And I am logged into the driver app

  # --- GO ONLINE ---

  Scenario: Go online successfully
    Given I am on the driver home screen
    And I am currently offline
    When I tap the "Go Online" button
    Then an ActivateDriverRequest should be sent
    And I should receive an ActiveDriverState response
    And the driver stream should start (DriverStreamEvent)
    And my status should change to "Online"
    And I should see the online status indicator
    And my location should begin streaming to the ride service

  Scenario: Go online when already online
    Given I am currently online
    When I try to go online again
    Then I should see the DriverIsActiveAlreadyState
    And no duplicate stream should be created

  Scenario: Go online with location services disabled
    Given I am on the home screen
    And location services are disabled on my device
    When I tap "Go Online"
    Then I should see a prompt to enable location services
    And I should not go online until location is enabled

  Scenario: Go online with expired documents
    Given one of my required documents has expired
    When I try to go online
    Then I should see a message about the expired document
    And I should be directed to update the expired document
    And I should not be able to go online

  # --- GO OFFLINE ---

  Scenario: Go offline successfully
    Given I am currently online
    And I have no active trip
    When I tap the "Go Offline" button
    Then a DeactivateDriverRequest should be sent
    And I should receive a DeactivateDriverState response
    And my status should change to "Offline"
    And the driver stream should stop
    And my location should stop streaming

  Scenario: Attempt to go offline during active trip
    Given I am currently online
    And I have an active trip in progress
    When I try to go offline
    Then I should see a message "Complete your current trip first"
    And I should remain online

  # --- DRIVER STREAM ---

  Scenario: Maintain heartbeat while online
    Given I am online
    Then the driver stream should send periodic heartbeat signals
    And I should receive DriverHeartbeatState responses
    And the app should maintain the connection

  Scenario: Stream disconnection recovery
    Given I am online
    When the driver stream disconnects
    Then the app should attempt to reconnect automatically
    And I should see a "Reconnecting..." indicator
    When the stream reconnects
    Then my online status should be restored

  Scenario: Stream status update received
    Given I am online
    When a DriverStreamResponse_StreamStatus is received
    Then the driver status should be updated in the UI
    And the status should reflect the current driver state

  # --- LOCATION TRACKING ---

  Scenario: Location updates streamed while online
    Given I am online
    Then my GPS location should be streamed to the ride service
    And location updates should occur at regular intervals
    And I should see my current position on the map

  Scenario: Poor GPS accuracy while online
    Given I am online
    And GPS accuracy degrades below the threshold
    Then I should see a warning about poor GPS
    And the system should handle location interpolation

  # --- STATUS BOTTOM SHEET ---

  Scenario: View initial status bottom sheet
    Given I am on the home screen after launching the app
    Then I should see the initial status bottom sheet
    And I should see the "Go Online" toggle
    And I should see my current earnings summary

  Scenario: View online status bottom sheet
    Given I am online
    Then I should see the online status bottom sheet
    And I should see my availability status
    And I should see the "Go Offline" button
    And I should see a map with my current position

  # --- EDGE CASES ---

  Scenario: Battery saver mode interferes with location
    Given I am online
    When the device enters battery saver mode
    Then the app should warn about potential location update delays
    And the driver should remain technically online

  Scenario: App backgrounded while online
    Given I am online
    When the app goes to the background
    Then the background service should maintain location streaming
    And the driver stream should remain active
    And I should still receive ride requests via push notification

  Scenario: Membership expired while online
    Given I am online
    And my membership expires
    Then I should receive a notification about membership expiry
    And I should be prompted to renew
