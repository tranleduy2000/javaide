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

import java.util.ArrayList;
import java.util.List;

/**
 * Custom test result based on Gradle's TestResult
 */
class TestResult extends TestResultModel implements Comparable<TestResult> {

    private final long duration;
    private final String device;
    private final String project;
    private final String flavor;
    final ClassTestResults classResults;
    final List<TestFailure> failures = new ArrayList<TestFailure>();
    final String name;
    private boolean ignored;

    public TestResult(String name, long duration, String device, String project, String flavor,
                      ClassTestResults classResults) {
        this.name = name;
        this.duration = duration;
        this.device = device;
        this.project = project;
        this.flavor = flavor;
        this.classResults = classResults;
    }

    public Object getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDevice() {
        return device;
    }

    public String getProject() {
        return project;
    }

    public String getFlavor() {
        return flavor;
    }

    @Override
    public String getTitle() {
        return String.format("Test %s", name);
    }

    @Override
    public ResultType getResultType() {
        if (ignored) {
            return ResultType.SKIPPED;
        }
        return failures.isEmpty() ? ResultType.SUCCESS : ResultType.FAILURE;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public String getFormattedDuration() {
        return ignored ? "-" : super.getFormattedDuration();
    }

    public ClassTestResults getClassResults() {
        return classResults;
    }

    public List<TestFailure> getFailures() {
        return failures;
    }

    public void addFailure(String message, String stackTrace,
                           String deviceName, String projectName, String flavorName) {
        classResults.failed(this, deviceName, projectName, flavorName);
        failures.add(new TestFailure(message, stackTrace, null));
    }

    public void ignored() {
        ignored = true;
    }

    @Override
    public int compareTo(TestResult testResult) {
        int diff = classResults.getName().compareTo(testResult.classResults.getName());
        if (diff != 0) {
            return diff;
        }

        diff = name.compareTo(testResult.name);
        if (diff != 0) {
            return diff;
        }

        diff = device.compareTo(testResult.device);
        if (diff != 0) {
            return diff;
        }

        diff = flavor.compareTo(testResult.flavor);
        if (diff != 0) {
            return diff;
        }

        Integer thisIdentity = System.identityHashCode(this);
        int otherIdentity = System.identityHashCode(testResult);
        return thisIdentity.compareTo(otherIdentity);
    }

    public static class TestFailure {
        private final String message;
        private final String stackTrace;
        private final String exceptionType;

        public TestFailure(String message, String stackTrace, String exceptionType) {
            this.message = message;
            this.stackTrace = stackTrace;
            this.exceptionType = exceptionType;
        }

        public String getMessage() {
            return message;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public String getExceptionType() {
            return exceptionType;
        }
    }

}
