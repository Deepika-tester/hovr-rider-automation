package com.automation.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Entry point for the backend/API suite (api_auth, api_dispatch, api_pricing).
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
        glue = {"com.automation.api.stepdefinitions", "com.automation.api.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/api/cucumber.html",
                "json:target/cucumber-reports/api/cucumber.json"
        },
        monochrome = true
)
public class ApiTests extends AbstractTestNGCucumberTests {
}
