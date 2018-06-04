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

package com.android.build.gradle.managed;

import com.android.build.gradle.api.VariantFilter;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.coverage.JacocoExtension;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.AdbOptions;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.PreprocessingOptions;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.build.gradle.model.AndroidComponentModelSourceSet;
import com.android.builder.core.LibraryRequest;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestServer;
import com.android.sdklib.repository.FullRevision;

import org.gradle.api.Action;
import org.gradle.model.Managed;
import org.gradle.model.ModelMap;
import org.gradle.model.Unmanaged;

import java.util.Collection;
import java.util.List;

import groovy.lang.Closure;

/**
 * Component model for all Android plugin.
 */
@Managed
public interface AndroidConfig {

    /** Build tool version */
    String getBuildToolsVersion();
    void setBuildToolsVersion(String buildToolsVersion);

    /** Compile SDK version */
    String getCompileSdkVersion();
    void setCompileSdkVersion(String compileSdkVersion);

    /** Build tool revisions */
    @Unmanaged
    FullRevision getBuildToolsRevision();
    void setBuildToolsRevision(FullRevision fullRevision);

    /** Default config, shared by all flavors. */
    ProductFlavor getDefaultConfig();

    /** List of device providers */
    @Unmanaged
    List<DeviceProvider> getDeviceProviders();
    void setDeviceProviders(List<DeviceProvider> providers);

    /** List of remote CI servers */
    @Unmanaged
    List<TestServer> getTestServers();
    void setTestServers(List<TestServer> providers);

    /** Name of the variant to publish */
    String getDefaultPublishConfig();
    void setDefaultPublishConfig(String defaultPublishConfig);

    /** Whether to also publish non-default variants */
    Boolean getPublishNonDefault();
    void setPublishNonDefault(Boolean publishNonDefault);

    /** Filter to determine which variants to build */
    @Unmanaged
    Action<VariantFilter> getVariantFilter();
    void setVariantFilter(Action<VariantFilter> filter);

    /** A prefix to be used when creating new resources. Used by Studio */
    String getResourcePrefix();
    void setResourcePrefix(String resourcePrefix);

    /** Whether to generate pure splits or multi apk */
    Boolean getGeneratePureSplits();
    void setGeneratePureSplits(Boolean generateSplits);

    /** Whether to preprocess resources */
    @Unmanaged
    PreprocessingOptions getPreProcessingOptions();
    void setPreProcessingOptions(PreprocessingOptions preprocessingOptions);

    /** Build types used by this project. */
    ModelMap<BuildType> getBuildTypes();

    /** All product flavors used by this project. */
    ModelMap<ProductFlavor> getProductFlavors();

    /** Signing configs used by this project. */
    ModelMap<SigningConfig> getSigningConfigs();

    @Unmanaged
    AndroidComponentModelSourceSet getSources();
    void setSources(AndroidComponentModelSourceSet sources);

    NdkConfig getNdk();

    /** Adb options */
    @Unmanaged
    AdbOptions getAdbOptions();
    void setAdbOptions(AdbOptions adbOptions);

    /** Options for aapt, tool for packaging resources. */
    @Unmanaged
    AaptOptions getAaptOptions();
    void setAaptOptions(AaptOptions aaptOptions);

    /** Compile options */
    @Unmanaged
    CompileOptions getCompileOptions();
    void setCompileOptions(CompileOptions compileOptions);

    /** Dex options. */
    @Unmanaged
    DexOptions getDexOptions();
    void setDexOptions(DexOptions dexOptions);

    /** JaCoCo options. */
    @Unmanaged
    JacocoExtension getJacoco();
    void setJacoco(JacocoExtension jacoco);

    /** Lint options. */
    @Unmanaged
    LintOptions getLintOptions();
    void setLintOptions(LintOptions lintOptions);

    /** Packaging options. */
    @Unmanaged
    PackagingOptions getPackagingOptions();
    void setPackagingOptions(PackagingOptions packagingOptions);

    /** Options for running tests. */
    @Unmanaged
    TestOptions getTestOptions();
    void setTestOptions(TestOptions testOptions);

    /** APK splits */
    @Unmanaged
    Splits getSplits();
    void setSplits(Splits splits);

    @Unmanaged
    Collection<LibraryRequest> getLibraryRequests();
    void setLibraryRequests(Collection<LibraryRequest> libraryRequests);
}
