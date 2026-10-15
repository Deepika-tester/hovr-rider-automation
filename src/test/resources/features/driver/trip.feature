@trip
Feature: Completing a rideshare trip
  As a driver with an accepted ride
  I want to pick up, drive and drop off my rider
  So that the trip is completed and I get paid

  Background:
    Given the driver is logged in
    And the driver is online
    And the driver has accepted a ride offer

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @smoke @blocked
  Scenario: Full trip from pickup to drop off
    Then the text "Drive to pickup spot" is shown
    When the driver reaches the pickup location
    And the driver swipes to "Arrive"
    Then the driver waiting bottom sheet is shown
    When the rider gets in
    And the driver starts the trip
    Then the text "Heading to Destination" is shown
    When the driver reaches the destination
    And the driver swipes to "Drop Off"
    Then the rating screen is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @navigation @blocked
  Scenario Outline: Driver chooses a navigation app
    Given the driver is heading to the pickup
    When the driver opens navigation
    Then the "Navigate with" sheet is shown
    When the driver chooses "<app>"
    Then navigation opens with "<app>"

    Examples:
      | app                |
      | Google Maps        |
      | In App Navigation  |

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @navigation @ios @blocked
  Scenario: Apple Maps is offered on iOS
    Given the device is an iPhone
    And the driver is heading to the pickup
    When the driver opens navigation
    Then "Apple Maps" is listed in the "Navigate with" sheet

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @waiting @blocked
  Scenario: Rider no-show after waiting
    Given the driver has arrived at pickup
    When the wait time runs out
    And the driver reports a no-show
    Then the trip is cancelled
    And the driver returns to the home screen

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @cancel @blocked
  Scenario Outline: Driver cancels the trip with a reason
    Given the driver is heading to the pickup
    When the driver taps "Cancel ride"
    Then the dialog "Cancel ride?" is shown
    When the driver selects reason "<reason>"
    And the driver confirms the cancellation
    Then the trip status is "Canceled by you"

    Examples:
      | reason                 |
      | Rider kept me waiting  |
      | Overly intoxicated     |
      | Other                  |

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @blocked
  Scenario: Rider cancels the trip
    Given the driver is heading to the pickup
    When the rider cancels the trip
    Then the trip status is "Canceled by rider"
    And the driver returns to the home screen

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @stops @blocked
  Scenario: Trip with an extra stop
    Given the rider added a stop to the trip
    When the driver starts the trip
    Then the text "Heading to Stop" is shown
    When the driver completes the stop
    Then the text "Heading to Destination" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @blocked
  Scenario: Rider changes the destination during the trip
    Given the trip is in progress
    When the rider changes the destination
    Then the new destination is shown
    And the route is updated

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @chat @blocked
  Scenario: Driver chats with the rider
    Given the driver is heading to the pickup
    When the driver opens the chat
    And the driver sends the message "I'm here"
    Then the message "I'm here" appears in the chat

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @chat @blocked
  Scenario: Driver receives a rider message notification
    Given the driver is heading to the pickup
    When the rider sends a message
    Then the notification "Message from your rider" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @rating @smoke @blocked
  Scenario: Driver rates the rider with a compliment
    Given the driver has dropped off the rider
    When the driver gives 5 stars
    And the driver selects the compliment "Polite rider"
    And the driver submits the rating
    Then the driver returns to the home screen
    And the text "You're online" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @rating @blocked
  Scenario: Driver rates the rider with a complaint
    Given the driver has dropped off the rider
    When the driver gives 2 stars
    And the driver selects the complaint "Disrespectful"
    And the driver submits the rating
    Then the driver returns to the home screen

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @rating @blocked
  Scenario: Driver skips rating
    Given the driver has dropped off the rider
    When the driver taps "Skip"
    Then the driver returns to the home screen

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @rating @negative @blocked
  Scenario: Rating submission fails
    Given the submit rating request returns an error
    And the driver has dropped off the rider
    When the driver submits the rating
    Then the text "Something went wrong while submiting rating!" is shown

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @negative @blocked
  Scenario Outline: Trip actions fail
    Given the "<request>" request returns an error
    When the driver performs "<action>"
    Then the text "<error>" is shown

    Examples:
      | request      | action          | error                                          |
      | startTrip    | start the trip  | Something went wrong while starting trip!      |
      | completeTrip | drop off        | Something went wrong while completing the trip! |

  # BLOCKED: needs a ride request from a rider (creating one is a real ride on the shared dev backend, held until ride-confirming scenarios are approved) and a driver acting on it.
  @trip-status @blocked
  Scenario Outline: Trip status labels
    Given the trip status is "<status>"
    When the driver opens the trip information screen
    Then the text "<label>" is shown

    Examples:
      | status      | label              |
      | accepted    | Accepted           |
      | arriving    | En route to pickup |
      | arrived     | Arrived at pickup  |
      | in_progress | Trip in progress   |
      | completed   | Completed          |
