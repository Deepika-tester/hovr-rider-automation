@api @pricing @backend
Feature: API Fare Calculation and Pricing
  As the HOVR platform
  I want to calculate accurate fares for rides
  So that riders are charged fairly and drivers are compensated correctly

  # Derived from: hovr-rider-app fare display in choose_a_ride_bottom_sheet.dart,
  #               view_breakdown.dart (FareBreakdown type)
  # Fare components observed: Base fare, Distance, Time, Service fee
  # Ride types: HOVR, HOVR XL
  # HOVR model: No surge pricing (differentiator from Uber/Lyft)
  # Driver keeps 100% of fare ($20/month membership model)
  # Promo/wallet: Credits applied to reduce rider-side fare

  Background:
    Given the pricing service is operational
    And marketplace "GTA" rates are configured

  # --- FARE ESTIMATION ---

  Scenario: Calculate fare for a standard HOVR ride
    Given a ride from "Union Station" to "Pearson Airport" in the GTA marketplace
    When a FindRideRequest is processed
    Then the response should include a fare estimate for "HOVR"
    And the fare should include:
      | component     | description              |
      | Base fare     | Fixed starting amount    |
      | Distance      | Per-km rate * distance   |
      | Time          | Per-minute rate * time   |
      | Service fee   | Platform service charge  |
      | Total         | Sum of all components    |

  Scenario: Calculate fare for HOVR XL
    Given a ride from "Union Station" to "Pearson Airport"
    When a FindRideRequest is processed
    Then the response should include a fare estimate for "HOVR XL"
    And the HOVR XL fare should be higher than the standard HOVR fare

  Scenario: View fare breakdown
    Given a fare has been calculated
    When the FareBreakdown is requested
    Then each component should be itemized:
      | component     | amount  |
      | Base fare     | $3.50   |
      | Distance      | $15.20  |
      | Time          | $6.80   |
      | Service fee   | $2.00   |
      | Total         | $27.50  |

  # --- FARE CONSISTENCY ---

  Scenario: Fare does not change with surge (no surge model)
    Given high demand in the GTA at 5:00 PM Friday
    When a fare is calculated for the same route
    Then the fare should be the same as during low-demand hours
    And no surge multiplier should be applied

  Scenario: Fare consistency between estimate and final charge
    Given a fare estimate of $25.50 was shown before booking
    When the trip is completed along the estimated route
    Then the final fare should be approximately $25.50
    And any variation should be due to actual distance/time differences

  # --- PROMO APPLICATION ---

  Scenario: Apply promo discount to fare
    Given a fare of $30.00 has been calculated
    And the rider has a $10.00 promo credit in their wallet
    When the promo is applied
    Then the rider should be charged $20.00
    And the driver should still receive the full $30.00 fare
    And HOVR absorbs the promotional discount

  Scenario: Promo exceeds fare amount
    Given a fare of $8.00 has been calculated
    And the rider has a $10.00 promo credit
    When the promo is applied
    Then the rider should be charged $0.00
    And $2.00 should remain in the rider's wallet
    And the driver should receive the full $8.00

  # --- MARKETPLACE-SPECIFIC RATES ---

  Scenario: Different rates for GTA vs Ottawa
    Given GTA has base fare $3.50 and Ottawa has base fare $3.00
    When identical-distance rides are calculated in each marketplace
    Then the GTA fare should differ from the Ottawa fare
    And each should use their marketplace-specific rate card

  # --- TIP PROCESSING ---

  Scenario: Tip added to completed fare
    Given a trip was completed with fare $25.00
    When the rider adds a $5.00 tip via AddTipRequest
    Then the driver's total earning should be $30.00
    And the tip should not be subject to any deduction

  # --- FARE FOR CANCELLED TRIPS ---

  Scenario: No-show fee charged to rider
    Given a driver has arrived and waited the required time
    When the driver reports a no-show
    Then a no-show fee should be charged to the rider
    And the driver should receive the no-show compensation

  Scenario: Rider cancellation fee
    Given a rider cancels after a driver has been dispatched
    And the driver is already en route
    Then a cancellation fee may be charged to the rider

  Scenario: No charge for early cancellation
    Given a rider cancels before a driver is dispatched
    Then no cancellation fee should be charged

  # --- EDGE CASES ---

  Scenario: Very short trip fare
    Given a ride of 0.5km and 2 minutes
    When the fare is calculated
    Then the fare should not be below the minimum fare threshold

  Scenario: Very long trip fare
    Given a ride of 100km and 90 minutes
    When the fare is calculated
    Then the fare should correctly compute without overflow
    And all components should scale linearly

  Scenario: Zero-distance fare request
    Given pickup and drop-off are at the same coordinates
    When a fare is calculated
    Then the system should return a validation error or minimum fare
