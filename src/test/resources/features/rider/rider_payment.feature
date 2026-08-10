@rider @payment
Feature: Rider Payment Management
  As a registered rider
  I want to manage my payment methods
  So that I can pay for rides securely

  # Derived from: hovr-rider-app/lib/features/payment/
  # Key blocs: PaymentBloc (events: ListPaymentMethod, NewPaymentMethodAdd, UpdateRiderDefault, Delete, AdyenDropIn)
  # States: ListPaymentMethodState (list, userWallet, defaultPaymentMethodId), AdyenDropInState (sessionId, sessionData)
  # Payment provider: Adyen (drop-in component)
  # Services: IAM service payment handlers

  Background:
    Given I am a registered rider
    And I am logged into the rider app

  # --- LIST PAYMENT METHODS ---

  Scenario: View payment methods with existing cards
    Given I have saved payment methods on my account
    When I navigate to the payment methods screen
    Then I should see a list of my saved payment methods
    And each card should display the brand and last 4 digits
    And the default payment method should be marked

  Scenario: View payment methods with no saved cards
    Given I have no saved payment methods
    When I navigate to the payment methods screen
    Then I should see an empty state
    And I should see an "Add Payment Method" button

  Scenario: View wallet balance
    Given I have a wallet with credits
    When I navigate to the payment methods screen
    Then I should see my wallet balance displayed

  # --- ADD PAYMENT METHOD ---

  Scenario: Add a new credit card via Adyen Drop-In
    Given I am on the payment methods screen
    When I tap "Add Payment Method"
    Then the Adyen Drop-In session should be initialized
    And I should see the Adyen payment form
    When I enter valid card details:
      | field       | value            |
      | Card Number | 4111111111111111 |
      | Expiry      | 12/28            |
      | CVV         | 123              |
      | Name        | Jane Doe         |
    And I tap "Save Card"
    Then the payment method should be saved successfully
    And I should see the new card in my payment methods list

  Scenario: Add payment method with invalid card number
    Given I am on the Adyen payment form
    When I enter an invalid card number "1234567890"
    And I tap "Save Card"
    Then I should see a validation error from Adyen
    And the card should not be saved

  Scenario: Add payment method with expired card
    Given I am on the Adyen payment form
    When I enter a card with expiry date "01/20"
    And I tap "Save Card"
    Then I should see an error "Card has expired"

  # --- DEFAULT PAYMENT METHOD ---

  Scenario: Change default payment method
    Given I have multiple saved payment methods
    And "Visa ...4242" is my default payment method
    When I tap on "Mastercard ...5555"
    And I select "Set as Default"
    Then "Mastercard ...5555" should become my default payment method
    And the UpdateRiderDefaultPaymentMethod event should be dispatched

  # --- DELETE PAYMENT METHOD ---

  Scenario: Delete a non-default payment method
    Given I have multiple saved payment methods
    When I tap on "Mastercard ...5555"
    And I select "Remove Card"
    And I confirm the deletion
    Then the card should be removed from my payment methods
    And I should see a confirmation message

  Scenario: Attempt to delete the only payment method
    Given I have only one saved payment method
    When I try to delete it
    Then I should either see a warning that at least one payment method is required, or the deletion should proceed with a warning about future ride bookings

  # --- PAYMENT METHOD DETAIL ---

  Scenario: View payment method details
    Given I have saved payment methods
    When I tap on a specific card "Visa ...4242"
    Then I should see the payment method detail screen
    And I should see the card brand, last 4 digits
    And I should see options to "Set as Default" and "Remove"

  # --- EDGE CASES ---

  Scenario: Adyen session initialization fails
    Given I am on the payment methods screen
    When I tap "Add Payment Method"
    And the Adyen session fails to initialize
    Then I should see an error message
    And I should be able to retry

  Scenario: Network error during payment method save
    Given I am entering card details in the Adyen form
    And there is no network connectivity
    When I tap "Save Card"
    Then I should see a network error
    And my card details should not be stored
    And I should be able to retry when connectivity returns

  Scenario: Payment processing during ride
    Given I have booked a ride and it is completed
    When the fare is charged to my default payment method
    Then the payment should be processed via Adyen
    And I should see the charge amount in my trip receipt
