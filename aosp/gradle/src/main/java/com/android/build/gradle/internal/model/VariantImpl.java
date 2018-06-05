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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.Variant;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of Variant that is serializable.
 */
class VariantImpl implements Variant, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final String name;
    @NonNull
    private final String displayName;
    @NonNull
    private final String buildTypeName;
    @NonNull
    private final List<String> productFlavorNames;
    @NonNull
    private final ProductFlavor mergedFlavor;
    @NonNull
    private final AndroidArtifact mainArtifactInfo;
    @NonNull
    private final Collection<AndroidArtifact> extraAndroidArtifacts;
    @NonNull
    private final Collection<JavaArtifact> extraJavaArtifacts;

    VariantImpl(@NonNull String name,
                @NonNull String displayName,
                @NonNull String buildTypeName,
                @NonNull List<String> productFlavorNames,
                @NonNull ProductFlavorImpl mergedFlavor,
                @NonNull AndroidArtifact mainArtifactInfo,
                @NonNull Collection<AndroidArtifact> extraAndroidArtifacts,
                @NonNull Collection<JavaArtifact> extraJavaArtifacts) {
        this.name = name;
        this.displayName = displayName;
        this.buildTypeName = buildTypeName;
        this.productFlavorNames = productFlavorNames;
        this.mergedFlavor = mergedFlavor;
        this.mainArtifactInfo = mainArtifactInfo;
        this.extraAndroidArtifacts = extraAndroidArtifacts;
        this.extraJavaArtifacts = extraJavaArtifacts;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @NonNull
    public String getBuildType() {
        return buildTypeName;
    }

    @Override
    @NonNull
    public List<String> getProductFlavors() {
        return productFlavorNames;
    }

    @Override
    @NonNull
    public ProductFlavor getMergedFlavor() {
        return mergedFlavor;
    }

    @NonNull
    @Override
    public AndroidArtifact getMainArtifact() {
        return mainArtifactInfo;
    }

    @NonNull
    @Override
    public Collection<AndroidArtifact> getExtraAndroidArtifacts() {
        return extraAndroidArtifacts;
    }

    @NonNull
    @Override
    public Collection<JavaArtifact> getExtraJavaArtifacts() {
        return extraJavaArtifacts;
    }
}
