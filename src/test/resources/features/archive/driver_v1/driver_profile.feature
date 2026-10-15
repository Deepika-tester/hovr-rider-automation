@driver @profile @driver-account
Feature: Driver Dashboard, Profile and Account Area
  As a HOVR driver who has signed up
  I want to see what I still need to do and manage my profile
  So that I can finish onboarding and start driving

  # Explored on a real build (driver dev v6.2.4+592, emulator, India marketplace) on 2026-10-14, signed in
  # as the existing test driver (+91 7428730894, staging OTP 111111). This replaces the earlier
  # scenarios that were derived from source code and assumed an APPROVED driver (drawer screens, banking,
  # app settings, language) -- none of those exist on a driver who is still "Pending".
  #
  # What a freshly signed-up (Pending) driver sees:
  #  - Dashboard: header "SIGNING UP FOR INDIA", "Welcome <first name>", "Here's what you need to do to set
  #    up your account.", tabs Rideshare / Co-Commute, cards "Vehicle" (Required 1/1) and "Membership"
  #    (Required 1/1), plus "Referral Program" and "Member Benefits" which stay locked behind a
  #    "Verify now" ID-verification button.
  #  - Menu (top right): Help, Profile, Logout.
  #  - Profile: status Pending, 0 Rides, 5.00 Rating, Account Info (Driver ID, Legal Name, Preferred Name,
  #    Phone, Email).
  #  - Vehicle > "Vehicle Documents" lists "Vehicle Registration | Required"; its page offers Upload and
  #    Camera. (India's checklist is one document; the GTA marketplace shows the 15-document checklist
  #    covered in driver_registration.feature.)
  #  - Co-Commute tab is gated: "Driver is not registered in this marketplace" and a "driver pic | Missing"
  #    requirement that opens a photo upload page.
  #  - Help opens "Support tickets" (create / FAQ).
  #
  # NOT automated: actually uploading documents on the emulator (needs the system photo picker / camera),
  # paying for the membership (the dev payment web page answers "Payment server is down"), ID
  # verification, going online, ride requests and trips -- they all need an APPROVED driver, which this
  # environment cannot produce (see the @needs-approved-driver features).

  Background:
    Given I am signed in as the test driver

  # --- DASHBOARD ---

  @positive
  Scenario: Dashboard shows the account set-up checklist for a pending driver
    Then I should see all of these driver texts:
      | SIGNING UP FOR      |
      | INDIA               |
      | Welcome             |
      | set up your account |
      | Vehicle             |
      | Membership          |

  @positive
  Scenario: Referral Program and Member Benefits are locked until the ID is verified
    Then I should see all of these driver texts:
      | Referral Program |
      | Member Benefits  |
      | Verify now       |

  # --- VEHICLE DOCUMENTS ---

  @positive
  Scenario: Vehicle card lists the required vehicle document
    When I open the "Vehicle" card
    Then I should see the driver text "Vehicle Documents"
    And I should see the driver text "Vehicle Registration"
    And I should see the driver text "Required"

  @positive
  Scenario: A vehicle document page offers Upload and Camera
    When I open the "Vehicle" card
    And I open the "Vehicle Registration" card
    Then I should see all of these driver texts:
      | Upload |
      | Camera |

  # --- MENU AND PROFILE ---

  @positive
  Scenario: Menu lists Help, Profile and Logout
    When I open the driver menu
    Then I should see all of these driver texts:
      | Help    |
      | Profile |
      | Logout  |

  @positive
  Scenario: Profile shows the driver's account info
    When I open the driver menu
    And I open the "Profile" card
    Then I should see all of these driver texts:
      | Account Info   |
      | Driver ID      |
      | Legal Name     |
      | Preferred Name |
      | Phone          |
      | Email          |
      | Rides          |
      | Rating         |
      | Pending        |

  @positive
  Scenario: Help opens the support tickets page
    When I open the driver menu
    And I open the "Help" card
    Then I should see the driver text "Support tickets"

  # --- CO-COMMUTE (gated for this driver) ---

  @negative
  Scenario: Co-Commute is not available until the driver is registered in the marketplace
    When I switch to the "Co-Commute" dashboard tab
    Then I should see all of these driver texts:
      | Share your daily commute          |
      | Action required                   |
      | not registered in this marketplace |
      | driver pic                        |
      | Missing                           |

  @negative
  Scenario: The missing driver picture requirement opens a photo upload page
    When I switch to the "Co-Commute" dashboard tab
    And I open the "driver pic" card
    Then I should see all of these driver texts:
      | Upload |
      | Camera |
