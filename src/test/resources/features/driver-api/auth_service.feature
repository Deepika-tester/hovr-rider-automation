@api @auth
Feature: API - iam.auth.v1.AuthService
  Login, token refresh, profile and app version endpoints.
  Called from core/network/otp_login_api.dart, core/network/client_manager.dart,
  core/session/driver_session_repository.dart and app_version_sync_coordinator.

  Background:
    Given the API base URL for the "dev" environment
    And the "Content-Type" header is "application/proto"

  @smoke
  Scenario: CreateVerificationChallenge sends an OTP
    When I call "iam.auth.v1.AuthService/CreateVerificationChallenge" with:
      | field        | value        |
      | phone_number | +14165550100 |
    Then the response code is "ok"
    And an OTP is sent to "+14165550100"

  @negative
  Scenario: CreateVerificationChallenge rejects an invalid phone number
    When I call "iam.auth.v1.AuthService/CreateVerificationChallenge" with:
      | field        | value |
      | phone_number | 123   |
    Then the response code is "invalid_argument"

  @smoke
  Scenario: VerifyVerificationChallenge returns tokens for a valid OTP
    Given a verification challenge was created for "+14165550100"
    When I call "iam.auth.v1.AuthService/VerifyVerificationChallenge" with:
      | field        | value                  |
      | phone_number | +14165550100           |
      | otp_code     | <valid otp>            |
      | user_type    | USER_TYPE_DRIVER       |
      | device_token | <fcm token>            |
      | lat_lng      | 43.6532,-79.3832       |
    Then the response code is "ok"
    And the response contains an access token
    And the response contains a refresh token

  @negative
  Scenario: VerifyVerificationChallenge rejects a wrong OTP
    Given a verification challenge was created for "+14165550100"
    When I call "iam.auth.v1.AuthService/VerifyVerificationChallenge" with:
      | field        | value            |
      | phone_number | +14165550100     |
      | otp_code     | 000000           |
      | user_type    | USER_TYPE_DRIVER |
    Then the response code is "unauthenticated" or "invalid_argument"

  @token
  Scenario: RefreshAccessToken issues a new access token
    Given a valid refresh token
    When I call "iam.auth.v1.AuthService/RefreshAccessToken" with:
      | field         | value           |
      | refresh_token | <refresh token> |
    Then the response code is "ok"
    And the response contains a new access token

  @token @negative
  Scenario: RefreshAccessToken rejects an expired refresh token
    When I call "iam.auth.v1.AuthService/RefreshAccessToken" with:
      | field         | value                   |
      | refresh_token | <expired refresh token> |
    Then the response code is "unauthenticated"

  # BLOCKED: describes the app's own retry behaviour, not a backend call; it cannot be observed through the API.
  @token @blocked
  Scenario: App retries a call after refreshing the token
    Given the access token has expired
    And a valid refresh token
    When the app calls any authenticated endpoint
    Then the first response code is "unauthenticated"
    And "iam.auth.v1.AuthService/RefreshAccessToken" is called once
    And the original call is retried with the new access token

  Scenario: GetActiveProfile returns the driver profile
    Given an authenticated driver session
    When I call "iam.auth.v1.AuthService/GetActiveProfile" with:
      | field   | value            |
      | lat_lng | 43.6532,-79.3832 |
    Then the response code is "ok"
    And the response contains the driver profile

  Scenario: GetAppVersion returns the minimum supported version
    When I call "iam.auth.v1.AuthService/GetAppVersion" with:
      | field    | value       |
      | app_name | hovr_driver |
      | platform | android     |
    Then the response code is "ok"
    And the response contains the latest and minimum app version

  Scenario: GetPaymentUrl returns a membership checkout URL
    Given an authenticated driver session
    When I call "iam.auth.v1.AuthService/GetPaymentUrl" with:
      | field       | value                  |
      | id          | <membership plan id>   |
      | success_url | <app success deeplink> |
    Then the response code is "ok"
    And the response contains an https payment URL

  @security
  Scenario Outline: Authenticated endpoints reject calls without a token
    Given no Authorization header
    When I call "<endpoint>" with an empty request
    Then the response code is "unauthenticated"

    Examples:
      | endpoint                                          |
      | iam.auth.v1.AuthService/GetActiveProfile           |
      | iam.driver.v1.DriverProfileService/GetDriverProfile |
      | ride.v1.driver.DriverService/GetDriverStatus       |
      | ride.v1.trip.TripService/GetActiveTrip             |
      | iam.wallet.v1.WalletService/GetWallet              |
      | neighbour.v1.driver.NeighbourDriverService/ListMyNeighbourTrips |
