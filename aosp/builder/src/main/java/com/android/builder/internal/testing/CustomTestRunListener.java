/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.builder.internal.testing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.ddmlib.testrunner.TestResult;
import com.android.ddmlib.testrunner.XmlTestRunListener;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Custom version of {@link com.android.ddmlib.testrunner.ITestRunListener}.
 */
public class CustomTestRunListener extends XmlTestRunListener {

    @NonNull
    private final String mDeviceName;
    @NonNull
    private final String mProjectName;
    @NonNull
    private final String mFlavorName;
    private final ILogger mLogger;
    private final Set<TestIdentifier> mFailedTests = Sets.newHashSet();


    public CustomTestRunListener(@NonNull String deviceName,
                                 @NonNull String projectName, @NonNull String flavorName,
                                 @Nullable ILogger logger) {
        mDeviceName = deviceName;
        mProjectName = projectName;
        mFlavorName = flavorName;
        mLogger = logger;
    }

    @Override
    protected File getResultFile(File reportDir) throws IOException {
        return new File(reportDir,
                "TEST-" + mDeviceName + "-" + mProjectName + "-" + mFlavorName + ".xml");
    }

    @Override
    protected String getTestSuiteName() {
        // in order for the gradle report to look good we put the test suite name as one of the
        // test class name.

        Map<TestIdentifier, TestResult> testResults = getRunResult().getTestResults();
        if (testResults.isEmpty()) {
            return null;
        }

        Map.Entry<TestIdentifier, TestResult> testEntry = testResults.entrySet().iterator().next();
        return testEntry.getKey().getClassName();
    }

    @Override
    protected Map<String, String> getPropertiesAttributes() {
        Map<String, String> propertiesAttributes = Maps.newLinkedHashMap(super.getPropertiesAttributes());
        propertiesAttributes.put("device", mDeviceName);
        propertiesAttributes.put("flavor", mFlavorName);
        propertiesAttributes.put("project", mProjectName);
        return ImmutableMap.copyOf(propertiesAttributes);
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
        if (mLogger != null) {
            mLogger.info("Starting %1$d tests on %2$s", testCount, mDeviceName);
        }
        super.testRunStarted(runName, testCount);
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        if (mLogger != null) {
            mLogger.warning("\n%1$s > %2$s[%3$s] \033[31mFAILED \033[0m",
                    test.getClassName(), test.getTestName(), mDeviceName);
            mLogger.warning(getModifiedTrace(trace));
        }

        mFailedTests.add(test);

        super.testFailed(test, trace);
    }
    
    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
        if (mLogger != null) {
            mLogger.warning("\n%1$s > %2$s[%3$s] \033[33mSKIPPED \033[0m\n%4$s",
                    test.getClassName(), test.getTestName(), mDeviceName, getModifiedTrace(trace));
        }
        super.testAssumptionFailure(test, trace);
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        if (!mFailedTests.remove(test)) {
            // if wasn't present in the list, then the test succeeded.
            if (mLogger != null) {
                mLogger.info("\n%1$s > %2$s[%3$s] \033[32mSUCCESS \033[0m",
                        test.getClassName(), test.getTestName(), mDeviceName);
            }

        }
        super.testEnded(test, testMetrics);
    }

    @Override
    public void testRunFailed(String errorMessage) {
        if (mLogger != null) {
            mLogger.warning("Tests on %1$s failed: %2$s", mDeviceName, errorMessage);
        }
        super.testRunFailed(errorMessage);
    }

    @Override
    public void testIgnored(TestIdentifier test) {
        if (mLogger != null) {
            mLogger.warning("\n%1$s > %2$s[%3$s] \033[33mSKIPPED \033[0m",
                    test.getClassName(), test.getTestName(), mDeviceName);
        }
        super.testIgnored(test);
    }

    private String getModifiedTrace(String trace) {
        // split lines
        String[] lines = trace.split("\n");

        if (lines.length < 2) {
            return trace;
        }

        // get the first two lines, and prepend \t on them
        return "\t" + lines[0] + "\n\t" + lines[1];
    }
}
