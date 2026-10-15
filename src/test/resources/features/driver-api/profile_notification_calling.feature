@api @profile
Feature: API - driver profile, device tokens and masked calling
  iam.driver.v1.DriverProfileService (driver_profile_bloc, auth_bloc),
  iam.notification.v1.NotificationSettingsService (driver_notification_registration, logout_coordinator),
  iam.twilio.v1.MaskedCallingService (auth_bloc).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session

  @smoke
  Scenario: GetDriverProfile returns the driver
    When I call "iam.driver.v1.DriverProfileService/GetDriverProfile" with:
      | field | value       |
      | id    | <driver id> |
    Then the response code is "ok"
    And the response contains first name, last name, phone and email

  Scenario: UpdateDriverProfile updates the preferred name
    When I call "iam.driver.v1.DriverProfileService/UpdateDriverProfile" with:
      | field          | value       |
      | id             | <driver id> |
      | preferred_name | AJ          |
    Then the response code is "ok"
    And "GetDriverProfile" returns preferred name "AJ"

  @negative
  Scenario: UpdateDriverProfile rejects another driver's id
    When I call "iam.driver.v1.DriverProfileService/UpdateDriverProfile" with:
      | field      | value              |
      | id         | <other driver id>  |
      | first_name | Mallory            |
    Then the response code is "permission_denied"

  # BLOCKED: would change the shared approved test driver's account in a way that cannot be undone from here (email, vehicle, onboarding, documents); not run without approval.
  @blocked
  Scenario: ChangeEmail then VerifyEmail
    When I call "iam.driver.v1.DriverProfileService/ChangeEmail" with email "alex.new@example.com"
    Then the response code is "ok"
    When I call "iam.driver.v1.DriverProfileService/VerifyEmail" with the code sent to "alex.new@example.com"
    Then the response code is "ok"
    And the driver email is "alex.new@example.com"

  @notification
  Scenario: RegisterDeviceToken after login
    When I call "iam.notification.v1.NotificationSettingsService/RegisterDeviceToken" with:
      | field       | value       |
      | fcm_token   | <fcm token> |
      | platform    | android     |
      | app_version | <version>   |
      | locale      | en_US       |
      | latitude    | 43.6532     |
      | longitude   | -79.3832    |
    Then the response code is "ok"

  @notification
  Scenario: UnregisterDeviceToken on logout
    Given the device token is registered
    When I call "iam.notification.v1.NotificationSettingsService/UnregisterDeviceToken" with:
      | field     | value       |
      | fcm_token | <fcm token> |
    Then the response code is "ok"
    And no push notifications are sent to that device

  # BLOCKED: needs a rider's ride request and a driver acting on it (a real ride on the shared dev backend, held until ride-confirming scenarios are approved).
  @calling @blocked
  Scenario: SetupMapping then MakeMaskedCall to the rider
    Given the driver has an active trip with a rider
    When I call "iam.twilio.v1.MaskedCallingService/SetupMapping" with:
      | field    | value          |
      | caller   | <driver phone> |
      | receiver | <rider phone>  |
    Then the response code is "ok"
    When I call "iam.twilio.v1.MaskedCallingService/MakeMaskedCall" with:
      | field  | value          |
      | caller | <driver phone> |
    Then the response code is "ok"
    And the rider's real number is not returned
