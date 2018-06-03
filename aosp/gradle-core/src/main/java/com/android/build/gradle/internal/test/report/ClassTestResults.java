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

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Custom ClassTestResults based on Gradle's ClassTestResults
 */
class ClassTestResults extends CompositeTestResults {

    private final String name;
    private final PackageTestResults packageResults;
    private final Set<TestResult> results = new TreeSet<TestResult>();
    private final StringBuilder standardOutput = new StringBuilder();
    private final StringBuilder standardError = new StringBuilder();

    public ClassTestResults(String name, PackageTestResults packageResults) {
        super(packageResults);
        this.name = name;
        this.packageResults = packageResults;
    }

    @Override
    public String getTitle() {
        return String.format("Class %s", name);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSimpleName() {
        int pos = name.lastIndexOf(".");
        if (pos != -1) {
            return name.substring(pos + 1);
        }
        return name;
    }

    public PackageTestResults getPackageResults() {
        return packageResults;
    }

    public Map<String, Map<String, TestResult>> getTestResultsMap() {
        Map<String, Map<String, TestResult>> map = Maps.newHashMap();
        for (TestResult result : results) {
            String device = result.getDevice();

            Map<String, TestResult> deviceMap = map.get(device);
            if (deviceMap == null) {
                deviceMap = Maps.newHashMap();
                map.put(device, deviceMap);
            }

            deviceMap.put(result.getName(), result);
        }

        return map;
    }

    public CharSequence getStandardError() {
        return standardError;
    }

    public CharSequence getStandardOutput() {
        return standardOutput;
    }

    public TestResult addTest(String testName, long duration,
                              String device, String project, String flavor) {
        TestResult test = new TestResult(testName, duration, device, project, flavor, this);
        results.add(test);

        addDevice(device, test);
        addVariant(project, flavor, test);

        return addTest(test);
    }

    public void addStandardOutput(String textContent) {
        standardOutput.append(textContent);
    }

    public void addStandardError(String textContent) {
        standardError.append(textContent);
    }
}
