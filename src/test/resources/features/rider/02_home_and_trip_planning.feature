@home
Feature: Home screen and trip planning
  As a signed-in rider
  I want to plan a trip from the home screen
  So that I can quickly choose pickup and drop-off

  Background:
    Given I am signed in as a rider
    And location permission is granted
    And I am on the home screen

  @smoke
  Scenario: Home screen shows main navigation
    Then I should see the bottom navigation tabs "Ride", "Trips" and "Account"
    And I should see the "Where to?" search field
    And I should see the "Rideshare" and "Co-Commute" tabs

  # needs a brand-new rider so the first-visit guide is showing: the app is wiped before the session (@keep-guide)
  @keep-guide
  Scenario: Swipe guide on first visit
    Given this is my first visit to the home screen
    Then I should see "Swipe the panel"
    When I swipe up on the panel
    Then I should see "Nice! Now swipe down to close it."
    When I swipe down on the panel
    Then I should see "You're all set"

  # needs a brand-new rider so the first-visit guide is showing: the app is wiped before the session (@keep-guide)
  @keep-guide
  Scenario: Skip the swipe guide
    Given the swipe guide is displayed
    When I tap "Skip"
    Then the swipe guide is dismissed
    And it is not shown on the next launch

  Scenario: Open plan your trip
    When I tap "Where to?"
    Then I should see "Plan your trip"
    And the pickup field is prefilled with "Current location"

  Scenario: Search destination with autocomplete
    Given I am on the plan your trip screen
    When I type "Union Station" in the drop-off field
    Then I should see place suggestions
    When I select the first suggestion
    Then I should see the route preview

  Scenario: Recent searches
    Given I have previous searches
    When I open the plan your trip screen
    Then I should see the "Recent" section
    When I tap "Clear all"
    Then the recent searches list is empty

  Scenario: Set location on map
    Given I am on the plan your trip screen
    When I tap "Set location on map"
    And I drag the map to a new position
    Then I should see "Fetching address..."
    And the address field updates to the pinned location

  Scenario: Add and remove stops
    Given I am on the plan your trip screen with a destination
    When I tap "Add stop"
    Then I should see "Add stop 1"
    When I try to continue with an empty stop
    Then I should see "Select a stop or remove it to continue."

  Scenario: Location unavailable
    Given location services are disabled
    When I open the plan your trip screen
    Then I should see "Unable to fetch current location."

  Scenario: Use Home shortcut
    Given I have saved a "Home" place
    When I tap the "Home" shortcut
    Then the drop-off is set to my Home address

  Scenario: Add Home when not saved
    Given I have no saved Home place
    When I tap "Add Home"
    Then I should see the add new place screen

  Scenario: Schedule a ride for later
    Given I am on the plan your trip screen
    When I tap the time selector
    Then I should see "Choose a time"
    When I choose a pickup time 2 hours from now
    And I tap "Confirm and continue"
    Then the trip is planned for the selected time

  Scenario: Invalid scheduled time
    Given I am on the choose time screen
    When I choose a pickup time 5 minutes from now
    Then I should see "Choose a valid pickup time"

  # BLOCKED: the app's place search only offers places near the rider (searching Mumbai, London or an island answers "Hmm, we couldn't find that location"), so such a pickup/destination cannot be chosen from the UI. The backend behaviour is covered by the API suite (04_ride_service: 'pickup in not supported area').
  @blocked
  Scenario: Pickup in unsupported area
    Given my pickup point is outside the service area
    When I request route options
    Then I should see "Pickup in not supported area"

  # BLOCKED: the app's place search only offers places near the rider (searching Mumbai, London or an island answers "Hmm, we couldn't find that location"), so such a pickup/destination cannot be chosen from the UI. The backend behaviour is covered by the API suite (04_ride_service: 'pickup in not supported area').
  @blocked
  Scenario: No route found
    Given my destination has no drivable route
    When I request route options
    Then I should see "No route found for the destination!"
