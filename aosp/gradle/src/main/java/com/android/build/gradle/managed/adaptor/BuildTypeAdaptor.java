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

package com.android.build.gradle.managed.adaptor;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.managed.BuildType;
import com.android.builder.internal.ClassFieldImpl;
import com.android.builder.model.ClassField;
import com.android.builder.model.SigningConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An adaptor to convert a BuildType to a CoreBuildType.
 */
public class BuildTypeAdaptor implements CoreBuildType {
    @NonNull
    private final BuildType buildType;

    public BuildTypeAdaptor(@NonNull BuildType buildType) {
        this.buildType = buildType;
    }

    @NonNull
    @Override
    public String getName() {
        return buildType.getName();
    }

    @NonNull
    @Override
    public Map<String, ClassField> getBuildConfigFields() {
        ImmutableMap.Builder<String, ClassField> builder = ImmutableMap.builder();
        for (com.android.build.gradle.managed.ClassField cf : buildType.getBuildConfigFields()) {
            builder.put(
                    cf.getName(),
                    new ClassFieldImpl(
                            cf.getType(),
                            cf.getName(),
                            cf.getValue(),
                            MoreObjects.firstNonNull(cf.getAnnotations(), ImmutableSet.<String>of()),
                            MoreObjects.firstNonNull(cf.getDocumentation(), "")));
        }
        return builder.build();
    }

    @NonNull
    @Override
    public Map<String, ClassField> getResValues() {
        ImmutableMap.Builder<String, ClassField> builder = ImmutableMap.builder();
        for (com.android.build.gradle.managed.ClassField cf : buildType.getResValues()) {
            builder.put(
                    cf.getName(),
                    new ClassFieldImpl(
                            cf.getType(),
                            cf.getName(),
                            cf.getValue(),
                            MoreObjects.firstNonNull(cf.getAnnotations(), ImmutableSet.<String>of()),
                            MoreObjects.firstNonNull(cf.getDocumentation(), "")));
        }
        return builder.build();
    }

    @NonNull
    @Override
    public Collection<File> getProguardFiles() {
        return buildType.getProguardFiles();
    }

    @NonNull
    @Override
    public Collection<File> getConsumerProguardFiles() {
        return buildType.getConsumerProguardFiles();
    }

    @NonNull
    @Override
    public Collection<File> getTestProguardFiles() {
        return buildType.getTestProguardFiles();
    }

    @NonNull
    @Override
    public Map<String, Object> getManifestPlaceholders() {
        // TODO: To be implemented
        return Maps.newHashMap();
    }

    @Nullable
    @Override
    public Boolean getMultiDexEnabled() {
        return null;
    }

    @Nullable
    @Override
    public File getMultiDexKeepFile() {
        return null;
    }

    @Nullable
    @Override
    public File getMultiDexKeepProguard() {
        return null;
    }

    @Override
    public boolean isDebuggable() {
        return buildType.getDebuggable();
    }

    @Override
    public boolean isJniDebuggable() {
        return false;
    }

    @Override
    public boolean isPseudoLocalesEnabled() {
        return buildType.getPseudoLocalesEnabled();
    }

    @Nullable
    @Override
    public String getApplicationIdSuffix() {
        return buildType.getApplicationIdSuffix();
    }

    @Nullable
    @Override
    public String getVersionNameSuffix() {
        return buildType.getVersionNameSuffix();
    }

    @Override
    public boolean isMinifyEnabled() {
        return buildType.getMinifyEnabled();
    }

    @Override
    public boolean isZipAlignEnabled() {
        return buildType.getZipAlignEnabled();
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return buildType.getSigningConfig() == null ? null : new SigningConfigAdaptor(buildType.getSigningConfig());
    }

    @Override
    public boolean isShrinkResources() {
        return buildType.getShrinkResources();
    }

    @NonNull
    @Override
    public List<File> getJarJarRuleFiles() {
        return buildType.getJarJarRuleFiles();
    }
}
