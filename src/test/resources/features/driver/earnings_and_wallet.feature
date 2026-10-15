@earnings @wallet
Feature: Earnings and wallet payouts
  As a driver
  I want to see my earnings and withdraw my balance
  So that I get paid for my trips

  Background:
    Given the driver is logged in

  @earnings @smoke
  Scenario: Driver views earnings
    Given the driver completed trips this week
    When the driver opens "Earnings" from the drawer
    Then "Sessions", "Trips" and "Distance Travelled" are shown

  @earnings
  Scenario: Driver with no sessions
    Given the driver has no driving sessions
    When the driver opens "Earnings" from the drawer
    Then the text "No session Found" is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @earnings @blocked
  Scenario: Driver opens a trip from earnings
    Given the driver completed trips this week
    When the driver opens "Earnings" from the drawer
    And the driver opens a completed trip
    Then the text "Trip Details" is shown
    And the pickup and dropoff are shown
    And "You earn" is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @wallet @smoke @blocked
  Scenario: Driver withdraws the available balance
    Given the driver has an available balance of "$120.00"
    And the driver has an active bank account
    When the driver opens the wallet
    And the driver taps "Withdraw to bank"
    Then the "Minimum payout" and "Maximum payout" are shown
    When the driver taps "Confirm withdrawal"
    Then the payout appears in "Payout history" with status "Requested"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @wallet @blocked
  Scenario: Driver views payout details
    Given the driver has a payout with status "Paid out"
    When the driver opens the wallet
    And the driver opens the payout
    Then the text "Payout details" is shown
    And "Payout ID", "Amount" and "Status" are shown

  @wallet
  Scenario: No payouts yet
    Given the driver has no payouts
    When the driver opens the wallet
    Then the text "No payouts yet" is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @wallet @blocked
  Scenario: Payout history paging
    Given the driver has more than one page of payouts
    When the driver opens the wallet
    And the driver taps "Load more"
    Then more payouts are listed

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @wallet @negative @blocked
  Scenario Outline: Withdrawal is blocked
    Given <precondition>
    When the driver opens the wallet
    And the driver taps "Withdraw to bank"
    Then the text "<message>" is shown

    Examples:
      | precondition                                 | message                                         |
      | the driver has no banking details            | Add or finish your banking details to withdraw. |
      | a payout is already in progress              | A payout is already in progress.                |
      | payouts are disabled for the driver account  | Payouts are not available for this account.     |
