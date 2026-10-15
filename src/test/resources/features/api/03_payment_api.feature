@api @payment
Feature: Payment methods API
  Adyen session and card management.
  Data source: payment_remote_data_source.dart

  Background:
    Given I am authenticated as a rider with id "<riderId>"

  Scenario: Create Adyen payment session
    When I call "CreateAdyenPaymentSession" with riderId "<riderId>"
    Then the response status is "OK"
    And the response contains a session id and session data
    And the call completes within 30 seconds

  Scenario: List payment methods for new rider
    Given the rider has no saved cards
    When I call "ListPaymentMethods"
    Then the response contains an empty list

  # BLOCKED: needs two saved cards, which can only be created through the Adyen card form (UI), not by this API client.
  @smoke @blocked
  Scenario: Set default payment method
    Given the rider has two saved cards "A" and "B" with "A" default
    When I call "SetDefaultPaymentMethod" with paymentMethodId of "B"
    Then the response status is "OK"
    And ListPaymentMethods shows "B" as default and "A" not default

  Scenario: Set default with unknown card
    When I call "SetDefaultPaymentMethod" with paymentMethodId "unknown"
    Then the response status is "NOT_FOUND"

  # BLOCKED: needs a saved card, which can only be created through the Adyen card form (UI).
  @blocked
  Scenario: Delete payment method
    Given the rider has a saved card "A"
    When I call "DeletePaymentMethod" with paymentMethodId of "A"
    Then the response status is "OK"
    And ListPaymentMethods does not contain "A"

  # BLOCKED: needs a card owned by another rider (Adyen card form).
  @blocked
  Scenario: Delete another rider's card
    When I call "DeletePaymentMethod" with a card owned by another rider
    Then the response status is "PERMISSION_DENIED"
