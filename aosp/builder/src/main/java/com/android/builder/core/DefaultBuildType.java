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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.internal.BaseConfigImpl;
import com.android.builder.model.BuildType;
import com.android.builder.model.SigningConfig;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class DefaultBuildType extends BaseConfigImpl implements BuildType {
    private static final long serialVersionUID = 1L;

    private final String mName;
    private boolean mDebuggable = false;
    private boolean mPseudoLocalesEnabled = false;
    private boolean mJniDebuggable = false;
    private String mApplicationIdSuffix = null;
    private String mVersionNameSuffix = null;
    private boolean mMinifyEnabled = false;
    private SigningConfig mSigningConfig = null;

    private boolean mZipAlignEnabled = true;

    public DefaultBuildType(@NonNull String name) {
        mName = name;
    }

    public DefaultBuildType initWith(DefaultBuildType that) {
        _initWith(that);

        setDebuggable(that.isDebuggable());
        setApplicationIdSuffix(that.getApplicationIdSuffix());
        setVersionNameSuffix(that.getVersionNameSuffix());
        setMinifyEnabled(that.isMinifyEnabled());
        setZipAlignEnabled(that.isZipAlignEnabled());
        setSigningConfig(that.getSigningConfig());
        setPseudoLocalesEnabled(that.isPseudoLocalesEnabled());

        return this;
    }

    /**
     * Name of this build type.
     */
    @Override
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * Whether this build type should generate a debuggable apk.
     */
    @NonNull
    public BuildType setDebuggable(boolean debuggable) {
        mDebuggable = debuggable;
        return this;
    }

    /**
     * Whether this build type should generate a debuggable apk.
     */
    @Override
    public boolean isDebuggable() {
        // Accessing coverage data requires a debuggable package.
        return mDebuggable;
    }

    /**
     * Whether to generate pseudo locale in the APK.
     * <p>
     * <p>If enabled, 2 fake pseudo locales (en-XA and ar-XB) will be added to the APK to help
     * test internationalization support in the app.
     */
    @Override
    public boolean isPseudoLocalesEnabled() {
        return mPseudoLocalesEnabled;
    }

    public void setPseudoLocalesEnabled(boolean pseudoLocalesEnabled) {
        mPseudoLocalesEnabled = pseudoLocalesEnabled;
    }

    /**
     * Whether this build type is configured to generate an APK with debuggable native code.
     */
    @NonNull
    public BuildType setJniDebuggable(boolean jniDebugBuild) {
        mJniDebuggable = jniDebugBuild;
        return this;
    }

    /**
     * Whether this build type is configured to generate an APK with debuggable native code.
     */
    @Override
    public boolean isJniDebuggable() {
        return mJniDebuggable;
    }

    /**
     * Application id suffix applied to this build type.
     */
    @NonNull
    public BuildType setApplicationIdSuffix(@Nullable String applicationIdSuffix) {
        mApplicationIdSuffix = applicationIdSuffix;
        return this;
    }

    /**
     * Application id suffix applied to this build type.
     */
    @Override
    @Nullable
    public String getApplicationIdSuffix() {
        return mApplicationIdSuffix;
    }

    /**
     * Version name suffix.
     */
    @NonNull
    public BuildType setVersionNameSuffix(@Nullable String versionNameSuffix) {
        mVersionNameSuffix = versionNameSuffix;
        return this;
    }

    /**
     * Version name suffix.
     */
    @Override
    @Nullable
    public String getVersionNameSuffix() {
        return mVersionNameSuffix;
    }

    /**
     * Whether Minify is enabled for this build type.
     */
    @NonNull
    public BuildType setMinifyEnabled(boolean enabled) {
        mMinifyEnabled = enabled;
        return this;
    }

    /**
     * Whether Minify is enabled for this build type.
     */
    @Override
    public boolean isMinifyEnabled() {
        return mMinifyEnabled;
    }


    /**
     * Whether zipalign is enabled for this build type.
     */
    @NonNull
    public BuildType setZipAlignEnabled(boolean zipAlign) {
        mZipAlignEnabled = zipAlign;
        return this;
    }

    /**
     * Whether zipalign is enabled for this build type.
     */
    @Override
    public boolean isZipAlignEnabled() {
        return mZipAlignEnabled;
    }

    /**
     * Sets the signing configuration. e.g.: {@code signingConfig signingConfigs.myConfig}
     */
    @NonNull
    public BuildType setSigningConfig(@Nullable SigningConfig signingConfig) {
        mSigningConfig = signingConfig;
        return this;
    }

    /**
     * Sets the signing configuration. e.g.: {@code signingConfig signingConfigs.myConfig}
     */
    @Override
    @Nullable
    public SigningConfig getSigningConfig() {
        return mSigningConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DefaultBuildType buildType = (DefaultBuildType) o;

        return Objects.equal(mName, buildType.mName) &&
                mDebuggable == buildType.mDebuggable &&
                mJniDebuggable == buildType.mJniDebuggable &&
                mPseudoLocalesEnabled == buildType.mPseudoLocalesEnabled &&
                mMinifyEnabled == buildType.mMinifyEnabled &&
                mZipAlignEnabled == buildType.mZipAlignEnabled &&
                Objects.equal(mApplicationIdSuffix, buildType.mApplicationIdSuffix) &&
                Objects.equal(mVersionNameSuffix, buildType.mVersionNameSuffix) &&
                Objects.equal(mSigningConfig, buildType.mSigningConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                super.hashCode(),
                mName,
                mDebuggable,
                mJniDebuggable,
                mPseudoLocalesEnabled,
                mApplicationIdSuffix,
                mVersionNameSuffix,
                mMinifyEnabled,
                mZipAlignEnabled,
                mSigningConfig);
    }

    @Override
    @NonNull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", mName)
                .add("debuggable", mDebuggable)
                .add("jniDebuggable", mJniDebuggable)
                .add("pseudoLocalesEnabled", mPseudoLocalesEnabled)
                .add("applicationIdSuffix", mApplicationIdSuffix)
                .add("versionNameSuffix", mVersionNameSuffix)
                .add("minifyEnabled", mMinifyEnabled)
                .add("zipAlignEnabled", mZipAlignEnabled)
                .add("signingConfig", mSigningConfig)
                .add("mBuildConfigFields", getBuildConfigFields())
                .add("mResValues", getResValues())
                .add("mProguardFiles", getProguardFiles())
                .add("mConsumerProguardFiles", getConsumerProguardFiles())
                .add("mManifestPlaceholders", getManifestPlaceholders())
                .toString();
    }
}
