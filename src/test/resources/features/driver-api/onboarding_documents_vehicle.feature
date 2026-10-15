@api @onboarding
Feature: API - onboarding, documents and vehicles
  driver.v1.driver.DriverService (driver_session_repository, driver_onboarding_feature_bloc),
  driver.v1.onboarding.OnboardingService and driver.v1.document.DcoumentService
  (driver_onboarding_feature_bloc, documents_feature_bloc, neighbour_eligibility_cubit),
  driver.v1.vehicle.VehicleService (driver_profile_bloc, my_vehicle_details).

  Background:
    Given the API base URL for the "dev" environment
    And an authenticated driver session

  @smoke
  Scenario: GetDriver returns the onboarding driver record
    When I call "driver.v1.driver.DriverService/GetDriver" with:
      | field     | value       |
      | driver_id | <driver id> |
    Then the response code is "ok"

  Scenario: GetDriverStatus returns status for the marketplace
    When I call "driver.v1.driver.DriverService/GetDriverStatus" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
    Then the response code is "ok"
    And the response contains the driver approval status

  @banking
  Scenario: GetDriverBankingStatus and GetBankingOnboardingUrl
    When I call "driver.v1.driver.DriverService/GetDriverBankingStatus" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
    Then the response code is "ok"
    When I call "driver.v1.driver.DriverService/GetBankingOnboardingUrl" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
    Then the response code is "ok"
    And the response contains an https URL

  # BLOCKED: would change the shared approved test driver's account in a way that cannot be undone from here (email, vehicle, onboarding, documents); not run without approval.
  @session @blocked
  Scenario: StartOnboarding creates a session that is listed
    Given the driver has no onboarding session for "<marketplace id>"
    When I call "driver.v1.onboarding.OnboardingService/StartOnboarding" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
    Then the response code is "ok"
    When I call "driver.v1.onboarding.OnboardingService/ListOnboardingSessions" with:
      | field     | value       |
      | driver_id | <driver id> |
      | page_size | 20          |
    Then the new session is listed

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @session @blocked
  Scenario: GetOnboardingChecklist returns the steps
    Given an onboarding session "<session id>"
    When I call "driver.v1.onboarding.OnboardingService/GetOnboardingChecklist" with:
      | field      | value        |
      | session_id | <session id> |
    Then the response code is "ok"
    And the checklist contains Driver, Vehicle, City Requirements, Membership, Banking and Training

  # BLOCKED: would change the shared approved test driver's account in a way that cannot be undone from here (email, vehicle, onboarding, documents); not run without approval.
  @upload @smoke @blocked
  Scenario: Document upload sequence
    Given an onboarding session "<session id>" with requirement "<requirement id>"
    When I call "driver.v1.document.DcoumentService/StartDocumentUpload" with:
      | field          | value             |
      | driver_id      | <driver id>       |
      | requirement_id | <requirement id>  |
      | marketplace_id | <marketplace id>  |
      | content_type   | image/jpeg        |
      | file_name      | licence.jpg       |
      | size_bytes     | 204800            |
    Then the response code is "ok"
    And the response contains an upload id and an upload URL
    When I upload the file to the upload URL
    And I call "driver.v1.document.DcoumentService/CompleteDocumentUpload" with:
      | field     | value       |
      | upload_id | <upload id> |
    Then the response code is "ok"
    When I call "driver.v1.onboarding.OnboardingService/SubmitRequirementDocument" with:
      | field          | value            |
      | session_id     | <session id>     |
      | requirement_id | <requirement id> |
      | storage_key    | <storage key>    |
    Then the response code is "ok"
    And the requirement status becomes "submitted"

  # BLOCKED: needs data the test driver does not have and the tests cannot create (another driver's records, unread alerts, payouts, bookings, ...).
  @upload @negative @blocked
  Scenario Outline: StartDocumentUpload rejects bad files
    When I call "driver.v1.document.DcoumentService/StartDocumentUpload" with content type "<type>" and size <size>
    Then the response code is "invalid_argument"

    Examples:
      | type            | size      |
      | application/zip | 204800    |
      | image/jpeg      | 0         |
      | image/jpeg      | 104857600 |

  @upload @negative
  Scenario: CompleteDocumentUpload with an unknown upload id
    When I call "driver.v1.document.DcoumentService/CompleteDocumentUpload" with:
      | field     | value   |
      | upload_id | unknown |
    Then the response code is "not_found"

  @documents
  Scenario: ListRequirementStatuses returns document statuses
    When I call "driver.v1.document.DcoumentService/ListRequirementStatuses" with:
      | field          | value            |
      | driver_id      | <driver id>      |
      | marketplace_id | <marketplace id> |
    Then the response code is "ok"
    And each requirement has a status

  @vehicle
  Scenario: ListVehicles returns the driver's vehicles
    When I call "driver.v1.vehicle.VehicleService/ListVehicles" with:
      | field     | value       |
      | driver_id | <driver id> |
      | page_size | 20          |
    Then the response code is "ok"

  # BLOCKED: would change the shared approved test driver's account in a way that cannot be undone from here (email, vehicle, onboarding, documents); not run without approval.
  @vehicle @blocked
  Scenario: DeleteVehicle removes a vehicle
    Given the driver has a vehicle "<vehicle id>"
    When I call "driver.v1.vehicle.VehicleService/DeleteVehicle" with:
      | field      | value        |
      | vehicle_id | <vehicle id> |
    Then the response code is "ok"
    And "ListVehicles" no longer returns "<vehicle id>"
