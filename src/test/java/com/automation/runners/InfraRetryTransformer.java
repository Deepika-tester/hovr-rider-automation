package com.automation.runners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Attaches {@link InfraRetryAnalyzer} to every test (every Cucumber scenario) in the run. Registered via ServiceLoader. */
public class InfraRetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(InfraRetryAnalyzer.class);
    }
}
