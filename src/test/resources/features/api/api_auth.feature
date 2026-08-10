@api @auth @backend
Feature: API Authentication and Session Management
  As the HOVR platform
  I want to securely authenticate users via phone OTP
  So that only verified users can access the platform

  # Derived from: iam-service (Go) - auth_service, rider_service, driver_service
  # Auth flow: RequestOTP -> VerifyOTP -> Token issued
  # Profile services: CreateRiderProfile, CreateDriverProfile, GetDriverProfile, etc.
  # Payment: PaymentService (Adyen sessions, list/update/delete payment methods)
  # Referral: ReferralService (get profile, redeem, list rewards)
  # Wallet: WalletService
  # Infrastructure: DynamoDB, Redis, S3, protovalidate, notification router

  Background:
    Given the IAM service is running at iam.api.prod.ridehovr.com
    And the DynamoDB database is accessible
    And the Redis cache is accessible

  # --- OTP REQUEST ---

  Scenario: Request OTP for valid Canadian phone number
    Given a user with phone number "+14165551234"
    When a RequestOTP API call is made with that phone number
    Then the system should generate a 6-digit OTP
    And the OTP should be sent via SMS notification
    And the OTP should be stored with a 5-minute expiry

  Scenario: Request OTP for non-Canadian number
    Given a user with phone number "+442071234567"
    When a RequestOTP API call is made
    Then the system should validate the phone number format
    And process according to supported regions

  Scenario: Request OTP with invalid phone format
    Given a malformed phone number "12345"
    When a RequestOTP API call is made
    Then the system should return a validation error via protovalidate
    And no OTP should be generated

  Scenario: Rate limit OTP requests
    Given a phone number "+14165551234" has requested 5 OTPs in 10 minutes
    When another OTP request is made for the same number
    Then the system should rate-limit the request
    And return an appropriate error message

  # --- OTP VERIFICATION ---

  Scenario: Verify correct OTP
    Given an OTP "123456" was sent to "+14165551234"
    And the OTP has not expired
    When a VerifyOTP API call is made with phone "+14165551234" and OTP "123456"
    Then the system should return authentication tokens
    And the response should indicate if this is a new or returning user

  Scenario: Verify incorrect OTP
    Given an OTP "123456" was sent to "+14165551234"
    When a VerifyOTP API call is made with OTP "000000"
    Then the system should return an authentication error
    And no tokens should be issued

  Scenario: Verify expired OTP
    Given an OTP was sent more than 5 minutes ago
    When a VerifyOTP API call is made with the expired OTP
    Then the system should return an expiration error

  Scenario: Brute force OTP protection
    Given incorrect OTPs have been submitted 5 times for "+14165551234"
    When another incorrect OTP is submitted
    Then the account should be temporarily locked
    And a new OTP should be required after the lockout period

  # --- RIDER PROFILE ---

  Scenario: Create new rider profile
    Given a newly authenticated user (first-time)
    When a CreateRiderProfile request is made with:
      | field         | value                |
      | firstName     | Jane                 |
      | lastName      | Doe                  |
      | email         | jane@example.com     |
      | preferredName | Jane                 |
      | referralCode  |                      |
    Then a rider profile should be created in DynamoDB
    And the response should include the rider ID

  Scenario: Create rider profile with referral code
    Given a valid referral code "HOVR-JOHN2026" exists
    When a CreateRiderProfile request includes that referral code
    Then the rider profile should be created
    And the referral should be linked via the ReferralService
    And both referrer and referee should receive their rewards

  Scenario: Create duplicate rider profile
    Given a rider profile already exists for this phone number
    When a CreateRiderProfile request is made
    Then the system should return the existing profile
    And not create a duplicate

  # --- DRIVER PROFILE ---

  Scenario: Create new driver profile
    Given a newly authenticated driver
    When a CreateDriverProfile request is made
    Then a driver profile should be created
    And the profile status should be "PENDING_ONBOARDING"

  Scenario: Get driver profile by phone
    Given a driver profile exists for "+16475559876"
    When a GetDriverProfileByPhone request is made
    Then the driver profile should be returned with all fields

  Scenario: Activate driver profile
    Given a driver has completed all onboarding requirements
    When an ActiveDriverProfile request is made
    Then the driver status should change to "ACTIVE"
    And the driver should be eligible to go online

  Scenario: Deactivate driver profile
    Given an active driver profile
    When a DeactivateDriverProfile request is made with a reason
    Then the driver status should change to "DEACTIVATED"
    And the driver should not be able to go online

  Scenario: Update driver profile
    Given an existing driver profile
    When an UpdateDriverProfile request is made with updated fields
    Then the profile should be updated in DynamoDB

  # --- PAYMENT SERVICE ---

  Scenario: Get rider payment session (Adyen)
    Given a rider with ID "rider-123"
    When a GetRiderPaymentSession request is made
    Then an Adyen session should be created
    And the session ID and data should be returned

  Scenario: List rider payment methods
    Given a rider has 2 saved payment methods
    When a ListRiderPaymentMethods request is made
    Then both payment methods should be returned
    And the default payment method should be flagged

  Scenario: Update default payment method
    Given a rider with multiple payment methods
    When an UpdateRiderDefaultPaymentMethod request is made with a new card ID
    Then the default should be updated

  Scenario: Get driver onboarding payment URL
    Given a driver completing membership subscription
    When a GetDriverOnboarding request is made
    Then a payment URL should be returned for the membership subscription

  # --- REFERRAL SERVICE ---

  Scenario: Get referral profile
    Given a user with an active referral code
    When a GetReferralProfile request is made
    Then the referral code and reward details should be returned

  Scenario: Redeem referral code
    Given a valid unused referral code "HOVR-JOHN2026"
    When a RedeemReferral request is made by a new user
    Then the referral should be redeemed
    And rewards should be applied to both parties

  Scenario: List referral rewards
    Given a user who has referred 3 friends
    When a ListReferralReward request is made
    Then all 3 referral records should be returned with their status

  # --- WALLET SERVICE ---

  Scenario: Check wallet balance
    Given a rider with $15.00 in wallet credits
    When the wallet balance is queried
    Then the balance should return $15.00

  # --- MARKETPLACE VALIDATION ---

  Scenario: Validate user region against active marketplaces
    Given active marketplaces "GTA" and "Ottawa"
    When a user from a supported region authenticates
    Then they should be allowed to proceed

  Scenario: User from unsupported marketplace
    Given a user from a region with no active marketplace
    When they authenticate
    Then the system should return a "not available in your region" indicator

  # --- EDGE CASES ---

  Scenario: Concurrent authentication requests
    Given two devices authenticate with the same phone number simultaneously
    When both verify OTP
    Then only one session should be active
    And the other should be invalidated or handled gracefully

  Scenario: Token refresh
    Given a user's authentication token is about to expire
    When a token refresh request is made
    Then a new token should be issued
    And the old token should be invalidated

  Scenario: Unauthorized API access
    Given no authentication token is provided
    When any protected API endpoint is called
    Then the system should return a 401 Unauthorized response

  Scenario: Expired token API access
    Given an expired authentication token
    When a protected API endpoint is called
    Then the system should return a 401 or 403 response
    And the client should be prompted to re-authenticate
