@api @wallet @referral
Feature: WalletService and ReferralService API
  Clients: WalletServiceClient, ReferralServiceClient

  Background:
    Given I am authenticated as a rider with id "<riderId>"

  Scenario: Get wallet
    When I call "WalletService/GetWallet" with userId "<riderId>"
    Then the response status is "OK"
    And the response contains available, pending and locked balances

  Scenario: Get another user's wallet
    When I call "WalletService/GetWallet" with another user's id
    Then the response status is "PERMISSION_DENIED"

  @smoke
  Scenario: Get referral profile
    When I call "ReferralService/GetReferralProfile"
    Then the response contains my referral code and invite link

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @india-only-skip
  Scenario: Redeem a valid referral code
    Given another rider has referral code "FRIEND5"
    When I call "ReferralService/RedeemReferral" with code "FRIEND5"
    Then the response status is "OK"

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @india-only-skip
  Scenario Outline: Redeem invalid referral codes
    When I call "ReferralService/RedeemReferral" with code "<code>"
    Then the response status is "<status>"

    Examples:
      | code           | status              |
      |                | INVALID_ARGUMENT    |
      | NOPE000        | NOT_FOUND           |
      | <my own code>  | FAILED_PRECONDITION |

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @india-only-skip
  Scenario: Redeem twice
    Given I already redeemed a referral code
    When I call "ReferralService/RedeemReferral" again
    Then the response status is "ALREADY_EXISTS" or "FAILED_PRECONDITION"

  Scenario: List referral rewards
    When I call "ReferralService/ListReferralReward"
    Then each reward shows rides completed out of total and amount
