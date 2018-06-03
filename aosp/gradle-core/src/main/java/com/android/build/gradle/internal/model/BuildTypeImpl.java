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
import com.android.annotations.Nullable;
import com.android.builder.model.BuildType;
import com.android.builder.model.SigningConfig;

import java.io.Serializable;

/**
 * Implementation of BuildType that is serializable. Objects used in the DSL cannot be
 * serialized.
 */
class BuildTypeImpl extends BaseConfigImpl implements BuildType, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private boolean debuggable;
    private boolean testCoverageEnabled;
    private boolean jniDebuggable;
    private boolean pseudoLocalesEnabled;
    private boolean renderscriptDebuggable;
    private int renderscriptOptimLevel;
    private String applicationIdSuffix;
    private String versionNameSuffix;
    private boolean minifyEnabled;
    private boolean zipAlignEnabled;
    private boolean embedMicroApp;

    @NonNull
    static BuildTypeImpl cloneBuildType(@NonNull BuildType buildType) {
        BuildTypeImpl clonedBuildType = new BuildTypeImpl(buildType);

        clonedBuildType.name = buildType.getName();
        clonedBuildType.debuggable = buildType.isDebuggable();
        clonedBuildType.testCoverageEnabled = buildType.isTestCoverageEnabled();
        clonedBuildType.jniDebuggable = buildType.isJniDebuggable();
        clonedBuildType.renderscriptDebuggable = buildType.isRenderscriptDebuggable();
        clonedBuildType.renderscriptOptimLevel = buildType.getRenderscriptOptimLevel();
        clonedBuildType.applicationIdSuffix = buildType.getApplicationIdSuffix();
        clonedBuildType.versionNameSuffix = buildType.getVersionNameSuffix();
        clonedBuildType.minifyEnabled = buildType.isMinifyEnabled();
        clonedBuildType.zipAlignEnabled = buildType.isZipAlignEnabled();
        clonedBuildType.embedMicroApp = buildType.isEmbedMicroApp();
        clonedBuildType.pseudoLocalesEnabled = buildType.isPseudoLocalesEnabled();

        return clonedBuildType;
    }

    private BuildTypeImpl(@NonNull BuildType buildType) {
        super(buildType);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDebuggable() {
        return debuggable;
    }

    @Override
    public boolean isTestCoverageEnabled() {
        return testCoverageEnabled;
    }

    @Override
    public boolean isJniDebuggable() {
        return jniDebuggable;
    }

    @Override
    public boolean isRenderscriptDebuggable() {
        return renderscriptDebuggable;
    }

    @Override
    public boolean isPseudoLocalesEnabled() {
        return pseudoLocalesEnabled;
    }

    @Override
    public int getRenderscriptOptimLevel() {
        return renderscriptOptimLevel;
    }

    @Nullable
    @Override
    public String getApplicationIdSuffix() {
        return applicationIdSuffix;
    }

    @Nullable
    @Override
    public String getVersionNameSuffix() {
        return versionNameSuffix;
    }

    @Override
    public boolean isMinifyEnabled() {
        return minifyEnabled;
    }

    @Override
    public boolean isZipAlignEnabled() {
        return zipAlignEnabled;
    }

    @Override
    public boolean isEmbedMicroApp() {
        return embedMicroApp;
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return null;
    }

    @Override
    public String toString() {
        return "BuildTypeImpl{" +
                "name='" + name + '\'' +
                ", debuggable=" + debuggable +
                ", testCoverageEnabled=" + testCoverageEnabled +
                ", jniDebuggable=" + jniDebuggable +
                ", renderscriptDebuggable=" + renderscriptDebuggable +
                ", renderscriptOptimLevel=" + renderscriptOptimLevel +
                ", applicationIdSuffix='" + applicationIdSuffix + '\'' +
                ", versionNameSuffix='" + versionNameSuffix + '\'' +
                ", minifyEnabled=" + minifyEnabled +
                ", zipAlignEnabled=" + zipAlignEnabled +
                ", embedMicroApp=" + embedMicroApp +
                "} " + super.toString();
    }
}
