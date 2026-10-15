@account
Feature: Account, profile and settings
  As a rider
  I want to manage my profile and preferences

  Background:
    Given I am signed in as a rider
    And I open the "Account" tab

  Scenario: View profile
    When I tap "View profile"
    Then I should see "Legal Name", "Phone" and "Email"

  Scenario: Edit profile
    Given I am on the edit profile screen
    When I change preferred name to "JD"
    And I tap "Update"
    Then I should see "Updated successfully!"

  Scenario: Invalid preferred name
    Given I am on the edit profile screen
    When I change preferred name to "@@"
    And I tap "Update"
    Then I should see "Preferred name is not valid"

  # BLOCKED: needs a photo in the emulator gallery or the camera through the system picker; not wired up on the emulator.
  @blocked
  Scenario: Update profile photo
    When I choose a new profile photo
    Then I should see "Profile photo updated!"

  Scenario: Add a saved place
    Given I open "Saved Places"
    When I tap "Add new place"
    And I enter name "Gym" and select an address suggestion
    And I tap "Save"
    Then I should see "Gym is saved."

  Scenario: Saved place requires all fields
    Given I am on the add new place screen
    When I tap "Save" with empty fields
    Then I should see "Please fill all fields and select an address from suggestions"

  Scenario: Edit and delete a saved place
    Given I have a saved place "Gym"
    When I open "Edit saved place"
    And I tap "Delete"
    Then "Gym" is removed from saved places

  # NOTE: switching the language re-localises the whole app, so the suite wipes the app after each example (@language).
  @language
  Scenario Outline: Change app language
    Given I open "Settings"
    When I open "Language"
    And I select "<language>"
    Then I should see "Language updated to <language>"

    Examples:
      | language        |
      | Spanish         |
      | French (Canada) |
      | English (US)    |

  Scenario: Notification preferences require one channel
    Given I open notification preferences for "Receipts"
    When I turn off all of "Email", "Push" and "SMS"
    Then I should see "At least one option must be activated."

  Scenario: Check for app update
    Given I open the app update screen
    When I tap "Check for update"
    Then I should see "You're on the latest update." or "Update ready"

  Scenario: Logout
    When I tap "Logout"
    Then I am returned to the landing screen

  # BLOCKED: irreversible -- confirming permanently deletes the test rider. The confirmation dialog itself is checked in supplement scenarios instead.
  @blocked
  Scenario: Delete account
    When I tap "Delete Account"
    Then I should see "Confirm Account Deletion"
    When I confirm
    Then my account is deleted and I am signed out
