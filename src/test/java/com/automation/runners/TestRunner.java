package com.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Entry point for the rider-app Appium mobile suite only. Scoped to
 * features/rider — features/driver (a different app: different package/bundle
 * id, no page objects/step defs yet) and features/api (backend REST suite, see
 * com.automation.api.ApiTests) are intentionally excluded so this runner's glue
 * doesn't collide with theirs or launch a driver session for non-UI scenarios.
 */
@CucumberOptions(
        features = "src/test/resources/features/rider",
        glue = {"com.automation.stepdefinitions", "com.automation.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json"
        },
        monochrome = true
        // tags = "@smoke"   // uncomment and set to filter which scenarios run
)
public class TestRunner extends AbstractTestNGCucumberTests {
}