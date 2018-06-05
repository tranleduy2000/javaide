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

import com.android.build.gradle.internal.dependency.DependencyChecker;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.SyncIssue;
import com.android.sdklib.SdkVersionInfo;
import com.android.utils.StringHelper;
import com.google.common.collect.Lists;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.tasks.TaskAction;

import java.util.List;
import java.util.Map;

public class PrepareDependenciesTask extends BaseTask {

    private BaseVariantData variant;
    private final List<DependencyChecker> checkers = Lists.newArrayList();

    @TaskAction
    protected void prepare() {
        ApiVersion minSdkVersion = variant.getVariantConfiguration().getMinSdkVersion();
        int minSdk = 1;
        if (minSdkVersion.getCodename() != null) {
            minSdk = SdkVersionInfo.getApiByBuildCode(minSdkVersion.getCodename(), true);
        } else {
            minSdk = minSdkVersion.getApiLevel();
        }

        boolean foundError = false;

        for (DependencyChecker checker : checkers) {
            for (Map.Entry<ModuleVersionIdentifier, Integer> entry :
                    checker.getLegacyApiLevels().entrySet()) {
                ModuleVersionIdentifier mavenVersion = entry.getKey();
                int api = entry.getValue();
                if (api > minSdk) {
                    foundError = true;
                    String configurationName = checker.getConfigurationDependencies().getName();
                    getLogger().error(
                            "Variant {} has a dependency on version {} of the legacy {} Maven " +
                                    "artifact, which corresponds to API level {}. This is not " +
                                    "compatible with min SDK of this module, which is {}. " +
                                    "Please use the 'gradle dependencies' task to debug your " +
                                    "dependencies graph.",
                            StringHelper.capitalize(configurationName),
                            mavenVersion.getVersion(),
                            mavenVersion.getGroup(),
                            api,
                            minSdk);
                }
            }

            for (SyncIssue syncIssue : checker.getSyncIssues()) {
                foundError = true;
                getLogger().error(syncIssue.getMessage());
            }
        }

        if (foundError) {
            throw new GradleException("Dependency Error. See console for details.");
        }

    }

    public void addChecker(DependencyChecker checker) {
        checkers.add(checker);
    }

    public BaseVariantData getVariant() {
        return variant;
    }

    public void setVariant(BaseVariantData variant) {
        this.variant = variant;
    }
}
