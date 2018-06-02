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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * a Build Type. This is only the configuration of the build type.
 *
 * It does not include the sources or the dependencies. Those are available on the container
 * or in the artifact info.
 *
 * @see BuildTypeContainer
 * @see AndroidArtifact#getDependencies()
 */
public interface BuildType extends BaseConfig {

    /**
     * Returns the name of the build type.
     *
     * @return the name of the build type.
     */
    @Override
    @NonNull
    String getName();

    /**
     * Returns whether the build type is configured to generate a debuggable apk.
     *
     * @return true if the apk is debuggable
     */
    boolean isDebuggable();

    /**
     * Returns whether the build type is configured to be build with support for code coverage.
     *
     * @return true if code coverage is enabled.
     */
    boolean isTestCoverageEnabled();

    /**
     * Returns whether the build type is configured to be build with support for pseudolocales.
     *
     * @return true if code coverage is enabled.
     */
    boolean isPseudoLocalesEnabled();

    /**
     * Returns whether the build type is configured to generate an apk with debuggable native code.
     *
     * @return true if the apk is debuggable
     */
    boolean isJniDebuggable();

    /**
     * Returns whether the build type is configured to generate an apk with debuggable
     * renderscript code.
     *
     * @return true if the apk is debuggable
     */
    boolean isRenderscriptDebuggable();

    /**
     * Returns the optimization level of the renderscript compilation.
     *
     * @return the optimization level.
     */
    int getRenderscriptOptimLevel();

    /**
     * Returns the application id suffix applied to this build type.
     * To get the final application id, use {@link AndroidArtifact#getApplicationId()}.
     *
     * @return the application id
     */
    @Nullable
    String getApplicationIdSuffix();

    /**
     * Returns the version name suffix.
     *
     * @return the version name suffix.
     */
    @Nullable
    String getVersionNameSuffix();

    /**
     * Returns whether minification is enabled for this build type.
     *
     * @return true if minification is enabled.
     */
    boolean isMinifyEnabled();

    /**
     * Return whether zipalign is enabled for this build type.
     *
     * @return true if zipalign is enabled.
     */
    boolean isZipAlignEnabled();

    /**
     * Returns whether the variant embeds the micro app.
     */
    boolean isEmbedMicroApp();

    /**
     * Returns the associated signing config or null if none are set on the build type.
     */
    @Nullable
    SigningConfig getSigningConfig();
}
