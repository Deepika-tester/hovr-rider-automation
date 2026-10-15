@wallet
Feature: Wallet and payouts
  As a rider
  I want to see my HOVR balance and withdraw it

  Background:
    Given I am signed in as a rider
    And I open "Wallet"

  Scenario: Empty balance
    Given my balance is zero
    Then I should see "No balance yet"

  # BLOCKED: needs a wallet balance, banking details or payouts, which a new rider cannot have and the dev tools cannot grant.
  @blocked
  Scenario: View balance breakdown
    Given I have a balance
    Then I should see "Available", "Pending" and "Locked"

  # BLOCKED: needs a wallet balance, banking details or payouts, which a new rider cannot have and the dev tools cannot grant.
  @blocked
  Scenario: Withdraw available balance
    Given my available balance is above the minimum payout
    And I have banking details set up
    When I tap "Withdraw available balance"
    And I tap "Confirm withdrawal"
    Then I should see "Withdrawal requested"
    And the payout appears in "Payout history" with status "Pending"

  # BLOCKED: needs a wallet balance, banking details or payouts, which a new rider cannot have and the dev tools cannot grant.
  @blocked
  Scenario Outline: Withdrawal blocked
    Given <condition>
    When I tap "Withdraw available balance"
    Then I should see "<message>"

    Examples:
      | condition                               | message                                              |
      | my balance is below the minimum payout  | Your available balance is below the minimum payout.  |
      | I have no banking details               | Add or finish your banking details to withdraw.      |
      | a payout is already in progress         | A payout is already in progress.                     |

  # BLOCKED: needs a wallet balance, banking details or payouts, which a new rider cannot have and the dev tools cannot grant.
  @blocked
  Scenario: View payout details
    Given I have a past payout
    When I open it from "Payout history"
    Then I should see "Amount", "Status" and "Payout ID"
