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
import com.android.builder.testing.TestData;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.ddmlib.testrunner.TestRunResult;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Basic Callable to run tests on a given {@link DeviceConnector} using
 * {@link RemoteAndroidTestRunner}.
 *
 * The boolean return value is true if success.
 */
public class SimpleTestCallable implements Callable<Boolean> {

    public static final String FILE_COVERAGE_EC = "coverage.ec";

    @NonNull
    private final String projectName;
    @NonNull
    private final DeviceConnector device;
    @NonNull
    private final String flavorName;
    @NonNull
    private final TestData testData;
    @NonNull
    private final File resultsDir;
    @NonNull
    private final File coverageDir;
    @NonNull
    private final File testApk;
    @NonNull
    private final List<File> testedApks;
    @NonNull
    private final ILogger logger;

    private final int timeoutInMs;

    public SimpleTestCallable(
            @NonNull DeviceConnector device,
            @NonNull String projectName,
            @NonNull String flavorName,
            @NonNull File testApk,
            @NonNull List<File> testedApks,
            @NonNull TestData testData,
            @NonNull File resultsDir,
            @NonNull File coverageDir,
            int timeoutInMs,
            @NonNull ILogger logger) {
        this.projectName = projectName;
        this.device = device;
        this.flavorName = flavorName;
        this.resultsDir = resultsDir;
        this.coverageDir = coverageDir;
        this.testApk = testApk;
        this.testedApks = testedApks;
        this.testData = testData;
        this.timeoutInMs = timeoutInMs;
        this.logger = logger;
    }

    @Override
    public Boolean call() throws Exception {
        String deviceName = device.getName();
        boolean isInstalled = false;

        CustomTestRunListener runListener = new CustomTestRunListener(
                deviceName, projectName, flavorName, logger);
        runListener.setReportDir(resultsDir);

        long time = System.currentTimeMillis();
        boolean success = false;

        String coverageFile = "/data/data/" + testData.getTestedApplicationId() + "/" + FILE_COVERAGE_EC;

        try {
            device.connect(timeoutInMs, logger);

            if (!testedApks.isEmpty()) {
                logger.verbose("DeviceConnector '%s': installing %s", deviceName,
                        Joiner.on(',').join(testedApks));
                if (testedApks.size() > 1 && device.getApiLevel() < 21) {
                    throw new InstallException("Internal error, file a bug, multi-apk applications"
                            + " require a device with API level 21+");
                }
                if (device.getApiLevel() >= 21) {
                    device.installPackages(testedApks,
                            ImmutableList.<String>of() /* installOptions */, timeoutInMs, logger);
                } else {
                    device.installPackage(testedApks.get(0),
                            ImmutableList.<String>of() /* installOptions */, timeoutInMs, logger);
                }
            }

            logger.verbose("DeviceConnector '%s': installing %s", deviceName, testApk);
            if (device.getApiLevel() >= 21) {
                device.installPackages(ImmutableList.of(testApk),
                        ImmutableList.<String>of() /* installOptions */,timeoutInMs, logger);
            } else {
                device.installPackage(testApk,
                        ImmutableList.<String>of() /* installOptions */, timeoutInMs, logger);
            }
            isInstalled = true;

            RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
                    testData.getApplicationId(),
                    testData.getInstrumentationRunner(),
                    device);

            for (Map.Entry<String, String> argument:
                    testData.getInstrumentationRunnerArguments().entrySet()) {
                runner.addInstrumentationArg(argument.getKey(), argument.getValue());
            }

            if (testData.isTestCoverageEnabled()) {
                runner.addInstrumentationArg("coverage", "true");
                runner.addInstrumentationArg("coverageFile", coverageFile);
            }

            runner.setRunName(deviceName);
            runner.setMaxtimeToOutputResponse(timeoutInMs);

            runner.run(runListener);

            TestRunResult testRunResult = runListener.getRunResult();

            success = true;

            // for now throw an exception if no tests.
            // TODO return a status instead of allow merging of multi-variants/multi-device reports.
            if (testRunResult.getNumTests() == 0) {
                CustomTestRunListener fakeRunListener = new CustomTestRunListener(
                        deviceName, projectName, flavorName, logger);
                fakeRunListener.setReportDir(resultsDir);

                // create a fake test output
                Map<String, String> emptyMetrics = Collections.emptyMap();
                TestIdentifier fakeTest = new TestIdentifier(device.getClass().getName(), "No tests found.");
                fakeRunListener.testStarted(fakeTest);
                fakeRunListener.testFailed(
                        fakeTest,
                        "No tests found. This usually means that your test classes are"
                                + " not in the form that your test runner expects (e.g. don't inherit from"
                                + " TestCase or lack @Test annotations).");
                fakeRunListener.testEnded(fakeTest, emptyMetrics);

                // end the run to generate the XML file.
                fakeRunListener.testRunEnded(System.currentTimeMillis() - time, emptyMetrics);
                return false;
            }

            return !testRunResult.hasFailedTests();
        } catch (Exception e) {
            Map<String, String> emptyMetrics = Collections.emptyMap();

            // create a fake test output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos, true);
            e.printStackTrace(pw);
            TestIdentifier fakeTest = new TestIdentifier(device.getClass().getName(), "runTests");
            runListener.testStarted(fakeTest);
            runListener.testFailed(fakeTest , baos.toString());
            runListener.testEnded(fakeTest, emptyMetrics);

            // end the run to generate the XML file.
            runListener.testRunEnded(System.currentTimeMillis() - time, emptyMetrics);

            // and throw
            throw e;
        } finally {
            if (isInstalled) {
                // Get the coverage if needed.
                if (success && testData.isTestCoverageEnabled()) {
                    String temporaryCoverageCopy = "/data/local/tmp/"
                            + testData.getTestedApplicationId() + "." + FILE_COVERAGE_EC;

                    MultiLineReceiver outputReceiver = new MultiLineReceiver() {
                        @Override
                        public void processNewLines(String[] lines) {
                            for (String line : lines) {
                                logger.info(line);
                            }
                        }

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }
                    };

                    logger.verbose("DeviceConnector '%s': fetching coverage data from %s",
                            deviceName, coverageFile);
                    device.executeShellCommand("run-as " + testData.getTestedApplicationId()
                                    + " cat " + coverageFile + " | cat > " + temporaryCoverageCopy,
                            outputReceiver,
                            30, TimeUnit.SECONDS);
                    device.pullFile(
                            temporaryCoverageCopy,
                            new File(coverageDir, FILE_COVERAGE_EC).getPath());
                    device.executeShellCommand("rm " + temporaryCoverageCopy,
                            outputReceiver,
                            30, TimeUnit.SECONDS);
                }

                // uninstall the apps
                // This should really not be null, because if it was the build
                // would have broken before.
                uninstall(testApk, testData.getApplicationId(), deviceName);

                if (!testedApks.isEmpty()) {
                    for (File testedApk : testedApks) {
                        uninstall(testedApk, testData.getTestedApplicationId(), deviceName);
                    }
                }
            }

            device.disconnect(timeoutInMs, logger);
        }
    }

    private void uninstall(@NonNull File apkFile, @Nullable String packageName,
                           @NonNull String deviceName)
            throws DeviceException {
        if (packageName != null) {
            logger.verbose("DeviceConnector '%s': uninstalling %s", deviceName, packageName);
            device.uninstallPackage(packageName, timeoutInMs, logger);
        } else {
            logger.verbose("DeviceConnector '%s': unable to uninstall %s: unable to get package name",
                    deviceName, apkFile);
        }
    }
}
