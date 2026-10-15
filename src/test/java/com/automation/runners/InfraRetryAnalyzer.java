package com.automation.runners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a scenario once or twice, but ONLY when it died of an infrastructure fault -- the UiAutomator2 server
 * crashing or dropping its connection, or the Appium session failing to start. Those say nothing about the app, and
 * on a busy emulator they hit roughly one scenario in ten. A scenario whose assertion failed is never retried, so
 * real failures are reported exactly once and as they are.
 */
public class InfraRetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES = 2;
    private static final String[] INFRA_SIGNS = {
            "socket hang up",
            "instrumentation process is not running",
            "Could not proxy command",
            "Could not start a new session",
            "SessionNotCreatedException",
            "A session is either terminated or not started",
            "NoSuchSessionException",
            "ECONNRESET",
            "UiAutomator2 server"
    };

    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts >= MAX_RETRIES || !isInfra(result.getThrowable())) {
            return false;
        }
        attempts++;
        return true;
    }

    private static boolean isInfra(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String m = String.valueOf(c.getMessage()) + " " + c.getClass().getName();
            for (String sign : INFRA_SIGNS) {
                if (m.contains(sign)) {
                    return true;
                }
            }
        }
        return false;
    }
}
