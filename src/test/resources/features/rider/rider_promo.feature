@rider @promo
Feature: Rider Promotions and Discounts
  As a rider
  I want to apply promotional codes and earn discounts
  So that I can save money on rides

  # Derived from: HOVR marketing campaigns (GoodLife $10 off, FIFA26, etc.)
  # Promo application happens during ride booking via wallet credits
  # Wallet visible in: PaymentState -> ListPaymentMethodState (userWallet field)

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I have a valid payment method

  # --- APPLY PROMO CODE ---

  Scenario: Apply a valid promo code
    Given I am in the ride booking flow
    When I tap "Apply Promo Code"
    And I enter promo code "GOODLIFE10"
    And I tap "Apply"
    Then the promo should be validated
    And I should see "$10.00 discount applied"
    And the estimated fare should reflect the discount

  Scenario: Apply an expired promo code
    Given I am in the ride booking flow
    When I enter promo code "SUMMER2025"
    And I tap "Apply"
    Then I should see an error "This promo code has expired"

  Scenario: Apply an invalid promo code
    Given I am in the ride booking flow
    When I enter promo code "NOTREAL"
    And I tap "Apply"
    Then I should see an error "Invalid promo code"

  Scenario: Apply a promo code that has been fully redeemed
    Given I have already used promo code "GOODLIFE10"
    When I try to apply "GOODLIFE10" again
    Then I should see an error "This promo code has already been used"

  # --- WALLET CREDITS ---

  Scenario: Ride paid partially with wallet credits
    Given I have $10.00 in wallet credits
    And my ride fare is $25.50
    When I confirm the ride
    Then $10.00 should be deducted from my wallet
    And $15.50 should be charged to my payment method

  Scenario: Ride fully covered by wallet credits
    Given I have $50.00 in wallet credits
    And my ride fare is $25.50
    When I confirm the ride
    Then $25.50 should be deducted from my wallet
    And nothing should be charged to my payment method

  Scenario: View wallet balance
    Given I have wallet credits from promotions
    When I navigate to the payment methods screen
    Then I should see my current wallet balance
    And the balance should reflect all applied credits

  # --- CAMPAIGN-SPECIFIC SCENARIOS ---

  Scenario: GoodLife fitness partnership discount
    Given the GoodLife partnership promotion is active
    When I apply promo code "GOODLIFE10"
    Then I should receive $10 credit in my wallet
    And the credit should be applicable to my next ride

  Scenario: FIFA 2026 event promotion
    Given the FIFA 2026 promotion is active
    When I apply promo code "FIFA26"
    Then I should receive the promotional discount
    And the discount should apply only to rides within the event zone

  # --- EDGE CASES ---

  Scenario: Apply promo code with trailing spaces
    Given I am in the ride booking flow
    When I enter promo code "  GOODLIFE10  "
    And I tap "Apply"
    Then the code should be trimmed and validated
    And the promo should be applied successfully

  Scenario: Apply promo code when wallet is at maximum
    Given my wallet has reached the maximum credit limit
    When I try to apply another promo code
    Then I should see a message about the wallet limit
