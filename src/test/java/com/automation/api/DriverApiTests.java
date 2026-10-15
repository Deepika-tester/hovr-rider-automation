package com.automation.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Driver backend suite (features/driver-api): ConnectRPC calls the driver app makes. Run with
 * mvn test -Dtest=DriverApiTests -Dplatform=driver-canada (the platform only supplies the test driver's phone number).
 */
@CucumberOptions(
        features = "src/test/resources/features/driver-api",
        glue = {"com.automation.spec.driverapi"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/driver-api/cucumber.html",
                "json:target/cucumber-reports/driver-api/cucumber.json"
        },
        monochrome = true,
        tags = "not @blocked and not @india-only-skip"
)
public class DriverApiTests extends AbstractTestNGCucumberTests {
}
