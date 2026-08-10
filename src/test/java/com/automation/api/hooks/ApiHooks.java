package com.automation.api.hooks;

import com.automation.api.ApiTestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;

/**
 * Hooks for the API suite only — deliberately does NOT touch DriverManager/Appium
 * (see com.automation.hooks.Hooks for the mobile suite's equivalent). Kept in a
 * separate glue package so ApiTestRunner and the mobile TestRunner never load
 * each other's hooks.
 */
public class ApiHooks {

    @Before
    public void setUp() {
        ApiTestContext.clear();
    }

    @After
    public void tearDown() {
        ApiTestContext.clear();
    }
}
