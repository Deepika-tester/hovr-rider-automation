@alerts @notifications
Feature: Alerts, notifications and inbox
  As a driver
  I want to see alerts and messages from HOVR
  So that I can act on account issues

  Background:
    Given the driver is logged in

  @alerts @smoke
  Scenario: Driver views alerts
    Given the driver has alerts
    When the driver opens "Account"
    And the driver taps "Alerts"
    Then the text "Alerts" is shown
    And each alert shows "Status" and "Severity"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @alerts @blocked
  Scenario: No alerts
    Given the driver has no alerts
    When the driver opens the alerts list
    Then the text "No alerts found!" is shown

  # BLOCKED: would change or consume data on the shared approved test driver in a way that cannot be undone from here; not run without approval.
  @alerts @blocked
  Scenario: Driver marks an alert as read
    Given the driver has an unread alert
    When the driver opens the alert
    And the driver taps "Mark as read"
    Then the alert is marked as read

  # BLOCKED: would change or consume data on the shared approved test driver in a way that cannot be undone from here; not run without approval.
  @alerts @blocked
  Scenario: Driver dismisses an alert
    Given the driver has an alert
    When the driver opens the alert
    And the driver taps "Dismiss"
    Then the alert is removed from the list

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @alerts @blocked
  Scenario Outline: Alert action opens the right screen
    Given the driver has an alert of type "<type>"
    When the driver opens the alert
    And the driver taps "<action>"
    Then the "<screen>" screen is shown

    Examples:
      | type         | action             | screen          |
      | banking      | Go to banking      | Banking Details |
      | document     | Go to Document     | Documents       |
      | subscription | Go to Subscription | Membership      |

  @alerts
  Scenario: Alerts are shown on the home screen
    Given the driver has an active alert
    When the driver opens the home screen
    Then the alert banner is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @alerts @negative @blocked
  Scenario: Alerts fail to load
    Given the list alerts request returns an error
    When the driver opens the alerts list
    Then the text "Something went wrong while getting alerts!" is shown

  @inbox
  Scenario: Empty inbox
    Given the driver has no messages
    When the driver opens "Inbox"
    Then the text "You’re all caught up." is shown
    And the text "No new messages." is shown

  @inbox
  Scenario: Driver opens a notification
    Given the driver has a notification
    When the driver opens "Inbox"
    And the driver opens the notification
    Then the notification detail is shown

  # BLOCKED: needs a push notification from the back office.
  @push @blocked
  Scenario: Tapping a push notification opens the app
    Given the app is in the background
    When the driver receives a push notification
    And the driver taps the push notification
    Then the app opens on the related screen
