@driver @registration @auth
Feature: Driver Registration and Onboarding
  As a new driver
  I want to create a driver account and complete onboarding
  So that I can start driving with HOVR and earning

  # Derived from: hovr-driver-app/lib/features/authentication-feature/ and driver-onboarding-feature/
  # Auth flow: Landing -> PhoneNumber -> OTP -> Name -> Email -> Terms -> Welcome -> Onboarding Checklist
  # Onboarding docs: Driver License, Vehicle Photos, Vehicle Insurance, Vehicle Inspection,
  #                   Criminal Background Check, Vehicle For Hire Certificate, PTC License,
  #                   Profile Photo, Driving Experience, HST/GST, Tax ID, Banking
  # Onboarding blocs: OnboardingEvent (GetChecklist, StartSession, SubmitDocument, StartUpload, CompleteUpload)
  # Services: IAM service + Driver service (ca.driver.api.prod.ridehovr.com)

  Background:
    Given the HOVR driver app is installed and launched

  # --- PHONE AUTHENTICATION ---

  Scenario: Navigate to phone number entry
    Given I am on the driver landing page
    When I tap "Get Started" or "Sign Up"
    Then I should see the phone number entry screen

  Scenario: Enter a valid phone number and receive OTP
    Given I am on the phone number entry screen
    When I select country code "+1"
    And I enter phone number "6475559876"
    And I tap "Continue"
    Then an OTP should be sent to "+16475559876"
    And I should see the OTP verification screen

  Scenario: Verify OTP successfully
    Given I am on the OTP verification screen
    When I enter the correct OTP
    Then I should be authenticated
    And I should proceed to profile creation

  Scenario: Enter phone from unsupported region
    Given I am on the phone number entry screen
    When I enter a phone number from an unsupported region
    And I tap "Continue"
    Then I should see the "Not Available In Your Region" screen

  # --- PROFILE CREATION ---

  Scenario: Enter driver name
    Given I have verified my phone number
    And I am on the driver name screen
    When I enter first name "Ahmad"
    And I enter last name "Khan"
    And I tap "Continue"
    Then I should proceed to the email entry screen

  Scenario: Enter driver email
    Given I am on the driver email screen
    When I enter email "ahmad.khan@email.com"
    And I tap "Continue"
    Then I should proceed to the terms screen

  Scenario: Accept driver terms and conditions
    Given I am on the driver terms and policy screen
    When I review and accept the terms
    Then my driver profile should be created
    And I should see the welcome screen

  # --- ONBOARDING CHECKLIST ---

  Scenario: View onboarding document checklist
    Given I have created my driver profile
    When I navigate to the onboarding page
    Then I should see the HOVR onboarding document list
    And I should see required documents:
      | document                        | status   |
      | Driver License                  | Required |
      | Vehicle Photo and Details       | Required |
      | Vehicle Insurance               | Required |
      | Vehicle Inspection              | Required |
      | Criminal Background Check       | Required |
      | Profile Photo                   | Required |
      | Driving Experience              | Required |
    And I should see marketplace-specific documents based on my city

  Scenario: Upload driver license
    Given I am on the onboarding checklist
    When I tap "Driver License"
    Then I should see the driver license info page
    When I scan or upload my driver license photo
    And I confirm the license details
    And I submit the document
    Then a SubmitRequiredDocumentRequest should be sent
    And the driver license status should change to "Submitted"

  Scenario: Upload vehicle photo and details
    Given I am on the onboarding checklist
    When I tap "Vehicle Photo and Details"
    Then I should see the vehicle photo and details page
    When I upload vehicle photos (front, back, side)
    And I enter vehicle make, model, year, and color
    And I enter the license plate number
    And I submit
    Then the vehicle details should be saved

  Scenario: Upload vehicle insurance
    Given I am on the onboarding checklist
    When I tap "Vehicle Insurance"
    Then I should see the vehicle insurance page
    When I upload my insurance document
    And I submit it
    Then the insurance document status should change to "Submitted"

  Scenario: Upload vehicle inspection report
    Given I am on the onboarding checklist
    When I tap "Vehicle Inspection"
    Then I should see the vehicle inspection page
    When I upload a valid vehicle inspection report
    And I submit it
    Then the document should be submitted for verification

  Scenario: Submit criminal background check
    Given I am on the onboarding checklist
    When I tap "Criminal Background Check"
    Then I should see the background check page with instructions
    When I complete and upload the background check
    And I submit it
    Then the status should change to "Pending Review"

  Scenario: Upload profile photo
    Given I am on the onboarding checklist
    When I tap "Profile Photo"
    Then I should see the profile photo page
    When I take or upload a clear face photo
    And I submit it
    Then the profile photo should be saved

  Scenario: Submit driving experience
    Given I am on the onboarding checklist
    When I tap "Driving Experience"
    Then I should see the driving experience page
    When I enter my years of driving experience
    And I submit
    Then the driving experience should be recorded

  Scenario: Submit preferred name
    Given I am on the onboarding checklist
    When I tap "Preferred Name"
    And I enter "Ahmad" as my preferred display name
    And I submit
    Then my preferred name should be saved

  # --- CITY-SPECIFIC REQUIREMENTS ---

  Scenario: Upload Vehicle For Hire Certificate (Toronto/GTA)
    Given I am onboarding in the GTA marketplace
    When I tap "Vehicle For Hire Certificate"
    Then I should see the certificate upload page
    When I upload my Vehicle For Hire Certificate
    And I submit it
    Then the document should be submitted for verification

  Scenario: Submit PTC License Number
    Given I am onboarding in a marketplace requiring a PTC license
    When I tap "PTC License Number"
    And I enter my PTC license number
    And I submit
    Then the PTC number should be saved

  Scenario: Submit HST/GST Number
    Given I am on the onboarding checklist
    When I tap "HST/GST Number"
    And I enter my tax number
    And I submit
    Then the HST/GST number should be recorded

  Scenario: Submit Tax Identification Number
    Given I am on the onboarding checklist
    When I tap "Tax Identification Number"
    And I enter my SIN or tax ID
    And I submit
    Then the tax ID should be securely stored

  # --- DOCUMENT UPLOAD FLOW ---

  Scenario: Start document upload with S3 pre-signed URL
    Given I am uploading a document
    When a StartDocumentUploadRequest is sent
    Then I should receive a pre-signed S3 upload URL
    And the document should be uploaded to S3
    And a CompleteDocumentUploadRequest should finalize the upload

  Scenario: Document upload fails
    Given I am uploading a document
    When the upload to S3 fails
    Then I should see an error message from UploadDocumentToS3State
    And I should be able to retry the upload

  Scenario: Document rejected during review
    Given I have submitted my driver license
    When the document is reviewed and rejected
    Then the document status should change to "Rejected"
    And I should see the rejection reason
    And I should be able to re-upload

  # --- ONBOARDING COMPLETION ---

  Scenario: All documents submitted and approved
    Given I have submitted all required documents
    And all documents have been approved
    Then my driver account status should change to "Approved"
    And I should be eligible to go online and accept rides
    And I should see the membership page

  Scenario: Partial onboarding completion
    Given I have submitted 4 of 7 required documents
    When I view the onboarding checklist
    Then I should see which documents are completed
    And I should see which documents are still required
    And I should not be able to go online until all are complete

  # --- TRAINING ---

  Scenario: Complete driver training
    Given I am on the onboarding page
    When I tap "Training"
    Then I should see the training document page
    When I complete the required training material
    Then training should be marked as complete

  # --- BANKING ---

  Scenario: Set up banking for payouts
    Given I am on the onboarding page
    When I tap "Banking Requirements"
    Then I should see the banking document page
    When I provide my banking details for direct deposit
    And I submit
    Then my banking information should be saved for payouts

  # --- HELP DURING ONBOARDING ---

  Scenario: Access help for a specific document
    Given I am on the driver license info page
    When I tap the help button
    Then I should see help content specific to driver license requirements
    And I should see instructions on what to upload

  # --- EDGE CASES ---

  Scenario: Resume onboarding after app restart
    Given I have partially completed onboarding
    When I relaunch the app
    Then I should be taken directly to the onboarding checklist
    And my previously submitted documents should show as submitted

  Scenario: Upload document with invalid file format
    Given I am uploading a vehicle insurance document
    When I select a file in an unsupported format
    Then I should see an error about the file format
    And I should be prompted to use a supported format (PDF, JPG, PNG)

  Scenario: Use document scanner to capture document
    Given I am on a document upload page
    When I tap "Scan Document"
    Then the document scanner should open
    When I capture a clear photo of the document
    Then the scanned image should be ready for upload
