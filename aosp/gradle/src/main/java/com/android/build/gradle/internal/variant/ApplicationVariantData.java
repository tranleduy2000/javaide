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
package com.android.build.gradle.internal.variant;

import com.android.annotations.NonNull;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;

import java.util.Collections;
import java.util.Set;

/**
 * Data about a variant that produce an application APK
 */
public class ApplicationVariantData extends ApkVariantData {
    private Set<String> compatibleScreens = null;

    public ApplicationVariantData(
            @NonNull AndroidConfig androidConfig,
            @NonNull GradleVariantConfiguration config,
            @NonNull TaskManager taskManager) {
        super(androidConfig, taskManager, config);
    }

    @NonNull
    public Set<String> getCompatibleScreens() {
        if (compatibleScreens == null) {
            return Collections.emptySet();
        }

        return compatibleScreens;
    }

    public void setCompatibleScreens(Set<String> compatibleScreens) {
        this.compatibleScreens = compatibleScreens;
    }

}
