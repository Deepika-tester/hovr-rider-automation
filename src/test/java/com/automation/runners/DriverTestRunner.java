package com.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Entry point for the driver-app Appium mobile suite. Scoped to features/driver
 * and its own glue package (com.automation.driver.stepdefinitions) — deliberately
 * NOT sharing com.automation.stepdefinitions with TestRunner (rider), since
 * several driver scenarios use identical Gherkin phrasing to rider ones but need
 * different locators/assertions; loading both in one glue scope would collide
 * with "Ambiguous step definitions" errors. See DriverCommonSteps's javadoc.
 *
 * Run with: mvn test -Dplatform=driver -Dcucumber.filter.tags="@profile-auth" -Dtest=DriverTestRunner
 * (the -Dplatform=driver is required — it's what makes ConfigReader load
 * driver.properties instead of config.properties; see DriverManager/ConfigReader.)
 *
 * Only the auth/profile-creation scenarios in driver_registration.feature have
 * step definitions so far (see DriverRegistrationSteps's javadoc), tagged
 * @profile-auth individually for exactly this reason: driver_registration.feature
 * carries @registration at the FEATURE level, so every one of its 30 scenarios
 * (including the 25 onboarding-document ones with no glue yet) matches that tag
 * too — @profile-auth is scenario-level and only on the five that actually have
 * step definitions, confirmed 2026-08-25: filtering by it is what keeps a run
 * from drowning in UndefinedStepException noise from the rest of the file.
 */
@CucumberOptions(
        features = "src/test/resources/features/driver",
        glue = {"com.automation.driver.stepdefinitions", "com.automation.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/driver/cucumber.html",
                "json:target/cucumber-reports/driver/cucumber.json"
        },
        monochrome = true,
        tags = "@profile-auth"
)
public class DriverTestRunner extends AbstractTestNGCucumberTests {
}
