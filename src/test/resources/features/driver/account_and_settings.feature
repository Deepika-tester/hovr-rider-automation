@account
Feature: Account, profile and app settings
  As a driver
  I want to manage my profile, vehicle, banking and app settings
  So that my account information is correct

  Background:
    Given the driver is logged in
    And the driver opens "Account" from the drawer

  @smoke
  Scenario: Account menu items are shown
    Then the following items are shown:
      | item             |
      | My Vehicle       |
      | Alerts           |
      | Banking          |
      | Documents        |
      | App settings     |
      | Update the app   |

  @profile
  Scenario: Driver views the profile
    When the driver opens the profile
    Then the text "Profile" is shown
    And "Rides", "Rating" and "Driver ID" are shown
    And "Legal Name", "Phone" and "Email" are shown

  @profile
  Scenario: New driver has no rating yet
    Given the driver has completed no rides
    When the driver opens the profile
    Then the text "No rating yet" is shown

  # BLOCKED: would change or consume data on the shared approved test driver in a way that cannot be undone from here; not run without approval.
  @profile @blocked
  Scenario: Driver updates the preferred name
    When the driver opens the profile
    And the driver edits "Preferred name" to "AJ"
    And the driver taps "Update"
    Then the text "Updated successfully!" is shown
    And the preferred name is "AJ"

  # BLOCKED: needs the backend to answer with an error (fault injection), which is not possible against the shared dev backend.
  @profile @negative @blocked
  Scenario: Profile update fails
    Given the update profile request returns an error
    When the driver updates the profile
    Then the text "Something went wrong!" is shown

  # BLOCKED: would change or consume data on the shared approved test driver in a way that cannot be undone from here; not run without approval.
  @profile @blocked
  Scenario: Driver changes the profile photo
    When the driver opens the profile
    And the driver picks a new profile photo
    Then the text "Uploading Image!" is shown
    And the new photo is shown

  @vehicle
  Scenario: Driver views vehicle details
    Given the driver has a registered vehicle
    When the driver taps "My Vehicle"
    Then "Make", "Model", "Year", "Type of Body", "Plate" and "Colour" are shown

  # BLOCKED: needs data the test accounts do not have and the test tools cannot create (referred riders, alerts, payouts, bookings, document states, ...).
  @vehicle @blocked
  Scenario: Driver without a vehicle
    Given the driver has no vehicle
    When the driver taps "My Vehicle"
    Then the text "No vehicle available!" is shown

  @banking
  Scenario: Driver with an active bank account
    Given the driver's banking account is active
    When the driver taps "Banking"
    Then the text "Banking Details" is shown
    And the text "Account is Active" is shown
    And the "Capabilities" section is shown
    And the "Manage Bank Account" button is shown

  @language
  Scenario Outline: Driver changes the app language
    When the driver taps "App settings"
    And the driver taps "Language"
    And the driver selects "<language>"
    Then the text "Language updated to <language>" is shown
    And the app is displayed in "<language>"

    Examples:
      | language        |
      | English (US)    |
      | French (Canada) |
      | Spanish         |
      | Turkish         |
      | Hindi           |
      | Punjabi         |
      | Arabic          |

  @language @rtl
  Scenario: Arabic switches the layout to right-to-left
    When the driver changes the language to "Arabic"
    Then the app layout is right-to-left

  @ota
  Scenario: App is up to date
    Given there is no new over-the-air update
    When the driver taps "Update the app"
    And the driver taps "Check for update"
    Then the text "You're on the latest update." is shown
    And "App version", "Build" and "Patch" are shown

  # BLOCKED: needs an over-the-air update to be published or withheld for this build.
  @ota @blocked
  Scenario: Over-the-air update is downloaded
    Given a new over-the-air update is available
    When the driver taps "Update the app"
    And the driver taps "Check for update"
    Then the text "Update downloaded. Restart the app to apply it." is shown
    When the driver taps "Restart now"
    Then the dialog "Update ready" is shown

  # BLOCKED: needs an over-the-air update to be published or withheld for this build.
  @ota @blocked
  Scenario: OTA update prompt while not on a trip
    Given a new over-the-air update was downloaded
    And the driver is not on a trip
    When the driver opens the app
    Then the text "A fix is ready. Restart the app to apply it when you are not on a trip." is shown
    When the driver taps "Later"
    Then the prompt is closed

  # BLOCKED: needs an over-the-air update to be published or withheld for this build.
  @ota @blocked
  Scenario: OTA is not available on this build
    Given the build does not support over-the-air updates
    When the driver taps "Update the app"
    Then the text "Not available on this build" is shown
