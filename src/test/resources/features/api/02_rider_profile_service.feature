@api @profile
Feature: RiderProfileService API
  Profile, preferences and saved places.
  Client: RiderProfileServiceClient (home_view_remote_data_source.dart)

  Background:
    Given I am authenticated as a rider with id "<riderId>"

  @smoke
  Scenario: Get rider profile
    When I call "RiderProfileService/GetRiderProfile" with riderId "<riderId>"
    Then the response status is "OK"
    And the profile contains first name, last name, phone and email

  Scenario: Get another rider's profile is forbidden
    When I call "RiderProfileService/GetRiderProfile" with another rider's id
    Then the response status is "PERMISSION_DENIED"

  Scenario: Update rider profile
    When I call "RiderProfileService/UpdateRiderProfile" with:
      | firstName     | Jane             |
      | lastName      | Doe              |
      | preferredName | JD               |
      | email         | jane@example.com |
    Then the response status is "OK"
    And GetRiderProfile returns the updated values

  Scenario Outline: Update profile validation
    When I call "RiderProfileService/UpdateRiderProfile" with <field> "<value>"
    Then the response status is "INVALID_ARGUMENT"

    Examples:
      | field     | value         |
      | email     | not-an-email  |
      | firstName |               |

  Scenario: Update rider preferences
    When I call "RiderProfileService/UpdateRiderPrefs" with language "es"
    Then the response status is "OK"

  Scenario: Update safety preferences
    When I call "RiderProfileService/UpdateRiderSafetyPrefs" with:
      | wheelchairAssistance | true |
      | verifyRidePin        | true |
    Then the response status is "OK"

  @smoke
  Scenario: Saved place lifecycle
    When I call "RiderProfileService/CreateSavedPlace" with name "Gym" and a valid address with lat/lng
    Then the response status is "OK"
    And the response contains a saved place id
    When I call "RiderProfileService/GetSavedPlace" with that id
    Then the place name is "Gym"
    When I call "RiderProfileService/UpdateSavedPlace" with name "Gym 2"
    Then ListSavedPlaces contains "Gym 2"
    When I call "RiderProfileService/DeleteSavedPlace" with that id
    Then ListSavedPlaces does not contain "Gym 2"

  Scenario: Create saved place without coordinates
    When I call "RiderProfileService/CreateSavedPlace" with name "Gym" and no lat/lng
    Then the response status is "INVALID_ARGUMENT"

  Scenario: Get a non-existent saved place
    When I call "RiderProfileService/GetSavedPlace" with id "does-not-exist"
    Then the response status is "NOT_FOUND"

  Scenario: Payment methods list
    When I call "RiderProfileService/ListPaymentMethods" with riderId "<riderId>"
    Then the response status is "OK"
    And at most one method is marked default
