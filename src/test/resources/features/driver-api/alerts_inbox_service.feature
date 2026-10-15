@api @alerts @inbox
Feature: API - alerts and inbox
  driver.v1.alerts.AlertsService (alerts_service_bloc) and
  notification.v1.inbox.InboxService (notification_feature_bloc).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session

  @alerts @smoke
  Scenario: ListAlerts returns the driver's alerts
    When I call "driver.v1.alerts.AlertsService/ListAlerts" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
      | page_size      | 20               |
    Then the response code is "ok"
    And each alert has a status and severity

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @alerts @blocked
  Scenario: ListAlerts with unread_only
    Given the driver has 2 unread and 3 read alerts
    When I call "driver.v1.alerts.AlertsService/ListAlerts" with unread_only true
    Then 2 alerts are returned

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @alerts @blocked
  Scenario: GetAlert, MarkAlertRead and DismissAlert
    Given an unread alert "<alert id>"
    When I call "driver.v1.alerts.AlertsService/GetAlert" with alert_id "<alert id>"
    Then the response code is "ok"
    When I call "driver.v1.alerts.AlertsService/MarkAlertRead" with alert_id "<alert id>"
    Then the alert is read
    When I call "driver.v1.alerts.AlertsService/DismissAlert" with:
      | field    | value       |
      | alert_id | <alert id>  |
      | reason   | Resolved    |
    Then the response code is "ok"
    And "ListAlerts" no longer returns "<alert id>"

  @alerts @negative
  Scenario: GetAlert with an unknown id
    When I call "driver.v1.alerts.AlertsService/GetAlert" with alert_id "unknown"
    Then the response code is "not_found"

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @alerts @negative @blocked
  Scenario: Driver cannot read another driver's alert
    Given an alert "<other alert id>" that belongs to another driver
    When I call "driver.v1.alerts.AlertsService/GetAlert" with alert_id "<other alert id>"
    Then the response code is "permission_denied" or "not_found"

  @inbox
  Scenario: ListNotifications returns notifications
    When I call "notification.v1.inbox.InboxService/ListNotifications" with:
      | field       | value |
      | page        | 1     |
      | unread_only | false |
    Then the response code is "ok"

  @inbox
  Scenario: ListBroadcasts returns broadcasts
    When I call "notification.v1.inbox.InboxService/ListBroadcasts" with:
      | field | value |
      | page  | 1     |
    Then the response code is "ok"

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @inbox @blocked
  Scenario: Empty inbox
    Given the driver has no notifications
    When I call "notification.v1.inbox.InboxService/ListNotifications" with page 1
    Then the response code is "ok"
    And the list is empty
