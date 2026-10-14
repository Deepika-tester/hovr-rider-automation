@rider @profile
Feature: Rider Account, Profile and Settings
  As a registered rider
  I want to manage my profile, saved places, wallet and app settings
  So that I can personalise my HOVR experience

  # Explored on a real build (rider dev v6.3.0+588, emulator, India marketplace) on 2026-10-14.
  # The Account tab (third bottom tab) lists: the profile card, Inbox / Wallet / Help tiles, "Refer a
  # friend, earn $5", Settings, "Earn by driving", "Update the app" and "Need more help?".
  #
  # Findings from the run (these replace the earlier guess-based scenarios):
  #  - Profile shows Legal Name, Preferred Name, Phone, Email, plus Delete Account and Logout. The pencil
  #    opens "Edit profile": Preferred name / First name / Last name are editable; phone and email are
  #    read-only. Saving shows the toast "Updated successfully!". An empty first name is NOT saved: the
  #    form just stays open (no inline error text).
  #  - Saved places live under Settings > Home / Add Work / Shortcuts (all open the same "Saved Places"
  #    list). Add = "+" -> Name + Address (address autocompletes) -> Save. Saving blank fields shows
  #    "Fill the field!". Opening a place gives "Edit saved place" with a trash icon (delete is immediate).
  #  - Settings has two switches (Wheelchair Assistance, Verify Your Ride) and Language / Notifications.
  #    Language offers English (US), French (Canada), Spanish, Turkish, Hindi, Punjabi, Arabic.
  #  - The wallet has no balance, Google Pay as the default method, and only "Credit or debit card" can
  #    be added. Inbox and Trips are empty for a new rider.
  #  - The app shows "$" amounts even in the India marketplace (dev build quirk, noted not asserted).
  #
  # NOT automated: Delete Account (irreversible) and switching the language (re-localises every later
  # screen) -- their entries are only checked for presence.

  Background:
    Given I am a registered rider
    And I am logged into the rider app
    And I am located in a supported marketplace

  # --- ACCOUNT MENU ---

  @positive
  Scenario: Account page lists every entry
    Given I am on the Account page
    Then I should see all of these texts:
      | Inbox                |
      | Wallet               |
      | Help                 |
      | Refer a friend       |
      | Settings             |
      | Earn by driving      |
      | Update the app       |
      | Need more help?      |

  # --- PROFILE ---

  @positive
  Scenario: View my profile
    Given I am on the Account page
    When I open my profile
    Then I should see all of these texts:
      | Legal Name        |
      | Preferred Name    |
      | Phone             |
      | Email             |
      | Delete Account    |
      | Logout            |

  @positive
  Scenario: Edit my preferred name
    Given I am on the Account page
    When I open my profile
    And I tap the edit pencil
    And I set my preferred name to "Janu"
    And I tap "Update"
    Then I should see the text "Updated successfully"
    And I should see the text "Janu"

  @negative
  Scenario: Saving with an empty first name does not update the profile
    Given I am on the Account page
    When I open my profile
    And I tap the edit pencil
    And I clear the first name on the edit form
    And I tap "Update"
    Then I should see the text "Edit profile"
    And I should not see the text "Updated successfully"

  @negative
  Scenario: Phone number and email cannot be edited
    Given I am on the Account page
    When I open my profile
    And I tap the edit pencil
    Then the edit form should have exactly 3 editable fields

  # --- SAVED PLACES ---

  @positive
  Scenario: A new rider has no saved places
    Given I am on the Account page
    When I open the Saved Places list
    Then I should see the text "No saved places found"

  @positive
  Scenario: Add a saved place and delete it again
    Given I am on the Account page
    And I open the Saved Places list
    When I tap the add place button
    And I enter the place name "Gym"
    And I enter the place address "CP67 Mall Mohali"
    And I choose the first address suggestion
    And I tap "Save"
    Then I should see the text "Gym"
    When I open the saved place "Gym"
    And I tap the delete icon
    Then I should see the text "No saved places found"

  @negative
  Scenario: Saving a place with blank fields is rejected
    Given I am on the Account page
    And I open the Saved Places list
    When I tap the add place button
    And I tap "Save"
    Then I should see the text "Fill the field"
    And I should see the text "Add new place"

  # --- SETTINGS ---

  @positive
  Scenario: Settings shows places, accessibility, safety and preferences
    Given I am on the Account page
    When I tap "Settings"
    Then I should see all of these texts:
      | Add Work              |
      | Shortcuts             |
      | Wheelchair Assistance |
      | Verify Your Ride      |
      | Language              |
      | Notifications         |

  @positive
  Scenario: Turn Wheelchair Assistance on and off
    Given I am on the Account page
    When I tap "Settings"
    And I toggle the "Wheelchair Assistance" switch
    Then the "Wheelchair Assistance" switch should be on
    When I toggle the "Wheelchair Assistance" switch
    Then the "Wheelchair Assistance" switch should be off

  @positive
  Scenario: Turn Verify Your Ride on and off
    Given I am on the Account page
    When I tap "Settings"
    And I toggle the "Verify Your Ride" switch
    Then the "Verify Your Ride" switch should be on
    When I toggle the "Verify Your Ride" switch
    Then the "Verify Your Ride" switch should be off

  @positive
  Scenario: Language settings list the supported languages
    Given I am on the Account page
    When I tap "Settings"
    And I open the "Language" setting
    Then I should see all of these texts:
      | English (US)    |
      | French (Canada) |
      | Spanish         |
      | Turkish         |
      | Hindi           |
      | Punjabi         |
      | Arabic          |

  @positive
  Scenario: Notification preferences list their categories
    Given I am on the Account page
    When I tap "Settings"
    And I open the "Notifications" setting
    Then I should see all of these texts:
      | Promotions and Tips |
      | Travelling          |
      | Partner Offers      |
      | Suggestions         |
      | Receipts            |

  # --- WALLET, INBOX, HELP, REFERRAL ---

  @positive
  Scenario: A new rider's wallet has no balance and lists Google Pay
    Given I am on the Account page
    When I open my wallet
    Then I should see all of these texts:
      | Total              |
      | No balance yet     |
      | Payment Methods    |
      | Google Pay         |
      | Add payment method |

  @positive
  Scenario: Choosing Google Pay makes it the default payment method
    Given I am on the Account page
    When I open my wallet
    And I tap "Google Pay"
    Then I should see the text "Default"

  @positive
  Scenario: Adding a payment method offers credit or debit card
    Given I am on the Account page
    When I open my wallet
    And I tap "Add payment method"
    Then I should see the text "Add a payment method"
    And I should see the text "Credit or debit card"

  @positive
  Scenario: Inbox is empty for a new rider
    Given I am on the Account page
    When I tap "Inbox"
    Then I should see all of these texts:
      | Broadcast |
      | Trip      |
      | Payment   |
      | Safety    |
    And I should see the text "all caught up"

  @positive
  Scenario: Help opens the support tickets page
    Given I am on the Account page
    When I tap "Need more help?"
    Then I should see all of these texts:
      | Support tickets |
      | FAQ             |
      | No open tickets |
      | Create ticket   |

  @positive
  Scenario: Refer a friend shows the referral link and code entry
    Given I am on the Account page
    When I tap "Refer a friend, earn $5"
    Then I should see all of these texts:
      | Refer and earn          |
      | REFERRAL LINK           |
      | Share with Rider        |
      | Enter a referral code   |
      | Referral history        |

  # --- TRIPS ---

  @positive
  Scenario: Trips tab is empty for a rider with no rides
    Given I am on the Account page
    When I open the "Trips" bottom tab
    Then I should see all of these texts:
      | Upcoming         |
      | No upcoming trips |
      | Past             |
      | HOVR ride        |
