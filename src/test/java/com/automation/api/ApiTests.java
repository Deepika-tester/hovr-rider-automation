package com.automation.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Entry point for the backend/API suite (features/api: the rider app's ConnectRPC services).
 * Deliberately separate from com.automation.runners.TestRunner (the Appium mobile
 * suite) — different glue package, different Hooks (no driver lifecycle), and its
 * own report output directory.
 *
 * Named "ApiTests" (not "ApiTestRunner") so Maven Surefire's default class
 * discovery (`**&#47;*Tests.java`) picks it up without extra pom.xml config —
 * same reason the mobile runner is named "TestRunner" (matches `Test*.java`).
 *
 * Run just this suite with: mvn test -Dtest=ApiTests
 */
@CucumberOptions(
        features = "src/test/resources/features/api",
        glue = {"com.automation.spec.api"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/api/cucumber.html",
                "json:target/cucumber-reports/api/cucumber.json"
        },
        monochrome = true,
        // @blocked scenarios need state this suite must not create on the shared dev backend (real trips, bookings,
        // saved cards) or fault injection; each carries a "# BLOCKED:" comment saying why.
        tags = "not @blocked and not @india-only-skip"
)
public class ApiTests extends AbstractTestNGCucumberTests {
}
