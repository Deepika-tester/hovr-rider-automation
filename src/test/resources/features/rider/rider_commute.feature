@rider @commute
Feature: Rider Co-Commute
  As a rider
  I want to browse and post commutes with neighbours
  So that I can share a daily commute with drivers heading the same way

  # Explored on a real build (rider dev v6.3.0+588, emulator, India marketplace) on 2026-10-12.
  # The home sheet has "Rideshare" and "Co-Commute" tabs. The Co-Commute page has three tabs
  # (Nearby journeys / My trips / My bookings), each with its own header line and empty state, and a
  # "Post your own commute" button that opens a "Plan a Co-Commute" form (from/to, Add stop, Schedule).
  #
  # Notes from the run: a search the app can't resolve shows "Hmm, we couldn't find that location" and
  # ALSO lists the rider's "Recent" places underneath (clickable rows shaped like real results), so
  # "no results" is asserted via the message + not advancing, not by counting rows. The middle tab is
  # "My trips" (its two chips are "My requests" and "My bookings"), but the app restores the last tab
  # and once came up with that tab labelled "My requests", so tests find it by position.
  #
  # NOT covered here: actually posting a commute. Posting asks for a default payment method ("About
  # $1 is held ... until the driver starts"); the sandbox card saves (a first attempt got a 403, the
  # retry worked) but the post request then timed out on the server, so a commute was never created.
  # Scenarios that need an existing commute (details, cancel, "Commutes near you" cards) wait for that.

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I am located in a supported marketplace

  # --- BROWSE ---

  @positive
  Scenario: Open the Co-Commute page from the home screen
    Given I am on the ride home screen
    When I open the Co-Commute tab
    Then I should see the Co-Commute page
    And I should see the tabs "Nearby journeys", "My trips" and "My bookings"
    And I should see the "Post your own commute" button

  @positive
  Scenario: Nearby journeys shows its header, filters and empty state
    Given I am on the Co-Commute page
    When I open the "Nearby journeys" tab
    Then I should see the text "Drivers heading your way"
    And I should see the filter chips "Today", "Morning" and "Downtown"
    And I should see the text "No nearby journeys right now"

  @positive
  Scenario: My trips is empty for a rider who has not posted a commute
    Given I am on the Co-Commute page
    When I open the "My trips" tab
    Then I should see the text "Your posted commutes"
    And I should see the text "You haven't posted a commute yet"
    And I should see the text "My requests"

  @positive
  Scenario: My bookings is empty for a rider with no booked seats
    Given I am on the Co-Commute page
    When I open the "My bookings" tab
    Then I should see the text "Seats you've booked"
    And I should see the text "You haven't booked a seat yet"

  @positive
  Scenario: Applying a time-of-day filter keeps the page working
    Given I am on the Co-Commute page
    And I open the "Nearby journeys" tab
    When I tap the filter chip "Morning"
    Then I should see the Co-Commute page
    And I should see the text "No nearby journeys right now"

  # --- PLAN A COMMUTE ---

  @positive
  Scenario: Open the Plan a Co-Commute form
    Given I am on the Co-Commute page
    When I tap "Post your own commute"
    Then I should see the text "Plan a Co-Commute"
    And I should see the text "Add stop"
    And I should see the text "Schedule"

  @positive
  Scenario: Search a destination in the commute form
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "CP67 Mall"
    Then I should see commute address suggestions

  @positive
  Scenario: Choosing a destination opens the time picker with the 90-minute rule
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "CP67 Mall"
    And I pick the first commute suggestion
    Then I should see the text "Choose a time"
    And I should see the text "at least 90 minutes from now"
    And I should see the text "Confirm and continue"

  # --- NEGATIVE ---

  @negative
  Scenario: Commute destination that does not exist
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "zzqxqx 999 nowhere lane"
    Then I should see the text "find that location"
    And I should not see the text "Choose a time"

  @negative
  Scenario: Commute destination outside the supported area
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "100 Queen St W, Toronto"
    Then I should see the text "find that location"
    And I should not see the text "Choose a time"

  @negative
  Scenario: Commute destination with only special characters
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "!@#$%^&*()"
    Then I should see the text "find that location"
    And I should see the text "Plan a Co-Commute"

  @negative
  Scenario: Posting a commute asks for a default payment method and can be cancelled
    Given I am on the Plan a Co-Commute form
    When I type the commute destination "CP67 Mall"
    And I pick the first commute suggestion
    And I tap "Confirm and continue"
    Then I should see the text "Choose a default payment method"
    And I should see the text "held on your default payment method"
    When I tap "Cancel"
    Then I should see the text "Plan a Co-Commute"
    And I should not see the text "Choose a default payment method"
