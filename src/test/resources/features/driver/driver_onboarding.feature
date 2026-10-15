@onboarding
Feature: Driver onboarding
  As a newly registered driver
  I want to complete my onboarding steps
  So that I can go online and accept rides

  Background:
    Given the driver is logged in
    And the driver has not completed onboarding

  @smoke
  Scenario: Onboarding checklist is shown
    When the driver opens the app
    Then the text "Welcome, Alex" is shown
    And the text "Here's what you need to do to set up your account." is shown
    And the onboarding steps are listed:
      | step               |
      | Driver             |
      | Vehicle            |
      | City Requirements  |
      | Membership         |
      | Banking            |
      | Training           |

  Scenario: Rideshare and Co-Commute tabs are shown
    When the driver opens the onboarding screen
    Then the "Rideshare" tab is shown
    And the "Co-Commute" tab is shown

  Scenario Outline: Driver opens each onboarding step
    When the driver taps the "<step>" step
    Then the "<title>" screen is shown
    And the text "Please submit the following documents to continue." is shown

    Examples:
      | step              | title              |
      | Driver            | Driver Documents   |
      | Vehicle           | Vehicle Documents  |
      | City Requirements | City Requirements  |
      | Training          | Training Documents |
      | Banking           | Banking Documents  |

  @upload
  Scenario: Driver uploads a document from the camera
    Given the driver is on the "Driver Documents" screen
    When the driver taps "Upload" on "Driver's licence"
    And the driver chooses "Camera"
    And the driver captures a photo
    Then the text "Uploading document…" is shown
    And the document status becomes "Submitted"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @upload @blocked
  Scenario Outline: Document status badges
    Given a driver document with status "<status>"
    When the driver opens the "Driver Documents" screen
    Then the document shows the "<badge>" badge

    Examples:
      | status    | badge        |
      | missing   | Missing      |
      | submitted | Submitted    |
      | review    | Under review |
      | approved  | Approved     |
      | rejected  | Rejected     |

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @upload @negative @blocked
  Scenario: Rejected document shows the reason
    Given a driver document was rejected with reason "Image is blurry"
    When the driver opens the rejected document
    Then the "Reason" section shows "Image is blurry"
    And the "Upload" button is shown

  @status
  Scenario: Driver refreshes the onboarding status
    Given the driver is on the onboarding screen
    When the driver taps "Refresh status"
    Then the onboarding checklist is reloaded

  @locked
  Scenario: Locked features before onboarding is complete
    Given the driver is on the onboarding screen
    Then the text "Complete rideshare onboarding to unlock this." is shown on locked features
    And the "Referral Program" card shows "Verify your ID to start earning rewards"
    And the "Member Benefits" card shows "Verify your ID and subscribe to access your benefits"

  @locked @negative
  Scenario: Driver cannot go online until documents are approved
    Given the driver documents are not approved
    When the driver opens the home screen
    Then the text "Need to get driver documents approved." is shown

  @complete @smoke
  Scenario: Driver with approved onboarding lands on home
    Given all onboarding steps are approved
    When the driver opens the app
    Then the driver home screen is shown
    And the "Go Online" button is shown
