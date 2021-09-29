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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ArtifactMetaData;

/**
 * Type of a variant.
 */
public enum VariantType {
    DEFAULT,
    LIBRARY,;

    private final String mPrefix;
    private final String mSuffix;
    private final boolean isSingleBuildType;
    private final String mArtifactName;
    private final int mArtifactType;

    /** App or library variant. */
    VariantType() {
        this.mPrefix = "";
        this.mSuffix = "";
        this.mArtifactName = AndroidProject.ARTIFACT_MAIN;
        this.mArtifactType = ArtifactMetaData.TYPE_ANDROID;
        this.isSingleBuildType = false;
    }

    /** Testing variant. */
    VariantType(
            String prefix,
            String suffix,
            boolean isSingleBuildType,
            String artifactName,
            int artifactType) {
        this.mArtifactName = artifactName;
        this.mArtifactType = artifactType;
        this.mPrefix = prefix;
        this.mSuffix = suffix;
        this.isSingleBuildType = isSingleBuildType;
    }

    /**
     * Returns prefix used for naming source directories. This is an empty string in
     * case of non-testing variants and a camel case string otherwise, e.g. "androidTest".
     */
    @NonNull
    public String getPrefix() {
        return mPrefix;
    }

    /**
     * Returns suffix used for naming Gradle tasks. This is an empty string in
     * case of non-testing variants and a camel case string otherwise, e.g. "AndroidTest".
     */
    @NonNull
    public String getSuffix() {
        return mSuffix;
    }

    /**
     * Returns the name used in the builder model for artifacts that correspond to this variant
     * type.
     */
    @NonNull
    public String getArtifactName() {
        return mArtifactName;
    }

    /**
     * Returns the artifact type used in the builder model.
     */
    public int getArtifactType() {
        return mArtifactType;
    }

    /**
     * Whether the artifact type supports only a single build type.
     */
    public boolean isSingleBuildType() {
        return isSingleBuildType;
    }
}
