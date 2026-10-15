@help
Feature: Help and support
  As a driver
  I want to find answers and contact support
  So that I can solve problems quickly

  Background:
    Given the driver is logged in
    And the driver opens "Help"

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @smoke @blocked
  Scenario: Help screen sections
    Then the text "Help" is shown
    And the sections are shown:
      | section       |
      | Support cases |
      | FAQ           |
      | Other         |

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario Outline: Driver opens an FAQ category
    When the driver taps "<category>"
    Then the questions for "<category>" are listed

    Examples:
      | category             |
      | App and features     |
      | Account and data     |
      | Payments and pricing |
      | Using HOVR           |

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario: Driver searches help
    When the driver searches "cancel" in "Search Help"
    Then matching help articles are listed

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario: Empty support inbox
    Given the driver has no open support conversations
    When the driver taps "Inbox"
    Then the text "No open conversations with HOVR Support" is shown

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario: Driver opens support tickets
    When the driver taps "Get in touch with us"
    Then the text "Support tickets" is shown

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario: About HOVR shows the app version
    When the driver taps "About HOVR"
    Then the text "About HOVR" is shown
    And the app version is shown

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support are not tested.
  @blocked
  Scenario Outline: Legal pages
    When the driver taps "<page>"
    Then the "<page>" page is opened

    Examples:
      | page             |
      | Terms of Service |
      | Privacy Policy   |
