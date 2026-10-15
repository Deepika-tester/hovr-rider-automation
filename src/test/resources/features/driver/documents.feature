@documents
Feature: Driver documents
  As an approved driver
  I want to view and update my documents
  So that my account stays compliant

  Background:
    Given the driver is logged in
    And the driver opens "Account"
    And the driver taps "Documents"

  @smoke
  Scenario: All documents approved
    Given all driver documents are approved
    Then the text "Documents" is shown
    And the text "All of your documents have been successfully uploaded and approved. Drive safe!" is shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @blocked
  Scenario: No documents found
    Given the driver has no documents
    Then the text "No document found!" is shown

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @negative @blocked
  Scenario: Documents fail to load
    Given the documents request returns an error
    Then the text "Something went wrong!" is shown

  Scenario: Driver views the documents for a scope
    Given the driver has vehicle documents
    When the driver opens the "Vehicle" document scope
    Then the vehicle documents are listed with their status

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @blocked
  Scenario: Driver re-uploads an expiring document
    Given a document with status "Attention Required"
    When the driver opens the document
    And the driver uploads a new photo
    Then the document status becomes "Submitted"

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @negative @blocked
  Scenario: Download link cannot be opened
    Given the device has no app that can open the download link
    When the driver opens a document download link
    Then the text "No app found to open download link." is shown
