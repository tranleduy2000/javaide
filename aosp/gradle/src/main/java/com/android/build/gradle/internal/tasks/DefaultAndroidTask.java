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

package com.android.build.gradle.internal.tasks;

import com.android.annotations.Nullable;
import com.android.builder.Version;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;

public class DefaultAndroidTask extends DefaultTask {

    @Nullable
    private String variantName;

    @Nullable
    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(@Nullable String variantName) {
        this.variantName = variantName;
    }

    /**
     * Force tasks to be re-run if the Android plugin version changes.
     * @return the plugin version, of the form "x.y.z"
     */
    @Input
    public String getAndroidGradlePluginVersion() {
        return Version.ANDROID_GRADLE_PLUGIN_VERSION;
    }
}
