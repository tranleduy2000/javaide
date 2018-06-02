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

package com.android.builder.testing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.internal.InstallUtils;
import com.android.builder.internal.testing.CustomTestRunListener;
import com.android.builder.internal.testing.SimpleTestCallable;
import com.android.builder.testing.api.DeviceConfigProviderImpl;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.builder.testing.api.TestException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.builder.testing.api.DeviceConfigProvider;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Basic {@link TestRunner} running tests on all devices.
 */
public class SimpleTestRunner implements TestRunner {

    @Nullable
    private final File mSplitSelectExec;
    @NonNull
    private final ProcessExecutor mProcessExecutor;

    public SimpleTestRunner(
            @Nullable File splitSelectExec,
            @NonNull ProcessExecutor processExecutor) {
        mSplitSelectExec = splitSelectExec;
        mProcessExecutor = processExecutor;
    }

    @Override
    public boolean runTests(
            @NonNull String projectName,
            @NonNull String variantName,
            @NonNull File testApk,
            @NonNull TestData testData,
            @NonNull List<? extends DeviceConnector> deviceList,
                     int maxThreads,
                     int timeoutInMs,
            @NonNull Collection<String> installOptions,
            @NonNull File resultsDir,
            @NonNull File coverageDir,
            @NonNull ILogger logger) throws TestException, NoAuthorizedDeviceFoundException, InterruptedException {

        WaitableExecutor<Boolean> executor = new WaitableExecutor<Boolean>(maxThreads);

        int totalDevices = deviceList.size();
        int unauthorizedDevices = 0;
        int compatibleDevices = 0;

        for (final DeviceConnector device : deviceList) {
            if (device.getState() != IDevice.DeviceState.UNAUTHORIZED) {
                if (InstallUtils.checkDeviceApiLevel(
                        device, testData.getMinSdkVersion(), logger, projectName, variantName)) {

                    final DeviceConfigProvider deviceConfigProvider;
                    try {
                        deviceConfigProvider = new DeviceConfigProviderImpl(device);
                    } catch (DeviceException e) {
                        throw new TestException(e);
                    }

                    // now look for a matching output file
                    ImmutableList<File> testedApks = ImmutableList.of();
                    if (!testData.isLibrary()) {
                        try {
                            testedApks = testData.getTestedApks(
                                    mProcessExecutor,
                                    mSplitSelectExec,
                                    deviceConfigProvider,
                                    logger);
                        } catch (ProcessException e) {
                            throw new TestException(e);
                        }

                        if (testedApks.isEmpty()) {
                            logger.info("Skipping device '%1$s' for '%2$s:%3$s': No matching output file",
                                    device.getName(), projectName, variantName);
                            continue;
                        }
                    }

                    compatibleDevices++;
                    executor.execute(
                            new SimpleTestCallable(
                                    device, projectName, variantName, testApk, testedApks, testData,
                                    resultsDir, coverageDir, timeoutInMs, logger));
                }
            } else {
                unauthorizedDevices++;
            }
        }

        if (totalDevices == 0 || compatibleDevices == 0) {
            CustomTestRunListener fakeRunListener = new CustomTestRunListener(
                    "TestRunner", projectName, variantName, logger);
            fakeRunListener.setReportDir(resultsDir);

            // create a fake test output
            Map<String, String> emptyMetrics = Collections.emptyMap();
            TestIdentifier fakeTest = new TestIdentifier(variantName,
                    totalDevices == 0 ? ": No devices connected." : ": No compatible devices connected.");
            fakeRunListener.testStarted(fakeTest);
            fakeRunListener.testFailed(
                    fakeTest,
                    String.format("Found %d connected device(s), %d of which were compatible.", totalDevices, compatibleDevices));
            fakeRunListener.testEnded(fakeTest, emptyMetrics);

            // end the run to generate the XML file.
            fakeRunListener.testRunEnded(0, emptyMetrics);

            return false;
        } else {

            if (unauthorizedDevices > 0) {
                CustomTestRunListener fakeRunListener = new CustomTestRunListener(
                        "TestRunner", projectName, variantName, logger);
                fakeRunListener.setReportDir(resultsDir);

                // create a fake test output
                Map<String, String> emptyMetrics = Collections.emptyMap();
                TestIdentifier fakeTest = new TestIdentifier(variantName, ": found unauthorized devices.");
                fakeRunListener.testStarted(fakeTest);
                fakeRunListener.testFailed(fakeTest , String.format("Found %d unauthorized device(s).", unauthorizedDevices));
                fakeRunListener.testEnded(fakeTest, emptyMetrics);

                // end the run to generate the XML file.
                fakeRunListener.testRunEnded(0, emptyMetrics);
            }

            List<WaitableExecutor.TaskResult<Boolean>> results = executor.waitForAllTasks();

            boolean success = unauthorizedDevices == 0;

            // check if one test failed or if there was an exception.
            for (WaitableExecutor.TaskResult<Boolean> result : results) {
                if (result.value != null) {
                    success &= result.value;
                } else {
                    success = false;
                    logger.error(result.exception, null);
                }
            }
            return success;
        }
    }
}
