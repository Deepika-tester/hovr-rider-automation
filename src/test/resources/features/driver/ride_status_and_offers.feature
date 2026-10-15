@ride @home
Feature: Going online and receiving ride offers
  As an approved driver
  I want to go online and receive ride requests
  So that I can earn money

  Background:
    Given the driver is logged in
    And the driver is approved for rideshare
    And location permission is granted
    And the driver is on the home screen

  @smoke
  Scenario: Driver is offline by default
    Then the text "You're offline" is shown
    And the text "Go online to get ride requests" is shown
    And the "Go Online" button is shown

  @smoke
  Scenario: Driver goes online
    When the driver taps "Go Online"
    Then the text "You're online" is shown
    And the text "Finding rides" is shown
    And the driver location stream is started

  @smoke
  Scenario: Driver goes offline
    Given the driver is online
    When the driver taps "Go Offline"
    Then the text "You're offline" is shown
    And the driver location stream is stopped

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @negative @blocked
  Scenario: Driver with pending actions cannot go online
    Given the driver has pending actions
    When the driver taps "Go Online"
    Then the text "Can't go online pending actions or not approved!" is shown
    And the driver stays offline

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @negative @blocked
  Scenario: Driver blocked by duty policy
    Given the driver is blocked by the duty policy
    When the driver taps "Go Online"
    Then the text "Driver blocked by duty policy!" is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @negative @blocked
  Scenario: Activation fails
    Given the activate driver request returns an error
    When the driver taps "Go Online"
    Then the text "Something went wrong while activating driver!" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @smoke @blocked
  Scenario: Driver accepts a ride offer
    Given the driver is online
    When a ride offer is received
    Then the ride offer bottom sheet is shown
    And the pickup and dropoff are shown
    And the estimated fare is shown
    When the driver taps "Accept"
    Then the text "Drive to pickup spot" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @blocked
  Scenario: Driver declines a ride offer
    Given the driver is online
    When a ride offer is received
    And the driver declines the offer
    Then the ride offer bottom sheet is closed
    And the text "Finding rides" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @blocked
  Scenario: Ride offer expires
    Given the driver is online
    When a ride offer is received
    And the driver does not respond before the offer times out
    Then the text "Offer expired" is shown
    And the driver keeps receiving offers

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @offer @negative @blocked
  Scenario: Accepted trip is no longer available
    Given the driver is online
    And a ride offer is received
    And the rider cancels before the driver accepts
    When the driver taps "Accept"
    Then the text "Trip is no longer available!" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @resume @blocked
  Scenario: Active trip is restored after app restart
    Given the driver has an active trip
    When the driver kills and relaunches the app
    Then the active trip screen is restored

  @logout
  Scenario: Driver logs out while online
    Given the driver is online
    When the driver logs out
    Then the driver is set offline
    And all trip and location streams are closed
    And the landing page is shown

  # BLOCKED: needs a 2-minute background wait while online; not run so the driver is not left receiving requests.
  @background @blocked
  Scenario: Driver location updates continue in the background
    Given the driver is online
    When the app is sent to the background for 2 minutes
    Then location updates are still sent
    When the app is brought to the foreground
    Then the text "You're online" is shown
