@notifications
Feature: Notifications and inbox
  As a rider
  I want to receive and read trip and promo messages

  Background:
    Given I am signed in as a rider

  Scenario: Empty inbox
    Given I have no messages
    When I open "Inbox"
    Then I should see "You’re all caught up."

  # BLOCKED: needs a notification, which is only produced by an active trip (driver required) or a broadcast from the back office.
  @blocked
  Scenario: Open a notification
    Given I have a trip notification
    When I open "Inbox"
    And I tap the notification
    Then I should see the notification details

  # BLOCKED: needs a notification, which is only produced by an active trip (driver required) or a broadcast from the back office.
  @blocked
  Scenario: Push notification for driver arrival
    Given I have an active trip
    And the app is in the background
    When the driver arrives
    Then I receive a push notification
    When I tap the notification
    Then the app opens on the active trip screen

  # BLOCKED: needs a notification, which is only produced by an active trip (driver required) or a broadcast from the back office.
  @blocked
  Scenario: Arrival notification
    Given my trip reaches the destination
    Then I should see "You have arrived at your destination."
