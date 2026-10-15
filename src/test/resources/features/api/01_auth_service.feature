@api @iam
Feature: AuthService (IAM) API
  Rider sign-in uses OTP verification over gRPC/ConnectRPC.
  Client: AuthServiceClient (lib/core/network/otp_login_api.dart, bare_iam_client.dart)

  Background:
    Given the API base URL is configured for the "staging" environment
    And I use a gRPC/ConnectRPC client with protobuf encoding

  @smoke
  Scenario: Create OTP challenge for a valid phone
    When I call "AuthService/CreateVerificationChallenge" with:
      | phone    | +14165550123 |
      | userType | RIDER        |
    Then the response status is "OK"
    And the response contains a non-empty challenge id

  Scenario Outline: Create OTP challenge with invalid phone
    When I call "AuthService/CreateVerificationChallenge" with phone "<phone>"
    Then the response status is "INVALID_ARGUMENT"

    Examples:
      | phone      |
      | 12345      |
      |            |
      | +1abc55501 |

  # BLOCKED: needs the daily OTP request limit exhausted (dozens of real SMS requests). The dev backend does enforce a per-number resend cooldown (HTTP 429 resource_exhausted, 'please wait N seconds'); that is noted in the report but is not the daily limit this scenario describes.
  @blocked
  Scenario: OTP rate limit
    Given I have requested OTP the maximum number of times today for "+14165550123"
    When I call "AuthService/CreateVerificationChallenge" with phone "+14165550123"
    Then the response status is "RESOURCE_EXHAUSTED"

  @smoke
  Scenario: Verify OTP with correct code
    Given an OTP challenge exists for "+14165550123"
    When I call "AuthService/VerifyVerificationChallenge" with:
      | phone       | +14165550123   |
      | otpCode     | <valid code>   |
      | deviceToken | <fcm token>    |
      | userType    | RIDER          |
    Then the response status is "OK"
    And the response contains an access token and a refresh token

  Scenario: Verify OTP with wrong code
    Given an OTP challenge exists for "+14165550123"
    When I call "AuthService/VerifyVerificationChallenge" with otpCode "000000"
    Then the response status is "INVALID_ARGUMENT" or "UNAUTHENTICATED"
    And no tokens are returned

  Scenario: Refresh access token
    Given I hold a valid refresh token
    When I call "AuthService/RefreshAccessToken"
    Then the response status is "OK"
    And a new access token is returned

  Scenario: Refresh with expired or revoked token
    Given I hold a revoked refresh token
    When I call "AuthService/RefreshAccessToken"
    Then the response status is "UNAUTHENTICATED"

  Scenario: Get active profile
    Given I am authenticated as a rider
    When I call "AuthService/GetActiveProfile"
    Then the response status is "OK"
    And the response contains my rider id

  # BLOCKED: GetAppVersion answers 'App ID is Required' to every request shape tried (appId, packageName, bundleId, ... as body fields or x-app-id style headers); the field is not discoverable without the proto, and the spec does not give it.
  @blocked
  Scenario: Get app version
    When I call "AuthService/GetAppVersion" with the current platform
    Then the response contains the latest and minimum supported version

  Scenario Outline: Protected RPC without valid token
    Given my access token is "<token>"
    When I call "AuthService/GetActiveProfile"
    Then the response status is "UNAUTHENTICATED"

    Examples:
      | token      |
      | missing    |
      | expired    |
      | malformed  |
