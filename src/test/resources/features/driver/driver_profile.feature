@driver @profile
Feature: Driver Profile and Settings
  As a HOVR driver
  I want to manage my profile, vehicle, documents, and app settings
  So that I can keep my account up to date

  # Derived from: hovr-driver-app/lib/features/home-view/presentation/pages/drawer-screens/
  # and documents-feature/
  # Pages: account.dart, my_vehicle_details.dart, banking_details.dart, app_settings.dart,
  #         driver_profile_screen.dart, language_settings_screen.dart
  # Documents: document_list_view.dart, document_scope_view.dart, document_upload_view.dart
  # Banking widgets: account_status_card.dart, capabilities_card.dart,
  #                   manage_bank_button.dart, payout_status_card.dart

  Background:
    Given I am an approved HOVR driver
    And I am logged into the driver app

  # --- PROFILE ---

  Scenario: View driver profile
    Given I navigate to my profile screen
    Then I should see my profile information:
      | field          |
      | Name           |
      | Email          |
      | Phone number   |
      | Profile photo  |
      | Driver rating  |
      | Preferred name |

  Scenario: Edit preferred name
    Given I am on my profile screen
    When I tap "Edit"
    And I change my preferred name to "Ahmad K."
    And I save
    Then my preferred name should be updated

  # --- ACCOUNT ---

  Scenario: View account details
    Given I navigate to the account screen
    Then I should see sections for:
      | section           |
      | My Vehicle        |
      | Banking Details   |
      | App Settings      |
      | Documents         |

  # --- VEHICLE DETAILS ---

  Scenario: View my vehicle details
    Given I navigate to "My Vehicle"
    Then I should see my vehicle information:
      | field         |
      | Make          |
      | Model         |
      | Year          |
      | Color         |
      | License plate |
      | Photos        |

  Scenario: Update vehicle details
    Given I am on the my vehicle screen
    When I update my license plate number
    And I save the changes
    Then the vehicle details should be updated

  # --- BANKING ---

  Scenario: View banking details
    Given I navigate to "Banking Details"
    Then I should see:
      | section           |
      | Account status    |
      | Payout status     |
      | Capabilities      |
      | Manage bank       |

  Scenario: View payout status
    Given I am on the banking details screen
    Then I should see the payout status card
    And I should see my current payout balance
    And I should see recent payout history

  Scenario: Update banking information
    Given I am on the banking details screen
    When I tap "Manage Bank"
    Then I should be able to update my bank account details
    When I enter new banking details
    And I save
    Then my banking information should be updated

  # --- DOCUMENTS ---

  Scenario: View document list
    Given I navigate to the documents screen
    Then I should see all my uploaded documents
    And each document should show its status:
      | status     |
      | Approved   |
      | Pending    |
      | Rejected   |
      | Expired    |

  Scenario: View document scope
    Given I am on the document list
    When I tap on a specific document
    Then I should see the document scope view
    And I should see the document details and status

  Scenario: Re-upload a rejected document
    Given a document has been rejected
    When I tap on the rejected document
    And I upload a new version
    And I submit
    Then the document status should change to "Pending"

  Scenario: Document nearing expiration
    Given my vehicle insurance expires in 30 days
    When I view my documents
    Then I should see an expiration warning on the insurance document
    And I should be prompted to upload a renewed document

  # --- APP SETTINGS ---

  Scenario: View app settings
    Given I navigate to "App Settings"
    Then I should see settings options

  Scenario: Change language to French
    Given I navigate to language settings
    When I select "French"
    Then the app language should change to French
    And all UI text should display in French

  # --- MESSAGES ---

  Scenario: View messages
    Given I navigate to "Messages"
    Then I should see messages from HOVR
    And messages should be sorted by date

  # --- HELP AND SUPPORT ---

  Scenario: Access help and support
    Given I am on the home screen
    When I tap "Help and Support"
    Then I should see the help and support view
    And I should see support contact options

  Scenario: Contact support directly
    Given I am on the help screen
    When I tap "Contact Support"
    Then I should see options to call or email HOVR support

  # --- ALERTS ---

  Scenario: View alerts list
    Given there are system alerts
    When I navigate to alerts
    Then I should see the alerts list
    And each alert should show title, date, and priority

  Scenario: View single alert details
    Given I am on the alerts list
    When I tap on a specific alert
    Then I should see the full alert details

  Scenario: View alerts on home screen
    Given there are important alerts
    When I am on the home screen
    Then I should see alert banners on the home view

  # --- REFER AND EARN ---

  Scenario: View driver referral code
    Given I navigate to "Refer and Earn"
    Then I should see my unique driver referral code
    And I should see the referral reward structure

  Scenario: Share driver referral code
    Given I am on the refer and earn screen
    When I tap "Share"
    Then the system share sheet should open
    And the referral link should be sharable

  # --- DRAWER NAVIGATION ---

  Scenario: Navigate via side drawer
    Given I am on the home screen
    When I open the side drawer
    Then I should see navigation options:
      | option            |
      | Home              |
      | Earnings          |
      | Messages          |
      | Membership        |
      | Account           |
      | Refer and Earn    |
      | Help and Support  |

  # --- EDGE CASES ---

  Scenario: Profile update with network error
    Given I am editing my profile
    And there is no network
    When I try to save changes
    Then I should see a network error
    And changes should not be persisted

  Scenario: Log out from driver app
    Given I am on the account screen
    When I tap "Log Out"
    And I confirm
    Then I should be logged out
    And I should be returned to the landing screen
