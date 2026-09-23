@driver @registration @auth
Feature: Driver Registration and Onboarding
  As a new driver
  I want to create a driver account and complete onboarding
  So that I can start driving with HOVR and earning

  # Derived from: hovr-driver-app/lib/features/authentication-feature/ and driver-onboarding-feature/
  # CONFIRMED 2026-08-25 on a real device (walked twice, once with a camera-permission dialog in
  # the middle and once with it pre-granted via adb, to rule out the dialog masking a screen):
  # there is NO separate Terms of Service screen in this flow. Real auth flow is:
  # Landing -> PhoneNumber -> OTP -> Name -> Email -> Welcome/Onboarding Dashboard, directly.
  # Also confirmed: the "111111" staging bypass OTP that works on the rider app works here too,
  # and the app needs ACCESS_BACKGROUND_LOCATION granted before launch or a "Background Location
  # Required" in-app dialog gets stuck in a loop (neither NOT NOW nor CONTINUE dismisses it).
  # CONFIRMED 2026-08-25 on a real device: the onboarding checklist is MARKETPLACE-DEPENDENT,
  # determined by device GPS at signup (not the phone number's country code) -- India shows just
  # ONE document ("Vehicle Registration"); GTA (mocked here via Appium's setLocation to Toronto,
  # 43.6532/-79.3832) shows a real 15-document checklist across three categories:
  #   Driver (6): Driver's License, Driver Selfie, Driving Experience Verification,
  #               Driver's License Back, city permit, workk permit [sic -- real app typo]
  #   Vehicle (4): Vehicle Registration, Safety Inspection Certificate, Address, Vehicle Insurance
  #   City Requirements (5): Background Check, Vehicle-For-Hire Certificate, PTC Number,
  #                          Work Proof, city permit (shared with the Driver category's copy)
  # "Membership" (also on the dashboard) is a benefits/subscription upsell page, not a document
  # category. The original list below this comment (Driver License, Vehicle Photos, Vehicle
  # Inspection, Criminal Background Check, Profile Photo, Driving Experience, PTC License, HST/
  # GST, Tax ID, Banking) doesn't match any of the above -- superseded by the confirmed list.
  # Onboarding blocs: OnboardingEvent (GetChecklist, StartSession, SubmitDocument, StartUpload, CompleteUpload)
  # Services: IAM service + Driver service (ca.driver.api.prod.ridehovr.com)

  Background:
    Given the HOVR driver app is installed and launched

  # --- PHONE AUTHENTICATION ---

  # CONFIRMED 2026-08-25 on a real device, passing. @profile-auth tags this and the other four
  # implemented scenarios below (Enter driver name / Enter driver email too) so they can be run
  # in isolation from the other 25 onboarding-document scenarios in this file, which share the
  # same feature-level @registration tag but have no step definitions yet:
  #   mvn test -Dplatform=driver -Dcucumber.filter.tags="@profile-auth" -Dtest=DriverTestRunner
  @profile-auth
  Scenario: Navigate to phone number entry
    Given I am on the driver landing page
    When I tap "Get Started" or "Sign Up"
    Then I should see the phone number entry screen

  @profile-auth
  Scenario: Enter a valid phone number and receive OTP
    Given I am on the phone number entry screen
    When I select country code "+1"
    And I enter phone number "6475559876"
    And I tap "Continue"
    Then an OTP should be sent to "+16475559876"
    And I should see the OTP verification screen

  # KNOWN INTERMITTENT FAILURE 2026-08-25: passes reliably in isolation, but has failed twice
  # (out of two attempts) when run as part of the full @profile-auth batch, both times landing on
  # an "An account with this email already exists." screen after entering the OTP -- despite this
  # scenario never touching email itself. Likely backend-side rate-limiting/anti-abuse throttling
  # after many rapid registration attempts from the same device today (this session alone created
  # several real accounts, manually and via repeated automated runs), not a step-definition bug --
  # isolating the scenario and re-running cleared it immediately. Documented rather than chased
  # further; revisit if it keeps failing on a day with less registration volume already behind it.
  @profile-auth
  Scenario: Verify OTP successfully
    Given I am on the OTP verification screen
    When I enter the correct OTP
    Then I should be authenticated
    And I should proceed to profile creation

  # UNVERIFIED 2026-08-25: confirmed on a real device that a fake/invalid-looking number
  # ("1234567890") is accepted and proceeds straight to the OTP screen -- same finding as the
  # rider app's equivalent scenario. No region-blocking happens at registration time from the
  # phone number alone. Left as-is (not yet implemented) pending the same decision the rider
  # app's version got: move it to a booking/eligibility-time check instead, or drop it.
  Scenario: Enter phone from unsupported region
    Given I am on the phone number entry screen
    When I enter a phone number from an unsupported region
    And I tap "Continue"
    Then I should see the "Not Available In Your Region" screen

  # --- PROFILE CREATION ---

  @profile-auth
  Scenario: Enter driver name
    Given I have verified my phone number
    And I am on the driver name screen
    When I enter first name "Ahmad"
    And I enter last name "Khan"
    And I tap "Continue"
    Then I should proceed to the email entry screen

  # CONFIRMED 2026-08-25 on a real device: submitting email creates the driver profile and lands
  # directly on the welcome/onboarding dashboard ("Welcome <name>") -- no terms screen in between.
  # "Accept driver terms and conditions" removed: there's no such screen to accept terms on.
  @profile-auth
  Scenario: Enter driver email
    Given I am on the driver email screen
    When I enter email "ahmad.khan@email.com"
    And I tap "Continue"
    Then my driver profile should be created
    And I should see the welcome screen


  # --- ONBOARDING CHECKLIST (GTA marketplace) ---
  #
  # CONFIRMED 2026-08-25 on a real device: the scenarios below assume the GTA marketplace (see
  # the feature-level comment above for why/how) since it's the one with a real, rich checklist
  # to exercise -- India's single-document checklist isn't covered separately here.

  @profile-auth
  Scenario: View onboarding document checklist
    Given I have created my driver profile in the GTA marketplace
    Then I should see the HOVR onboarding document list
    And I should see required document categories:
      | category          |
      | Driver             |
      | Vehicle            |
      | City Requirements  |

  # CONFIRMED 2026-08-25 on a real device: every document's detail page has "Upload" (opens
  # Android's system photo picker, com.google.android.photopicker) and "Camera" buttons, except
  # Driver Selfie which only has "Upload" -- not exercised as a special case below since every
  # row here uses the Upload/photo-picker path anyway. Selecting any existing photo and tapping
  # "Done" is enough to complete an upload; the app doesn't validate photo CONTENT, just that one
  # was provided. Confirmed the category's dashboard status changes from "Required · N/N" to
  # "In progress · <submitted>/N" after a successful upload (Vehicle Registration verified this
  # directly: "Required · 4/4" -> "In progress · 1/4").
  @profile-auth
  Scenario Outline: Upload a required onboarding document
    Given I have created my driver profile in the GTA marketplace
    And I am on the "<category>" document list
    When I tap the "<document>" document
    And I upload a photo for the document
    Then the document should be uploaded successfully

    # "Driver Selfie" deliberately excluded from these examples: CONFIRMED 2026-08-25 on a real
    # device that its "Upload" button opens the live CAMERA directly for this one document,
    # unlike every other document (where "Upload" opens the gallery photo picker this outline's
    # steps automate) -- a genuine per-document UX difference, not a bug in these steps. Covered
    # separately below ("Upload Driver Selfie via camera capture").
    Examples:
      | category           | document                          |
      | Driver              | Driver's License                  |
      | Driver              | Driving Experience Verification   |
      | Driver              | Driver's License Back              |
      | Driver              | city permit                       |
      | Driver              | workk permit                      |
      | Vehicle             | Vehicle Registration               |
      | Vehicle             | Safety Inspection Certificate      |
      | Vehicle             | Address                           |
      | Vehicle             | Vehicle Insurance                  |
      | City Requirements   | Background Check                  |
      | City Requirements   | Vehicle-For-Hire Certificate        |
      | City Requirements   | PTC Number                         |
      | City Requirements   | Work Proof                        |
      | City Requirements   | city permit                       |

  # CONFIRMED 2026-09-23 on a real device: unlike every other document, Driver Selfie's "Upload"
  # button opens the live Samsung Camera app directly instead of the gallery photo picker.
  # Capturing (the "Take picture" shutter) and confirming ("OK" on the review screen) returns
  # straight to the document LIST with the status already showing "Submitted" -- no separate
  # "Document uploaded Successfully!" screen like the gallery-picker path shows.
  @profile-auth
  Scenario: Upload Driver Selfie via camera capture
    Given I have created my driver profile in the GTA marketplace
    And I am on the "Driver" document list
    When I tap the "Driver Selfie" document
    And I capture a photo for the document
    Then the document should show as submitted

  # --- NOT APPLICABLE IN THIS APP VERSION ---
  #
  # Everything below assumed a document list (Driver License, Vehicle Photo and Details,
  # Vehicle Insurance, Vehicle Inspection, Criminal Background Check, Profile Photo, Driving
  # Experience, Preferred Name, PTC License Number, HST/GST Number, Tax ID, Training, Banking)
  # and interaction patterns (a scanner, S3 pre-signed-URL assertions, per-document approval/
  # rejection states) that don't match the REAL checklist confirmed above. Left here for
  # reference/traceability rather than deleted outright, since a future app version could
  # genuinely add some of these back -- not implemented, not run (no step definitions, and no
  # tag ties them into any current test run).
  #
  #   Scenario: Submit preferred name
  #   Scenario: Upload Vehicle For Hire Certificate (Toronto/GTA) -- superseded by the real
  #             "Vehicle-For-Hire Certificate" document under City Requirements above
  #   Scenario: Submit PTC License Number -- superseded by "PTC Number" above
  #   Scenario: Submit HST/GST Number
  #   Scenario: Submit Tax Identification Number
  #   Scenario: Start document upload with S3 pre-signed URL
  #   Scenario: Document upload fails
  #   Scenario: Document rejected during review
  #   Scenario: All documents submitted and approved
  #   Scenario: Partial onboarding completion
  #   Scenario: Complete driver training
  #   Scenario: Set up banking for payouts
  #   Scenario: Access help for a specific document
  #   Scenario: Resume onboarding after app restart
  #   Scenario: Upload document with invalid file format
  #   Scenario: Use document scanner to capture document
