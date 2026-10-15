@referral
Feature: Refer and earn
  As a driver
  I want to invite riders with my referral code
  So that I earn rewards

  Background:
    Given the driver is logged in
    And the driver is approved for rideshare
    And the driver opens "Refer a friend" from the drawer

  @smoke
  Scenario: Referral screen is shown
    Then the text "Refer and earn" is shown
    And the "Referral link" is shown
    And the "Share with Rider" button is shown
    And the "How it works" steps are listed:
      | step                                                 |
      | Share your link                                      |
      | Your friend signs up and takes their first 3 rides   |
      | Claim your reward!                                   |

  Scenario: Driver copies the referral link
    When the driver taps the referral link
    Then the snackbar "Copied to clipboard!" is shown

  Scenario: Driver shares the referral link
    When the driver taps "Share with Rider"
    Then the share sheet opens with "ridehovr.com/invite/"

  Scenario: Driver opens the referral terms
    When the driver taps "See terms and conditions"
    Then the referral terms are shown

  @redeem
  Scenario: Driver redeems a referral code
    When the driver taps "Enter a referral code"
    Then the text "Enter referral code" is shown
    When the driver enters the code "HOVR123"
    And the driver taps "Redeem"
    Then the text "Redeemed successfully!" is shown

  @redeem @negative
  Scenario: Empty referral code
    When the driver taps "Enter a referral code"
    And the driver taps "Redeem"
    Then the text "Enter the referral code!" is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @redeem @negative @blocked
  Scenario: Referral code cannot be redeemed
    Given the redeem request returns an error
    When the driver redeems the code "BADCODE"
    Then the text "Can't redeem at the moment!" is shown

  # BLOCKED: needs a camera showing a referral QR code.
  @redeem @blocked
  Scenario: Driver scans a referral QR code
    When the driver taps "Enter a referral code"
    And the driver scans a referral QR code
    Then the referral code is filled in

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @progress @blocked
  Scenario: Driver views referral progress
    Given the driver has referred riders
    When the driver taps "View progress"
    Then the text "Referral progress" is shown
    And each referral shows completed rides as "x/3 rides"

  @progress
  Scenario: No referrals yet
    Given the driver has not referred anyone
    When the driver taps "View progress"
    Then the text "No referrals yet" is shown
    And the text "Refer a friend to start earning" is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @wallet @blocked
  Scenario: Referral wallet balances
    Given the driver has referral rewards
    Then "Total", "Available", "Pending" and "Locked" are shown
