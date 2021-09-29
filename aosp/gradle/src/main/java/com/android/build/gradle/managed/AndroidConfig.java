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
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.PreprocessingOptions;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.model.AndroidComponentModelSourceSet;
import com.android.builder.core.LibraryRequest;
import com.android.sdklib.repository.FullRevision;

import org.gradle.api.Action;
import org.gradle.model.ModelMap;

import java.util.Collection;

/**
 * Component model for all Android plugin.
 */
public interface AndroidConfig {

    /**
     * Build tool version
     */
    String getBuildToolsVersion();

    void setBuildToolsVersion(String buildToolsVersion);

    /**
     * Compile SDK version
     */
    String getCompileSdkVersion();

    void setCompileSdkVersion(String compileSdkVersion);

    /**
     * Build tool revisions
     */

    FullRevision getBuildToolsRevision();

    void setBuildToolsRevision(FullRevision fullRevision);

    /**
     * Default config, shared by all flavors.
     */
    ProductFlavor getDefaultConfig();

    /**
     * Name of the variant to publish
     */
    String getDefaultPublishConfig();

    void setDefaultPublishConfig(String defaultPublishConfig);

    /**
     * Whether to also publish non-default variants
     */
    Boolean getPublishNonDefault();

    void setPublishNonDefault(Boolean publishNonDefault);

    /**
     * Filter to determine which variants to build
     */

    Action<VariantFilter> getVariantFilter();

    void setVariantFilter(Action<VariantFilter> filter);

    /**
     * A prefix to be used when creating new resources. Used by Studio
     */
    String getResourcePrefix();

    void setResourcePrefix(String resourcePrefix);

    /**
     * Whether to generate pure splits or multi apk
     */
    Boolean getGeneratePureSplits();

    void setGeneratePureSplits(Boolean generateSplits);

    /**
     * Whether to preprocess resources
     */

    PreprocessingOptions getPreProcessingOptions();

    void setPreProcessingOptions(PreprocessingOptions preprocessingOptions);

    /**
     * Build types used by this project.
     */
    ModelMap<BuildType> getBuildTypes();

    /**
     * All product flavors used by this project.
     */
    ModelMap<ProductFlavor> getProductFlavors();

    /**
     * Signing configs used by this project.
     */
    ModelMap<SigningConfig> getSigningConfigs();


    AndroidComponentModelSourceSet getSources();

    void setSources(AndroidComponentModelSourceSet sources);

    /**
     * Options for aapt, tool for packaging resources.
     */

    AaptOptions getAaptOptions();

    void setAaptOptions(AaptOptions aaptOptions);

    /**
     * Compile options
     */

    CompileOptions getCompileOptions();

    void setCompileOptions(CompileOptions compileOptions);

    /**
     * Dex options.
     */

    DexOptions getDexOptions();

    void setDexOptions(DexOptions dexOptions);

    /**
     * Lint options.
     */

    LintOptions getLintOptions();

    void setLintOptions(LintOptions lintOptions);

    /**
     * Packaging options.
     */

    PackagingOptions getPackagingOptions();

    void setPackagingOptions(PackagingOptions packagingOptions);

    /**
     * APK splits
     */

    Splits getSplits();

    void setSplits(Splits splits);


    Collection<LibraryRequest> getLibraryRequests();

    void setLibraryRequests(Collection<LibraryRequest> libraryRequests);
}
