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

package com.android.build.gradle.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.tasks.Dex;
import com.android.builder.model.SigningConfig;

import org.gradle.api.DefaultTask;

import java.io.File;
import java.util.Collection;

/**
 * A Build variant and all its public data.
 */
public interface ApkVariant extends BaseVariant {

    /**
     * Return the app versionCode. Even the value is not found, then 1 is returned as this
     * is the implicit value that the platform would use.
     *
     * If not output define its own variant override then this is used for all outputs.
     */
    int getVersionCode();

    /**
     * Return the app versionName or null if none found.
     */
    @Nullable
    String getVersionName();

    /**
     * Returns the {@link SigningConfig} for this build variant,
     * if one has been specified.
     */
    @Nullable
    SigningConfig getSigningConfig();

    /**
     * Returns true if this variant has the information it needs to create a signed APK.
     */
    boolean isSigningReady();

    /**
     * Returns the Dex task.
     */
    @Nullable
    Dex getDex();

    /**
     * Returns the list of jar files that are on the compile classpath. This does not include
     * the runtime.
     */
    @NonNull
    Collection<File> getCompileLibraries();

    /**
     * Returns the list of jar files that are packaged in the APK.
     */
    @NonNull
    Collection<File> getApkLibraries();

    /**
     * Returns the install task for the variant.
     */
    @Nullable
    DefaultTask getInstall();

    /**
     * Returns the uninstallation task.
     *
     * For non-library project this is always true even if the APK is not created because
     * signing isn't setup.
     */
    @Nullable
    DefaultTask getUninstall();
}
