/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.build.gradle.internal.tasks;

import static com.android.builder.core.BuilderConstants.CONNECTED;
import static com.android.builder.core.BuilderConstants.DEVICE;
import static com.android.builder.core.BuilderConstants.FD_ANDROID_RESULTS;
import static com.android.builder.core.BuilderConstants.FD_ANDROID_TESTS;
import static com.android.builder.core.BuilderConstants.FD_FLAVORS;
import static com.android.builder.core.BuilderConstants.FD_REPORTS;
import static com.android.builder.model.AndroidProject.FD_OUTPUTS;
import static com.android.sdklib.BuildToolInfo.PathId.SPLIT_SELECT;

import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.test.report.ReportType;
import com.android.build.gradle.internal.test.report.TestReport;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.builder.internal.testing.SimpleTestCallable;
import com.android.builder.sdk.SdkInfo;
import com.android.builder.sdk.TargetInfo;
import com.android.builder.testing.ConnectedDeviceProvider;
import com.android.builder.testing.SimpleTestRunner;
import com.android.builder.testing.TestData;
import com.android.builder.testing.TestRunner;
import com.android.builder.testing.api.DeviceException;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.utils.FileUtils;
import com.android.utils.StringHelper;
import com.google.common.collect.ImmutableList;

import org.gradle.api.GradleException;
import org.gradle.api.Nullable;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.ConsoleRenderer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Run instrumentation tests for a given variant
 */
public class DeviceProviderInstrumentTestTask extends BaseTask implements AndroidTestTask {

    private File reportsDir;
    private File resultsDir;
    private File coverageDir;

    private String flavorName;

    @Nullable
    private Collection<String> installOptions;

    private DeviceProvider deviceProvider;
    private TestData testData;

    private File adbExec;
    @Nullable
    private File splitSelectExec;
    private ProcessExecutor processExecutor;

    private boolean ignoreFailures;
    private boolean testFailed;

    @TaskAction
    protected void runTests() throws DeviceException, IOException, InterruptedException,
            TestRunner.NoAuthorizedDeviceFoundException, TestException {

        File resultsOutDir = getResultsDir();
        FileUtils.emptyFolder(resultsOutDir);

        File coverageOutDir = getCoverageDir();
        FileUtils.emptyFolder(coverageOutDir);

        boolean success = false;
        // If there are tests to run, and the test runner returns with no results, we fail (since
        // this is most likely a problem with the device setup). If no, the task will succeed.
        if (!testsFound()) {
            getLogger().info("No tests found, nothing to do.");
            // If we don't create the coverage file, createXxxCoverageReport task will fail.
            File emptyCoverageFile = new File(coverageOutDir, SimpleTestCallable.FILE_COVERAGE_EC);
            emptyCoverageFile.createNewFile();
            success = true;
        } else {
            File testApk = testData.getTestApk();
            String flavor = getFlavorName();
            TestRunner testRunner = new SimpleTestRunner(
                    getSplitSelectExec(),
                    getProcessExecutor());
            deviceProvider.init();

            Collection<String> extraArgs = installOptions == null || installOptions.isEmpty()
                    ? ImmutableList.<String>of() : installOptions;
            try {
                success = testRunner.runTests(getProject().getName(), flavor,
                        testApk,
                        testData,
                        deviceProvider.getDevices(),
                        deviceProvider.getMaxThreads(),
                        deviceProvider.getTimeoutInMs(),
                        extraArgs,
                        resultsOutDir,
                        coverageOutDir,
                        getILogger());
            } finally {
                deviceProvider.terminate();
            }

        }

        // run the report from the results.
        File reportOutDir = getReportsDir();
        FileUtils.emptyFolder(reportOutDir);

        TestReport report = new TestReport(ReportType.SINGLE_FLAVOR, resultsOutDir, reportOutDir);
        report.generateReport();

        if (!success) {
            testFailed = true;
            String reportUrl = new ConsoleRenderer().asClickableFileUrl(
                    new File(reportOutDir, "index.html"));
            String message = "There were failing tests. See the report at: " + reportUrl;
            if (getIgnoreFailures()) {
                getLogger().warn(message);
                return;

            } else {
                throw new GradleException(message);
            }
        }

        testFailed = false;
    }

    /**
     * Determines if there are any tests to run.
     *
     * @return true if there are some tests to run, false otherwise
     */
    private boolean testsFound() {
        // For now we check if there are any test sources. We could inspect the test classes and
        // apply JUnit logic to see if there's something to run, but that would not catch the case
        // where user makes a typo in a test name or forgets to inherit from a JUnit class
        return !getProject().files(testData.getTestDirectories()).getAsFileTree().isEmpty();
    }

    public File getReportsDir() {
        return reportsDir;
    }

    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    public File getResultsDir() {
        return resultsDir;
    }

    public void setResultsDir(File resultsDir) {
        this.resultsDir = resultsDir;
    }

    public File getCoverageDir() {
        return coverageDir;
    }

    public void setCoverageDir(File coverageDir) {
        this.coverageDir = coverageDir;
    }

    public String getFlavorName() {
        return flavorName;
    }

    public void setFlavorName(String flavorName) {
        this.flavorName = flavorName;
    }

    public Collection<String> getInstallOptions() {
        return installOptions;
    }

