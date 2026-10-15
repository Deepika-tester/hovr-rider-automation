@api @notifications
Feature: Notification, Inbox, Activity and Masked Calling APIs
  Clients: NotificationSettingsServiceClient, InboxServiceClient,
  ActivityServiceClient, MaskedCallingServiceClient

  Background:
    Given I am authenticated as a rider

  Scenario: Register device token
    When I call "NotificationSettingsService/RegisterDeviceToken" with a valid FCM token and platform
    Then the response status is "OK"

  Scenario: Unregister device token on logout
    Given my device token is registered
    When I call "NotificationSettingsService/UnregisterDeviceToken"
    Then the response status is "OK"

  Scenario: Get notification preferences
    When I call "NotificationSettingsService/GetNotificationPrefs"
    Then the response contains receipts, promotions and partner offer settings

  Scenario: List notifications
    When I call "InboxService/ListNotifications"
    Then the response status is "OK"
    And notifications are ordered newest first

  # BLOCKED: needs an existing notification, which only a trip or a back-office broadcast produces.
  @blocked
  Scenario: Get a notification
    Given I have a notification
    When I call "InboxService/GetNotification" with its id
    Then the response contains its title and body

  Scenario: List broadcasts
    When I call "InboxService/ListBroadcasts"
    Then the response status is "OK"

  Scenario: List activity
    When I call "ActivityService/ListActivity"
    Then the response status is "OK"

  # BLOCKED: needs an active trip with an assigned driver.
  @blocked
  Scenario: Set up masked call mapping
    Given I have an active trip with an assigned driver
    When I call "MaskedCallingService/SetupMapping"
    Then the response contains a proxy phone number

  Scenario: Make masked call without active trip
    Given I have no active trip
    When I call "MaskedCallingService/MakeMaskedCall"
    Then the response status is "FAILED_PRECONDITION"
