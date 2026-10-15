# HOVR rider automation

Cucumber + TestNG automation for the HOVR apps, in three suites that share one set of feature files per suite:

| Suite | Feature files | Glue (step definitions) | Runner | Needs |
|---|---|---|---|---|
| Rider app (UI) | `src/test/resources/features/rider/01..11_*.feature` | `com.automation.spec.rider` | `TestRunner` | Appium + Android emulator/device |
| Rider API (ConnectRPC) | `src/test/resources/features/api/01..09_*.feature` | `com.automation.spec.api` | `ApiTests` | network only |
| Driver app (UI) | `src/test/resources/features/driver/*.feature` | `com.automation.driver.stepdefinitions` | `DriverTestRunner` | Appium + emulator/device |

The rider UI and API feature files are the specification supplied by the product team (`docs/HOVR_Rider_Cucumber_Features.pdf`,
`docs/HOVR_Rider_API_Cucumber_Features.pdf`). They are run **as written** against the real build; where the app behaves differently the
scenario FAILS and the report shows expected vs actual, so a failure is usually a spec-vs-app decision, not a broken test.
Older, hand-verified feature files and the first-generation steps are kept in `src/test/resources/features/archive/`
and `com.automation.stepdefinitions` (the spec glue still reuses their registration helpers).

## Scenarios that are not run (`@blocked`)

A scenario tagged `@blocked` is skipped by every default run and carries a `# BLOCKED:` comment with the reason. The report lists them
separately. Reasons in use:

* needs a driver (the dev environment can only produce a *Pending* driver), another rider, or fault injection;
* would create a real ride request, booking, held payment or ticket on the shared dev backend (**ride-confirming scenarios are held until approved**);
* irreversible (account deletion);
* **Help / support tickets are never tested** (project owner's instruction), nor is anything that creates tickets.

Test data rules: India marketplace, `+91` numbers with the staging OTP `111111` (it is rejected for other countries), local Mohali
addresses, and only the first ride option (HOVR).

## Running

Prerequisites: JDK 21, Maven, Node + Appium 3 (UiAutomator2), Android SDK. Keep the Mac awake (`caffeinate -dimsu`).

```bash
scripts/start-emulator.sh                 # boots the HOVR_Test AVD and installs the rider + driver dev builds
caffeinate -dimsu appium &                # Appium on :4723

# rider UI (everything not @blocked)
mvn test -Dplatform=emulator -Dtest=TestRunner
# a slice:
mvn test -Dplatform=emulator -Dtest=TestRunner -Dcucumber.filter.tags="@payment and not @blocked"

# rider API (about a minute, no emulator)
mvn test -Dtest=ApiTests

# driver app on the emulator
mvn test -Dplatform=driver-emulator -Dtest=DriverTestRunner -Dcucumber.filter.tags="@driver-account"
```

`-Dplatform=` selects `src/test/resources/config/<platform>.properties` (`android` real device, `emulator`, `driver`, `driver-emulator`, `ios`).

### Report

```bash
# copy each run's Cucumber JSON next to the others (cp -p keeps the run time: the newest run of a scenario wins)
cp -p target/cucumber-reports/cucumber.json target/cucumber-reports/rider/<name>.json   # UI runs
cp -p target/cucumber-reports/api/cucumber.json target/cucumber-reports/api/<name>.json # API runs (ApiTests writes api/cucumber.json itself)
mvn -q test-compile exec:java@generate-report
open target/test-report/report.pdf        # also index.html
```

Statuses: **PASSED**, **FAILED** (app differs from the feature file), **N/A** (does not apply on Android), **BLOCKED** (not run, reason given).

## How the UI suite is built

* `SpecSession` gets a signed-in rider onto a screen with the bottom tabs (registering a fresh `+91` rider only when logged out) and walks the
  Account-area routes. Riders are reused between scenarios; `@auth` / `@fresh-install` / `@language` scenarios wipe the app (`SpecHooks`,
  before the Appium session starts, because wiping mid-session kills UiAutomator2).
* `SpecUi` matches on text, content-desc **or hint**, tolerant of curly/straight apostrophes; `SpecAliases` maps the handful of spec labels that
  differ from the real control ("Set as primary" → "Make Default", "Add card" → "CONFIRM PREAUTHORIZATION", ...). Every alias is a spec wording
  difference reported as such, never a way to hide a behaviour difference.
* Failures that are clearly infrastructure (UiAutomator2 crash, session start) are retried up to twice (`InfraRetryAnalyzer`); assertion failures never are.
* The Adyen card dialog is a secure window: its fields are reachable by Appium but it cannot be screenshotted.

## How the API suite is built

`ConnectClient` posts JSON to the Connect endpoints the app uses (`iam.api.dev…`, `ca.ride.api.dev…`, `ca.notification.api.dev…`), mapping HTTP/Connect
errors to gRPC status names. `RiderSession` signs a throw-away rider up exactly like the app (OTP challenge → verify with `111111` + location →
create profile → refresh), signing the token-bound calls with a DPoP proof (`Dpop`). Request shapes that differ from what the feature files imply
(`phoneNumber.e164`, `latLng`, flat `UpdateSavedPlace`, `position`, `scheduledPickupTime`, `fcmToken`, ...) are documented next to the steps.
