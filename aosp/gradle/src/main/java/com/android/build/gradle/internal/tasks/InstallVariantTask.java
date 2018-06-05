/*
 * Copyright (C) 2014 The Android Open Source Project
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

import static com.android.sdklib.BuildToolInfo.PathId.SPLIT_SELECT;

import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.internal.InstallUtils;
import com.android.builder.sdk.SdkInfo;
import com.android.builder.sdk.TargetInfo;
import com.android.builder.testing.ConnectedDeviceProvider;
import com.android.builder.testing.api.DeviceConfigProviderImpl;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.builder.testing.api.DeviceProvider;
import com.android.ide.common.build.SplitOutputMatcher;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Task installing an app variant. It looks at connected device and install the best matching
 * variant output on each device.
 */
public class InstallVariantTask extends BaseTask {

    private File adbExe;

    private File splitSelectExe;

    private ProcessExecutor processExecutor;

    private String projectName;

    private int timeOutInMs = 0;

    private Collection<String> installOptions;

    private BaseVariantData<? extends BaseVariantOutputData> variantData;

    public InstallVariantTask() {
        this.getOutputs().upToDateWhen(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                getLogger().debug("Install task is always run.");
                return false;
            }
        });
    }

    @TaskAction
    public void install() throws DeviceException, ProcessException, InterruptedException {
        final ILogger iLogger = new LoggerWrapper(getLogger(), LogLevel.LIFECYCLE);
        DeviceProvider deviceProvider = new ConnectedDeviceProvider(getAdbExe(), iLogger);
        deviceProvider.init();

        VariantConfiguration variantConfig = variantData.getVariantConfiguration();
        String variantName = variantConfig.getFullName();

        int successfulInstallCount = 0;
        List<? extends DeviceConnector> devices = deviceProvider.getDevices();
        for (final DeviceConnector device : devices) {
            if (InstallUtils.checkDeviceApiLevel(
                    device, variantConfig.getMinSdkVersion(), iLogger, projectName, variantName)) {
                // When InstallUtils.checkDeviceApiLevel returns false, it logs the reason.
                final List<File> apkFiles = SplitOutputMatcher.computeBestOutput(processExecutor,
                        getSplitSelectExe(),
                        new DeviceConfigProviderImpl(device),
                        variantData.getOutputs(),
                        variantData.getVariantConfiguration().getSupportedAbis());

                if (apkFiles.isEmpty()) {
                    getLogger().lifecycle(
                            "Skipping device '{}' for '{}:{}': Could not find build of variant " +
                                    "which supports density {} and an ABI in {}",
                            device.getName(), projectName, variantName,
                            device.getDensity(), Joiner.on(", ").join(device.getAbis()));
                } else {
                    getLogger().lifecycle(
                            "Installing APK '{}' on '{}' for {}:{}",
                            FileUtils.getNamesAsCommaSeparatedList(apkFiles),
                            device.getName(),
                            projectName,
                            variantName);

                    final Collection<String> extraArgs =
                            Objects.firstNonNull(installOptions, ImmutableList.<String>of());

                    if (apkFiles.size() > 1 || device.getApiLevel() >= 21) {
                        device.installPackages(apkFiles, extraArgs,
                                getTimeOutInMs(), getILogger());
                        successfulInstallCount++;
                    } else {
                        device.installPackage(apkFiles.get(0), extraArgs,
                                getTimeOutInMs(),
                                getILogger());
                        successfulInstallCount++;
                    }
                }
            }
        }

        if (successfulInstallCount == 0) {
            throw new GradleException("Failed to install on any devices.");
        } else {
            getLogger().quiet("Installed on {} {}.",
                    successfulInstallCount,
                    successfulInstallCount==1 ? "device" : "devices");
        }
    }

    @InputFile
    public File getAdbExe() {
        return adbExe;
    }

    public void setAdbExe(File adbExe) {
        this.adbExe = adbExe;
    }

    @InputFile
    @Optional
    public File getSplitSelectExe() {
        return splitSelectExe;
    }

    public void setSplitSelectExe(File splitSelectExe) {
        this.splitSelectExe = splitSelectExe;
    }

    public ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }

    public void setProcessExecutor(ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Input
    public int getTimeOutInMs() {
        return timeOutInMs;
    }

    public void setTimeOutInMs(int timeOutInMs) {
        this.timeOutInMs = timeOutInMs;
    }

    @Input
    @Optional
    public Collection<String> getInstallOptions() {
        return installOptions;
    }

    public void setInstallOptions(Collection<String> installOptions) {
        this.installOptions = installOptions;
    }

    public BaseVariantData<? extends BaseVariantOutputData> getVariantData() {
        return variantData;
    }

    public void setVariantData(
            BaseVariantData<? extends BaseVariantOutputData> variantData) {
        this.variantData = variantData;
    }

    public static class ConfigAction implements TaskConfigAction<InstallVariantTask> {

        private final VariantScope scope;

        public ConfigAction(VariantScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("install");
        }

        @Override
        public Class<InstallVariantTask> getType() {
            return InstallVariantTask.class;
        }

        @Override
        public void execute(InstallVariantTask installTask) {
            installTask.setDescription(
                    "Installs the " + scope.getVariantData().getDescription() + ".");
            installTask.setVariantName(scope.getVariantConfiguration().getFullName());
            installTask.setGroup(TaskManager.INSTALL_GROUP);
            installTask.setProjectName(scope.getGlobalScope().getProject().getName());
            installTask.setVariantData(scope.getVariantData());
            installTask.setTimeOutInMs(
                    scope.getGlobalScope().getExtension().getAdbOptions().getTimeOutInMs());
            installTask.setInstallOptions(
                    scope.getGlobalScope().getExtension().getAdbOptions().getInstallOptions());
            installTask.setProcessExecutor(
                    scope.getGlobalScope().getAndroidBuilder().getProcessExecutor());
            ConventionMappingHelper.map(installTask, "adbExe", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    final SdkInfo info = scope.getGlobalScope().getSdkHandler().getSdkInfo();
                    return (info == null ? null : info.getAdb());
                }
            });
            ConventionMappingHelper.map(installTask, "splitSelectExe", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    final TargetInfo info =
                            scope.getGlobalScope().getAndroidBuilder().getTargetInfo();
                    String path = info == null ? null : info.getBuildTools().getPath(SPLIT_SELECT);
                    if (path != null) {
                        File splitSelectExe = new File(path);
                        return splitSelectExe.exists() ? splitSelectExe : null;
                    } else {
                        return null;
                    }
                }
            });
            ((ApkVariantData) scope.getVariantData()).installTask = installTask;
        }
    }
}
