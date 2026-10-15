@location @permissions
Feature: Location and device permissions
  As a driver
  I want the app to ask for the permissions it needs
  So that ride matching and navigation work

  Background:
    Given the driver is logged in

  @smoke
  Scenario: Location services are off
    Given location services are turned off on the device
    When the driver opens the home screen
    Then the text "Turn On Location Services to Allow \"Hovr Driver\" to Determine Your Location" is shown
    When the driver taps "Settings"
    Then the device location settings are opened

  Scenario: Location permission is granted
    Given location permission has not been asked
    When the driver opens the home screen
    And the driver allows location access
    Then the driver's current location is shown on the map

  @negative
  Scenario: Location permission is denied
    Given location permission is denied
    When the driver taps "Go Online"
    Then the driver is asked to enable location
    And the driver stays offline

  Scenario: Notification permission prompt
    Given notification permission has not been asked
    When the driver opens the app
    Then the notification permission prompt is shown

  Scenario: Camera permission for document upload
    Given camera permission has not been asked
    When the driver uploads a document with the camera
    Then the camera permission prompt is shown
