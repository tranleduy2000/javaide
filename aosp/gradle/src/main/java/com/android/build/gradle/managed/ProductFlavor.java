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

package com.android.build.gradle.managed;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.DimensionAware;
import com.android.builder.model.Variant;

import org.gradle.api.Named;
import org.gradle.model.Managed;
import org.gradle.model.ModelSet;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * A Managed product flavor.
 * <p>
 * TODO: Convert Unmanaged Collection to Managed type when Gradle provides ModelSet for basic class.
 */
@Managed
public interface ProductFlavor extends Named, DimensionAware {

    /**
     * Map of Build Config Fields where the key is the field name.
     *
     * @return a non-null map of class fields (possibly empty).
     */
    @NonNull
    ModelSet<ClassField> getBuildConfigFields();

    /**
     * Map of generated res values where the key is the res name.
     *
     * @return a non-null map of class fields (possibly empty).
     */
    @NonNull
    ModelSet<ClassField> getResValues();

    /**
     * Returns the collection of proguard rule files.
     * <p>
     * <p>These files are only applied to the production code.
     *
     * @return a non-null collection of files.
     * @see #getTestProguardFiles()
     */
    Set<File> getProguardFiles();

    void setProguardFiles(Set<File> files);

    /**
     * Returns the collection of proguard rule files for consumers of the library to use.
     *
     * @return a non-null collection of files.
     */
    Set<File> getConsumerProguardFiles();

    void setConsumerProguardFiles(Set<File> files);

    /**
     * Returns the collection of proguard rule files to use for the test APK.
     *
     * @return a non-null collection of files.
     */
    Set<File> getTestProguardFiles();

    void setTestProguardFiles(Set<File> files);

    /**
     * Returns the map of key value pairs for placeholder substitution in the android manifest file.
     *
     * This map will be used by the manifest merger.
     * @return the map of key value pairs.
     */
    // TODO: Add the commented fields.
    //Map<String, Object> getManifestPlaceholders();

    /**
     * Returns whether multi-dex is enabled.
     * <p>
     * This can be null if the flag is not set, in which case the default value is used.
     */
    @Nullable
    Boolean getMultiDexEnabled();

    void setMultiDexEnabled(Boolean multiDexEnabled);

    @Nullable
    File getMultiDexKeepFile();

    void setMultiDexKeepFile(File multiDexKeepFile);

    @Nullable
    File getMultiDexKeepProguard();

    void setMultiDexKeepProguard(File multiDexKeepProguard);

    /**
     * Returns the optional jarjar rule files, or empty if jarjar should be skipped.
     * <p>
     * <p>If more than one file is provided, the rule files will be merged in order with last one
     * win in case of rule redefinition.
     * <p>
     * <p>Can only be used with Jack toolchain.
     *
     * @return the optional jarjar rule file.
     */
    List<File> getJarJarRuleFiles();

    void setJarJarRuleFiles(List<File> jarJarRuleFiles);

    /**
     * Returns the flavor dimension or null if not applicable.
     */
    @Override
    @Nullable
    String getDimension();

    void setDimension(String dimension);

    /**
     * Returns the name of the product flavor. This is only the value set on this product flavor.
     * To get the final application id name, use {@link AndroidArtifact#getApplicationId()}.
     *
     * @return the application id.
     */
    @Nullable
    String getApplicationId();

    void setApplicationId(String applicationId);

    /**
     * Returns the version code associated with this flavor or null if none have been set.
     * This is only the value set on this product flavor, not necessarily the actual
     * version code used.
     *
     * @return the version code, or null if not specified
     */
    @Nullable
    Integer getVersionCode();

    void setVersionCode(Integer versionCode);

    /**
     * Returns the version name. This is only the value set on this product flavor.
     * To get the final value, use {@link Variant#getMergedFlavor()} as well as
     * {@link BuildType#getVersionNameSuffix()}
     *
     * @return the version name.
     */
    @Nullable
    String getVersionName();

    void setVersionName(String versionName);

    /**
     * Returns the minSdkVersion. This is only the value set on this product flavor.
     *
     * @return the minSdkVersion, or null if not specified
     */
    @Nullable
    ApiVersion getMinSdkVersion();

    /**
     * Returns the targetSdkVersion. This is only the value set on this product flavor.
     *
     * @return the targetSdkVersion, or null if not specified
     */
    @Nullable
    ApiVersion getTargetSdkVersion();

    /**
     * Returns the maxSdkVersion. This is only the value set on this produce flavor.
     *
     * @return the maxSdkVersion, or null if not specified
     */
    @Nullable
    Integer getMaxSdkVersion();

    void setMaxSdkVersion(Integer maxSdkVersion);


    /**
     * Returns the resource configuration for this variant.
     * <p>
     * This is the list of -c parameters for aapt.
     *
     * @return the resource configuration options.
     */
    @Nullable
    Set<String> getResourceConfigurations();

    void setResourceConfigurations(Set<String> resourceConfigurations);

    /**
     * Returns the associated signing config or null if none are set on the product flavor.
     */
    SigningConfig getSigningConfig();

    void setSigningConfig(SigningConfig signingConfig);

}
