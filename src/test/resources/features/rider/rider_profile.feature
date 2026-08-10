@rider @profile
Feature: Rider Profile and Settings
  As a registered rider
  I want to manage my profile, saved places, and app settings
  So that I can personalize my HOVR experience

  # Derived from: hovr-rider-app/lib/features/home-view/presentation/pages/settings/
  # Pages: account_view.dart, edit_profile.dart, user_profile_screen.dart,
  #         saved_places_view.dart, add_new_place_view.dart, setting_view.dart,
  #         language_settings_screen.dart, notification_preference_screen.dart
  # Features: Profile edit, saved places (Home, Work, custom), language (EN/FR),
  #           notification preferences, trip history, messages

  Background:
    Given I am a registered rider
    And I am logged into the rider app

  # --- PROFILE ---

  Scenario: View my profile
    Given I navigate to the account section
    When I tap on my profile
    Then I should see the user profile screen with:
      | field          |
      | Name           |
      | Email          |
      | Phone number   |
      | Profile photo  |

  Scenario: Edit profile name
    Given I am on the edit profile screen
    When I change my first name to "Janet"
    And I tap "Save"
    Then my profile name should be updated to "Janet"
    And I should see a success confirmation

  Scenario: Edit profile with empty name
    Given I am on the edit profile screen
    When I clear the first name field
    And I tap "Save"
    Then I should see a validation error "Name is required"

  # --- SAVED PLACES ---

  Scenario: View saved places
    Given I navigate to "Saved Places"
    Then I should see my saved places list
    And I should see preset categories "Home" and "Work"

  Scenario: Add home address
    Given I am on the saved places screen
    And "Home" has not been set
    When I tap on "Home"
    And I search for "123 Maple Ave, Toronto"
    And I select the address from suggestions
    And I save it
    Then "Home" should be set to "123 Maple Ave, Toronto"
    And it should appear on the home screen for quick booking

  Scenario: Add work address
    Given I am on the saved places screen
    When I tap on "Work"
    And I search for and select "200 Bay St, Toronto"
    And I save it
    Then "Work" should be set to "200 Bay St, Toronto"

  Scenario: Add a custom saved place
    Given I am on the saved places screen
    When I tap "Add New Place"
    And I enter the label "Gym"
    And I search for and select "50 Bloor St W, Toronto"
    And I save it
    Then I should see "Gym" in my saved places list

  Scenario: Edit a saved place
    Given I have a saved place "Home" at "123 Maple Ave, Toronto"
    When I tap on "Home"
    And I select "Edit"
    And I change the address to "456 Oak St, Toronto"
    And I save it
    Then "Home" should be updated to "456 Oak St, Toronto"

  Scenario: Delete a saved place
    Given I have a custom saved place "Gym"
    When I tap on "Gym"
    And I select "Delete"
    And I confirm the deletion
    Then "Gym" should be removed from my saved places

  # --- RIDE HISTORY ---

  Scenario: View ride history
    Given I have completed rides
    When I navigate to the trips section
    Then I should see a list of my past trips
    And each trip should display:
      | field            |
      | Date             |
      | Pickup address   |
      | Drop-off address |
      | Fare amount      |
      | Trip status      |

  Scenario: View trip receipt from history
    Given I am on the trips list
    When I tap on a completed trip
    Then I should see the trip receipt view
    And I should see the route name and time
    And I should see the fare breakdown

  Scenario: View ride history with no trips
    Given I have not completed any trips
    When I navigate to the trips section
    Then I should see an empty state message

  # --- SETTINGS ---

  Scenario: Change app language to French
    Given I navigate to "Settings" then "Language"
    When I select "French"
    Then the app language should change to French
    And all UI text should be displayed in French

  Scenario: Change app language to English
    Given the app is currently in French
    When I navigate to language settings
    And I select "English"
    Then the app should switch back to English

  Scenario: Manage notification preferences
    Given I navigate to "Notification Settings"
    Then I should see notification categories
    When I toggle a notification category off
    Then I should no longer receive notifications for that category

  # --- MESSAGES ---

  Scenario: View messages inbox
    Given I navigate to "Messages"
    Then I should see my messages from HOVR
    And messages should be sorted by date with newest first

  # --- ACCOUNT ACTIONS ---

  Scenario: Log out of the app
    Given I am on the settings screen
    When I tap "Log Out"
    And I confirm the logout
    Then I should be logged out
    And I should be returned to the landing screen

  # --- HELP & SUPPORT ---

  Scenario: Access help from settings
    Given I am on the settings screen
    When I tap "Help"
    Then I should see help categories:
      | category             |
      | Using HOVR           |
      | Payments and Pricing |
      | Account and Data     |
      | App and Features     |

  Scenario: Access Zendesk support
    Given I am on the help screen
    When I tap "Need More Help"
    Then I should be directed to the Zendesk support interface

  Scenario: Search help articles
    Given I am on the help screen
    When I tap the search bar
    And I search for "cancel ride"
    Then I should see relevant FAQ articles
