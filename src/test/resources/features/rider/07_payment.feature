@payment
Feature: Payment methods
  As a rider
  I want to manage payment methods

  Background:
    Given I am signed in as a rider
    And I open "Payment Methods"

  @fresh-install
  Scenario: No payment methods
    Given I have no saved cards
    Then I should see "Add a payment method"

  @smoke
  Scenario: Add a credit card
    When I tap "Add payment method"
    And I select "Credit or debit card"
    And I enter valid card details
    And I tap "Add card"
    Then I should see "Payment method added successfully!"

  Scenario: Add an invalid card
    When I add a card with invalid details
    Then I should see "Payment failed. Please double-check your card details and try again."

  Scenario: Set primary card
    Given I have two saved cards
    When I open the second card
    And I tap "Set as primary"
    Then the second card is marked "Primary"

  Scenario: Remove a card
    Given I have a saved card
    When I open the card
    And I tap "Remove payment method"
    Then the card is removed from the list

  # BLOCKED: needs the server to reject a card deletion (fault injection); not possible against the shared dev backend.
  @blocked
  Scenario: Remove card fails
    Given the server rejects card deletion
    When I remove a card
    Then I should see "Failed to delete card. Please try again."

  Scenario Outline: Wallet payment options by platform
    Given I am on a "<platform>" device
    When I tap "Add payment method"
    Then I should see "<wallet>"

    Examples:
      | platform | wallet     |
      | iOS      | Apple Pay  |
      | Android  | Google Pay |

  # BLOCKED: needs the payment service to be down (fault injection); not possible against the shared dev backend.
  @blocked
  Scenario: Load payment methods failure
    Given the payment service is unavailable
    Then I should see "Couldn't finish loading"
    When I tap "Retry"
    Then payment methods are reloaded
