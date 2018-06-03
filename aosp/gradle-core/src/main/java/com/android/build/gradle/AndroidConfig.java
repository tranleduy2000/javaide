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

package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.VariantFilter;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.coverage.JacocoExtension;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.AdbOptions;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.PreprocessingOptions;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.builder.core.LibraryRequest;
import com.android.builder.model.SigningConfig;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestServer;
import com.android.sdklib.repository.FullRevision;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

import java.util.Collection;
import java.util.List;

/**
 * User configuration settings for all android plugins.
 */
public interface AndroidConfig {

    /** Build tool version */
    String getBuildToolsVersion();

    /** Compile SDK version */
    String getCompileSdkVersion();

    /** Build tool revisions */
    FullRevision getBuildToolsRevision();

    /** Name of the variant to publish */
    String getDefaultPublishConfig();

    /** Whether to also publish non-default variants */
    boolean getPublishNonDefault();

    /** Filter to determine which variants to build */
    Action<VariantFilter> getVariantFilter();

    /** Adb options */
    AdbOptions getAdbOptions();

    /** A prefix to be used when creating new resources. Used by Studio */
    String getResourcePrefix();

    /** List of flavor dimensions */
    List<String> getFlavorDimensionList();

    /** Whether to generate pure splits or multi apk */
    boolean getGeneratePureSplits();

    /** Preprocessing Options */
    PreprocessingOptions getPreprocessingOptions();

    @Deprecated
    boolean getEnforceUniquePackageName();

    /** Default config, shared by all flavors. */
    CoreProductFlavor getDefaultConfig();

    /** Options for aapt, tool for packaging resources. */
    AaptOptions getAaptOptions();

    /** Compile options */
    CompileOptions getCompileOptions();

    /** Dex options. */
    DexOptions getDexOptions();

    /** JaCoCo options. */
    JacocoExtension getJacoco();

    /** Lint options. */
    LintOptions getLintOptions();

    /** Packaging options. */
    PackagingOptions getPackagingOptions();

    /** APK splits */
    Splits getSplits();

    /** Options for running tests. */
    TestOptions getTestOptions();

    /** List of device providers */
    @NonNull
    List<DeviceProvider> getDeviceProviders();

    /** List of remote CI servers */
    @NonNull
    List<TestServer> getTestServers();

    /** All product flavors used by this project. */
    Collection<? extends CoreProductFlavor> getProductFlavors();

    /** Build types used by this project. */
    Collection<? extends CoreBuildType> getBuildTypes();

    /** Signing configs used by this project. */
    Collection<? extends SigningConfig> getSigningConfigs();

    /** Source sets for all variants */
    NamedDomainObjectContainer<AndroidSourceSet> getSourceSets();

    /** Whether to package build config class file */
    Boolean getPackageBuildConfig();

    Collection<LibraryRequest> getLibraryRequests();
}
