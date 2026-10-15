@api @wallet @referral
Feature: API - wallet payouts and referrals
  iam.wallet.v1.WalletService (wallet_cubit) and iam.referral.v1.ReferralService
  (referral_bloc, redeem_referral_cubit).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session

  @wallet @smoke
  Scenario: GetWallet returns balances
    When I call "iam.wallet.v1.WalletService/GetWallet" with:
      | field   | value       |
      | user_id | <driver id> |
    Then the response code is "ok"
    And the response contains the available balance

  # BLOCKED: moves or reserves real money on the shared dev backend; not run without approval.
  @wallet @smoke @blocked
  Scenario: Quote then create a payout
    Given the driver has an available balance of 120.00
    And the driver has an active bank account
    When I call "iam.wallet.v1.WalletService/GetWalletPayoutQuote" with:
      | field | value             |
      | code  | <payout method>   |
    Then the response code is "ok"
    And the quote contains the minimum and maximum payout
    When I call "iam.wallet.v1.WalletService/CreateWalletPayout" with:
      | field      | value           |
      | request_id | <uuid>          |
      | amount     | 120.00          |
      | code       | <payout method> |
    Then the response code is "ok"
    And the payout status is "requested"

  # BLOCKED: moves or reserves real money on the shared dev backend; not run without approval.
  @wallet @negative @blocked
  Scenario Outline: CreateWalletPayout is refused
    Given <precondition>
    When I call "iam.wallet.v1.WalletService/CreateWalletPayout" with amount <amount>
    Then the response code is "<code>"

    Examples:
      | precondition                         | amount  | code                |
      | the driver has no bank account       | 50.00   | failed_precondition |
      | a payout is already in progress      | 50.00   | failed_precondition |
      | the available balance is 20.00       | 50.00   | failed_precondition |
      | the minimum payout is 10.00          | 1.00    | invalid_argument    |

  # BLOCKED: moves or reserves real money on the shared dev backend; not run without approval.
  @wallet @idempotency @blocked
  Scenario: CreateWalletPayout is idempotent on request_id
    Given I created a payout with request_id "<uuid>"
    When I call "CreateWalletPayout" again with the same request_id
    Then only one payout exists

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @wallet @blocked
  Scenario: ListWalletPayouts and GetWalletPayout
    When I call "iam.wallet.v1.WalletService/ListWalletPayouts" with:
      | field     | value |
      | page_size | 20    |
    Then the response code is "ok"
    When I call "iam.wallet.v1.WalletService/GetWalletPayout" with the first payout_id
    Then the response code is "ok"
    And the payout id, amount and status are returned

  @referral
  Scenario: GetReferralProfile returns the referral code
    When I call "iam.referral.v1.ReferralService/GetReferralProfile" with:
      | field   | value       |
      | user_id | <driver id> |
    Then the response code is "ok"
    And the response contains a referral code

  @referral
  Scenario: ListReferralReward pages through rewards
    When I call "iam.referral.v1.ReferralService/ListReferralReward" with no pagination_token
    Then the response code is "ok"

  # BLOCKED: would change the shared approved test driver's account in a way that cannot be undone from here (email, vehicle, onboarding, documents); not run without approval.
  @referral @blocked
  Scenario: RedeemReferral with a valid code
    When I call "iam.referral.v1.ReferralService/RedeemReferral" with:
      | field         | value   |
      | referral_code | HOVR123 |
    Then the response code is "ok"

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @referral @negative @blocked
  Scenario Outline: RedeemReferral is refused
    When I call "iam.referral.v1.ReferralService/RedeemReferral" with referral_code "<code>"
    Then the response code is "<status>"

    Examples:
      | code          | status              |
      |               | invalid_argument    |
      | BADCODE       | not_found           |
      | <own code>    | failed_precondition |
      | <already used>| already_exists      |
