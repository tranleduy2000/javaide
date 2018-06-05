/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.internal.test.report;

import static org.gradle.api.tasks.testing.TestResult.ResultType;

import com.android.builder.core.BuilderConstants;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Custom CompositeTestResults based on Gradle's CompositeTestResults
 */
public abstract class CompositeTestResults extends TestResultModel {
    private final CompositeTestResults parent;
    private int tests;
    private final Set<TestResult> failures = new TreeSet<TestResult>();
    private long duration;

    private final Map<String, DeviceTestResults> devices = new TreeMap<String, DeviceTestResults>();
    private final Map<String, VariantTestResults> variants = new TreeMap<String, VariantTestResults>();


    protected CompositeTestResults(CompositeTestResults parent) {
        this.parent = parent;
    }

    public String getFilename(ReportType reportType) {
        return getName();
    }

    public abstract String getName();

    public int getTestCount() {
        return tests;
    }

    public int getFailureCount() {
        return failures.size();
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public String getFormattedDuration() {
        return getTestCount() == 0 ? "-" : super.getFormattedDuration();
    }

    public Set<TestResult> getFailures() {
        return failures;
    }

    Map<String, DeviceTestResults> getResultsPerDevices() {
        return devices;
    }

    Map<String, VariantTestResults> getResultsPerVariants() {
        return variants;
    }

    @Override
    public ResultType getResultType() {
        return failures.isEmpty() ? ResultType.SUCCESS : ResultType.FAILURE;
    }

    public String getFormattedSuccessRate() {
        Number successRate = getSuccessRate();
        if (successRate == null) {
            return "-";
        }
        return successRate + "%";
    }

    public Number getSuccessRate() {
        if (getTestCount() == 0) {
            return null;
        }

        BigDecimal tests = BigDecimal.valueOf(getTestCount());
        BigDecimal successful = BigDecimal.valueOf(getTestCount() - getFailureCount());

        return successful.divide(tests, 2,
                BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)).intValue();
    }

    protected void failed(TestResult failedTest,
                          String deviceName, String projectName, String flavorName) {
        failures.add(failedTest);
        if (parent != null) {
            parent.failed(failedTest, deviceName, projectName, flavorName);
        }

        DeviceTestResults deviceResults = devices.get(deviceName);
        if (deviceResults != null) {
            deviceResults.failed(failedTest, deviceName, projectName, flavorName);
        }

        String key = getVariantKey(projectName, flavorName);
        VariantTestResults variantResults = variants.get(key);
        if (variantResults != null) {
            variantResults.failed(failedTest, deviceName, projectName, flavorName);
        }
    }

    protected TestResult addTest(TestResult test) {
        tests++;
        duration += test.getDuration();
        return test;
    }

    protected void addDevice(String deviceName, TestResult testResult) {
        DeviceTestResults deviceResults = devices.get(deviceName);
        if (deviceResults == null) {
            deviceResults = new DeviceTestResults(deviceName, null);
            devices.put(deviceName, deviceResults);
        }

        deviceResults.addTest(testResult);
    }

    protected void addVariant(String projectName, String flavorName, TestResult testResult) {
        String key = getVariantKey(projectName, flavorName);
        VariantTestResults variantResults = variants.get(key);
        if (variantResults == null) {
            variantResults = new VariantTestResults(key, null);
            variants.put(key, variantResults);
        }

        variantResults.addTest(testResult);
    }

    private static String getVariantKey(String projectName, String flavorName) {
        if (BuilderConstants.MAIN.equalsIgnoreCase(flavorName)) {
            return projectName;
        }

        return projectName + ":" + flavorName;
    }
}
