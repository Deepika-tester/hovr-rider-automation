@referral
Feature: Refer and earn
  As a rider
  I want to invite friends
  So that I can earn rewards

  Background:
    Given I am signed in as a rider
    And I open "Refer and earn"

  Scenario: Copy referral link
    When I tap the referral link
    Then I should see "Copied to clipboard!"

  Scenario: Share referral link
    When I tap "Share with Rider"
    Then the system share sheet opens with my invite link

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @fresh-install @india-only-skip
  Scenario: Redeem a referral code
    When I tap "Enter a referral code"
    And I enter "FRIEND5"
    And I tap "Redeem"
    Then I should see "Redeemed successfully!"

  # INDIA-ONLY-SKIP: referral is only enabled for the Canada marketplace (project owner); skipped in the India run, run in the Canada run.
  @fresh-install @india-only-skip
  Scenario: Redeem with empty code
    Given I am on the redeem referral code screen
    When I tap "Redeem" without a code
    Then I should see "Enter the referral code!"

  Scenario: Empty referral progress
    Given I have not referred anyone
    When I open "Referral progress"
    Then I should see "No referrals yet"

  # BLOCKED: needs a referred friend who has completed rides (another rider plus a driver).
  @blocked
  Scenario: Referral progress tracking
    Given my friend has completed 1 of 3 rides
    When I open "Referral progress"
    Then I should see "1/3 rides"
