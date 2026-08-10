@rider @rating
Feature: Rider Rating and Feedback
  As a rider who has completed a trip
  I want to rate my driver and provide feedback
  So that HOVR can maintain quality standards

  # Derived from: hovr-rider-app/lib/features/ride-booking/.../rating-rider-view/
  # and refer-and-earn/presentation/bloc/referral_event.dart (SubmitRatingEvent)
  # Rating fields: rating (int), rideId, comments (list), suggestion, ratingGivenTo, furtherSupport, status
  # States: RatingState -> AddTipState (after optional tip)

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I have just completed a trip

  # --- RATING FLOW ---

  Scenario: Rate driver after trip completion
    Given the trip has ended and I am on the rating screen
    When I select a 5-star rating
    And I tap "Submit"
    Then the rating should be submitted with:
      | field         | value            |
      | rating        | 5                |
      | ratingGivenTo | driver           |
    And I should be navigated to the home screen

  Scenario: Rate driver with low rating
    Given the trip has ended and I am on the rating screen
    When I select a 2-star rating
    Then I should be prompted for additional feedback
    And I should see predefined complaint options

  Scenario: Skip rating
    Given the trip has ended and I am on the rating screen
    When I tap "Skip" or close the rating view
    Then I should be navigated to the home screen
    And no rating should be submitted for this trip

  # --- COMPLIMENTS ---

  Scenario: Compliment the driver
    Given I am on the rating screen
    When I select a 5-star rating
    And I tap "Add Compliment"
    Then I should see the compliment driver view
    And I should see predefined compliment categories like:
      | compliment          |
      | Great conversation  |
      | Clean car           |
      | Expert navigation   |
      | Smooth ride         |
    When I select "Clean car" and "Expert navigation"
    And I tap "Submit"
    Then the rating should include the selected compliments in comments

  # --- COMPLAINTS ---

  Scenario: Report an issue with the driver
    Given I am on the rating screen
    When I select a 1-star rating
    And I tap "Report Issue"
    Then I should see the complaint driver view
    And I should see complaint categories
    When I select a complaint reason
    And I enter additional details "Driver was rude"
    And I mark "I need further support" as true
    Then the rating should be submitted with furtherSupport as true
    And a support ticket should be created

  Scenario: Submit rating with a text suggestion
    Given I am on the rating screen
    When I select a 3-star rating
    And I enter a text suggestion "Route could have been faster"
    And I tap "Submit"
    Then the rating should include the suggestion text

  # --- TIPPING ---

  Scenario: Add a tip after rating
    Given I have submitted a rating for my trip
    When I see the tip prompt
    And I select a "$5" tip
    Then an AddTipRequest should be sent
    And the tip amount should be added to the trip fare
    And I should see confirmation of the tip

  Scenario: Add a custom tip amount
    Given I see the tip prompt
    When I tap "Custom amount"
    And I enter "$8.50"
    And I confirm
    Then the custom tip should be applied to the trip

  Scenario: Skip tipping
    Given I see the tip prompt
    When I tap "No thanks" or skip
    Then no tip should be added
    And I should be navigated to the home screen

  # --- EDGE CASES ---

  Scenario: Submit rating with no network
    Given I am on the rating screen
    And there is no network connectivity
    When I submit a 4-star rating
    Then the rating should be queued locally
    And it should be submitted when connectivity is restored

  Scenario: Navigate back to rating after leaving
    Given I completed a trip but did not rate the driver
    When I check my trip history
    Then I should see an option to rate the unrated trip
