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

import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.sdk.SdkInfo;
import com.android.utils.StringHelper;

import org.gradle.api.Task;
import org.gradle.api.specs.Spec;

import java.io.File;
import java.util.concurrent.Callable;

public class UninstallTask extends BaseTask {

    private BaseVariantData variant;

    private int mTimeOutInMs = 0;

    public UninstallTask() {
        this.getOutputs().upToDateWhen(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                getLogger().debug("Uninstall task is always run.");
                return false;
            }
        });
    }


    public BaseVariantData getVariant() {
        return variant;
    }

    public void setVariant(BaseVariantData variant) {
        this.variant = variant;
    }

    public void setTimeOutInMs(int timeoutInMs) {
        mTimeOutInMs = timeoutInMs;
    }

    public static class ConfigAction implements TaskConfigAction<UninstallTask> {

        private final VariantScope scope;

        public ConfigAction(VariantScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return "uninstall"
                    + StringHelper.capitalize(scope.getVariantConfiguration().getFullName());
        }

        @Override
        public Class<UninstallTask> getType() {
            return UninstallTask.class;
        }

        @Override
        public void execute(UninstallTask uninstallTask) {

            uninstallTask.setDescription(
                    "Uninstalls the " + scope.getVariantData().getDescription() + ".");
            uninstallTask.setGroup(TaskManager.INSTALL_GROUP);
            uninstallTask.setVariant(scope.getVariantData());
            uninstallTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            uninstallTask.setVariantName(scope.getVariantConfiguration().getFullName());
            uninstallTask.setTimeOutInMs(
                    scope.getGlobalScope().getExtension().getAdbOptions().getTimeOutInMs());

            ConventionMappingHelper.map(uninstallTask, "adbExe", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    final SdkInfo info = scope.getGlobalScope().getSdkHandler().getSdkInfo();
                    return (info == null ? null : info.getAdb());
                }
            });

            ((ApkVariantData) scope.getVariantData()).uninstallTask = uninstallTask;
        }
    }
}