    public void setInstallOptions(Collection<String> installOptions) {
        this.installOptions = installOptions;
    }

    public DeviceProvider getDeviceProvider() {
        return deviceProvider;
    }

    public void setDeviceProvider(DeviceProvider deviceProvider) {
        this.deviceProvider = deviceProvider;
    }

    public TestData getTestData() {
        return testData;
    }

    public void setTestData(TestData testData) {
        this.testData = testData;
    }

    public File getAdbExec() {
        return adbExec;
    }

    public void setAdbExec(File adbExec) {
        this.adbExec = adbExec;
    }

    public File getSplitSelectExec() {
        return splitSelectExec;
    }

    public void setSplitSelectExec(File splitSelectExec) {
        this.splitSelectExec = splitSelectExec;
    }

    public ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }

    public void setProcessExecutor(ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    @Override
    public boolean getTestFailed() {
        return testFailed;
    }


    public static class ConfigAction implements TaskConfigAction<DeviceProviderInstrumentTestTask> {

        private final VariantScope scope;
        private final DeviceProvider deviceProvider;
        private final TestData testData;

        public ConfigAction(VariantScope scope, DeviceProvider deviceProvider, TestData testData) {
            this.scope = scope;
            this.deviceProvider = deviceProvider;
            this.testData = testData;
        }

        @Override
        public String getName() {
            return scope.getTaskName(deviceProvider.getName());
        }

        @Override
        public Class<DeviceProviderInstrumentTestTask> getType() {
            return DeviceProviderInstrumentTestTask.class;
        }

        @Override
        public void execute(DeviceProviderInstrumentTestTask task) {
            final boolean connected = deviceProvider instanceof ConnectedDeviceProvider;
            String variantName = scope.getTestedVariantData() != null ?
                    scope.getTestedVariantData().getName() : scope.getVariantData().getName();
            if (connected) {
                task.setDescription("Installs and runs the tests for " + variantName +
                        " on connected devices.");
            } else {
                task.setDescription("Installs and runs the tests for " + variantName +
                        " using provider: " + StringHelper.capitalize(deviceProvider.getName()));

            }
            task.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
            task.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            task.setVariantName(variantName);
            task.setTestData(testData);
            task.setFlavorName(testData.getFlavorName());
            task.setDeviceProvider(deviceProvider);
            task.setInstallOptions(scope.getGlobalScope().getExtension().getAdbOptions().getInstallOptions());
            task.setProcessExecutor(scope.getGlobalScope().getAndroidBuilder().getProcessExecutor());

            String flavorFolder = testData.getFlavorName();
            if (!flavorFolder.isEmpty()) {
                flavorFolder = FD_FLAVORS + "/" + flavorFolder;
            }
            String providerFolder = connected ? CONNECTED : DEVICE + "/" + deviceProvider.getName();
            final String subFolder = "/" + providerFolder + "/" + flavorFolder;

            ConventionMappingHelper.map(task, "adbExec", new Callable<File>() {
                @Override
                public File call() {
                    final SdkInfo info = scope.getGlobalScope().getSdkHandler()
                            .getSdkInfo();
                    return (info == null ? null : info.getAdb());
                }
            });
            ConventionMappingHelper.map(task, "splitSelectExec", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    final TargetInfo info = scope.getGlobalScope().getAndroidBuilder().getTargetInfo();
                    String path = info == null ? null : info.getBuildTools().getPath(SPLIT_SELECT);
                    if (path != null) {
                        File splitSelectExe = new File(path);
                        return splitSelectExe.exists() ? splitSelectExe : null;
                    } else {
                        return null;
                    }
                }
            });

            ConventionMappingHelper.map(task, "resultsDir", new Callable<File>() {
                @Override
                public File call() {
                    String rootLocation = scope.getGlobalScope().getExtension().getTestOptions().getResultsDir();
                    if (rootLocation == null) {
                        rootLocation = scope.getGlobalScope().getBuildDir() + "/" +
                                FD_OUTPUTS + "/" + FD_ANDROID_RESULTS;
                    }
                    return scope.getGlobalScope().getProject().file(rootLocation + subFolder);
                }
            });

            ConventionMappingHelper.map(task, "reportsDir", new Callable<File>() {
                @Override
                public File call() {
                    String rootLocation = scope.getGlobalScope().getExtension().getTestOptions().getReportDir();
                    if (rootLocation == null) {
                        rootLocation = scope.getGlobalScope().getBuildDir() + "/" +
                                FD_REPORTS + "/" + FD_ANDROID_TESTS;
                    }
                    return scope.getGlobalScope().getProject().file(rootLocation + subFolder);
                }
            });

            String rootLocation = scope.getGlobalScope().getBuildDir() + "/" +
                                FD_OUTPUTS + "/code-coverage";
            task.setCoverageDir(scope.getGlobalScope().getProject().file(rootLocation + subFolder));

            if (scope.getVariantData() instanceof TestVariantData) {
                TestVariantData testVariantData = (TestVariantData) scope.getVariantData();
                if (connected) {
                    testVariantData.connectedTestTask = task;
                } else {
                    testVariantData.providerTestTaskList.add(task);
                }
            }

            task.setEnabled(deviceProvider.isConfigured());
        }
    }
}
