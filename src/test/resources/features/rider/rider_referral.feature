@rider @referral
Feature: Rider Refer and Earn
  As a registered rider
  I want to refer friends to HOVR and earn rewards
  So that I can save on future rides

  # Derived from: hovr-rider-app/lib/features/refer-and-earn/
  # Key blocs: ReferralBloc (events: GetReferralCode, RedeemReferralCode, GetReferList)
  # States: ReferralCodeSuccessState (GetReferralProfileResponse), ReferralCodeRedeemSuccessState, ReferralListSuccessState
  # Pages: refer_and_earn.dart, redeem_referral_code_view.dart, referral_progress.dart, qr_scanner.dart

  Background:
    Given I am a registered rider
    And I am logged into the rider app

  # --- VIEW REFERRAL CODE ---

  Scenario: View my referral code
    Given I navigate to the "Refer and Earn" screen
    When the referral profile is loaded
    Then I should see my unique referral code
    And I should see the referral reward details
    And I should see a "Share" button

  Scenario: Share referral code
    Given I am on the "Refer and Earn" screen
    And I see my referral code "HOVR-JANE2026"
    When I tap "Share"
    Then the system share sheet should open
    And the shared content should include my referral code
    And it should include a download link for the HOVR app

  # --- REDEEM REFERRAL CODE ---

  Scenario: Redeem a valid referral code
    Given I am on the "Redeem Referral Code" screen
    When I enter referral code "HOVR-JOHN2026"
    And I enter the referrer's name "John"
    And I tap "Redeem"
    Then the referral should be redeemed successfully
    And I should see a success message with the reward details

  Scenario: Redeem own referral code
    Given I am on the "Redeem Referral Code" screen
    When I enter my own referral code
    And I tap "Redeem"
    Then I should see an error "You cannot use your own referral code"

  Scenario: Redeem an invalid referral code
    Given I am on the "Redeem Referral Code" screen
    When I enter an invalid referral code "FAKE123"
    And I tap "Redeem"
    Then I should see an error message from ReferralErrorState

  Scenario: Redeem code when already referred
    Given I have already redeemed a referral code
    When I try to redeem another referral code
    Then I should see an error "You have already used a referral code"

  # --- SCAN QR CODE ---

  Scenario: Scan a referral QR code
    Given I am on the "Refer and Earn" screen
    When I tap "Scan QR Code"
    Then the QR scanner should open
    When I scan a valid referral QR code
    Then the referral code should be auto-filled
    And I should see the referrer's details

  Scenario: Scan an invalid QR code
    Given the QR scanner is open
    When I scan a QR code that is not a HOVR referral
    Then I should see an error "Invalid QR code"

  # --- REFERRAL PROGRESS ---

  Scenario: View referral progress with referrals
    Given I have referred 3 friends
    When I navigate to "Referral Progress"
    Then I should see a list of my referrals
    And each referral should show the status (pending, completed, rewarded)
    And I should see total rewards earned

  Scenario: View referral progress with no referrals
    Given I have not referred anyone
    When I navigate to "Referral Progress"
    Then I should see the empty referral progress view
    And I should see a call to action to start referring

  # --- EDGE CASES ---

  Scenario: Referral code loading fails
    Given I navigate to the "Refer and Earn" screen
    When the referral service returns an error
    Then I should see the error state
    And I should be able to retry loading

  Scenario: Network error during redemption
    Given I am redeeming a referral code
    And there is no network connectivity
    When I tap "Redeem"
    Then I should see a network error message
    And the code should not be consumed
