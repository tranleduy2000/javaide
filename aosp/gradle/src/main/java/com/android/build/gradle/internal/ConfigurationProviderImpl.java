/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class ConfigurationProviderImpl implements ConfigurationProvider {

    private final Project project;
    private final DefaultAndroidSourceSet sourceSet;

    ConfigurationProviderImpl(Project project, DefaultAndroidSourceSet sourceSet) {
        this.project = project;
        this.sourceSet = sourceSet;
    }

    @Override
    @NonNull
    public Configuration getCompileConfiguration() {
        return project.getConfigurations().getByName(sourceSet.getCompileConfigurationName());
    }

    @Override
    @NonNull
    public Configuration getPackageConfiguration() {
        return project.getConfigurations().getByName(sourceSet.getPackageConfigurationName());
    }

    @Override
    @NonNull
    public Configuration getProvidedConfiguration() {
        return project.getConfigurations().getByName(sourceSet.getProvidedConfigurationName());
    }
}

