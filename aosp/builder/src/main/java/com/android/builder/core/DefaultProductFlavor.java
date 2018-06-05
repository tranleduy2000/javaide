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
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The configuration of a product flavor.
 *
 * This is also used to describe the default configuration of all builds, even those that
 * do not contain any flavors.
 */
public class DefaultProductFlavor extends BaseConfigImpl implements ProductFlavor {
    private static final long serialVersionUID = 1L;

    private final String mName;
    @Nullable
    private String mDimension;
    @Nullable
    private ApiVersion mMinSdkVersion;
    @Nullable
    private ApiVersion mTargetSdkVersion;
    @Nullable
    private Integer mMaxSdkVersion;

    @Nullable
    private Integer mVersionCode;
    @Nullable
    private String mVersionName;
    @Nullable
    private String mApplicationId;

    @Nullable
    private SigningConfig mSigningConfig;
    @Nullable
    private Set<String> mResourceConfiguration;

    /**
     * Creates a ProductFlavor with a given name.
     *
     * Names can be important when dealing with flavor groups.
     * @param name the name of the flavor.
     *
     * @see BuilderConstants#MAIN
     */
    public DefaultProductFlavor(@NonNull String name) {
        mName = name;
    }

    @Override
    @NonNull
    public String getName() {
        return mName;
    }

    public void setDimension(@NonNull String dimension) {
        mDimension = dimension;
    }

    /** Name of the dimension this product flavor belongs to. */
    @Nullable
    @Override
    public String getDimension() {
        return mDimension;
    }

    /**
     * Sets the application id.
     */
    @NonNull
    public ProductFlavor setApplicationId(String applicationId) {
        mApplicationId = applicationId;
        return this;
    }

    /**
     * Returns the application ID.
     *
     * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/applicationid-vs-packagename">ApplicationId versus PackageName</a>
     */
    @Override
    @Nullable
    public String getApplicationId() {
        return mApplicationId;
    }

    /**
     * Sets the version code.
     *
     * @param versionCode the version code
     * @return the flavor object
     */
    @NonNull
    public ProductFlavor setVersionCode(Integer versionCode) {
        mVersionCode = versionCode;
        return this;
    }

    /**
     * Version code.
     *
     * <p>See <a href="http://developer.android.com/tools/publishing/versioning.html">Versioning Your Application</a>
     */
    @Override
    @Nullable
    public Integer getVersionCode() {
        return mVersionCode;
    }

    /**
     * Sets the version name.
     *
     * @param versionName the version name
     * @return the flavor object
     */
    @NonNull
    public ProductFlavor setVersionName(String versionName) {
        mVersionName = versionName;
        return this;
    }

    /**
     * Version name.
     *
     * <p>See <a href="http://developer.android.com/tools/publishing/versioning.html">Versioning Your Application</a>
     */
    @Override
    @Nullable
    public String getVersionName() {
        return mVersionName;
    }

    /**
     * Sets the minSdkVersion to the given value.
     */
    @NonNull
    public ProductFlavor setMinSdkVersion(ApiVersion minSdkVersion) {
        mMinSdkVersion = minSdkVersion;
        return this;
    }

    /**
     * Min SDK version.
     */
    @Nullable
    @Override
    public ApiVersion getMinSdkVersion() {
        return mMinSdkVersion;
    }

    /** Sets the targetSdkVersion to the given value. */
    @NonNull
    public ProductFlavor setTargetSdkVersion(@Nullable ApiVersion targetSdkVersion) {
        mTargetSdkVersion = targetSdkVersion;
        return this;
    }

    /**
     * Target SDK version.
     */
    @Nullable
    @Override
    public ApiVersion getTargetSdkVersion() {
        return mTargetSdkVersion;
    }

    @NonNull
    public ProductFlavor setMaxSdkVersion(Integer maxSdkVersion) {
        mMaxSdkVersion = maxSdkVersion;
        return this;
    }

    @Nullable
    @Override
    public Integer getMaxSdkVersion() {
        return mMaxSdkVersion;
    }


    /**
     * Signing config used by this product flavor.
     */
    @Override
    @Nullable
    public SigningConfig getSigningConfig() {
        return mSigningConfig;
    }

    /** Sets the signing configuration. e.g.: {@code signingConfig signingConfigs.myConfig} */
    @NonNull
    public ProductFlavor setSigningConfig(SigningConfig signingConfig) {
        mSigningConfig = signingConfig;
        return this;
    }

    /**
     * Adds a res config filter (for instance 'hdpi')
     */
    public void addResourceConfiguration(@NonNull String configuration) {
        if (mResourceConfiguration == null) {
            mResourceConfiguration = Sets.newHashSet();
        }

        mResourceConfiguration.add(configuration);
    }

    /**
     * Adds a res config filter (for instance 'hdpi')
     */
    public void addResourceConfigurations(@NonNull String... configurations) {
        if (mResourceConfiguration == null) {
            mResourceConfiguration = Sets.newHashSet();
        }

        mResourceConfiguration.addAll(Arrays.asList(configurations));
    }

    /**
     * Adds a res config filter (for instance 'hdpi')
     */
    public void addResourceConfigurations(@NonNull Collection<String> configurations) {
        if (mResourceConfiguration == null) {
            mResourceConfiguration = Sets.newHashSet();
        }

        mResourceConfiguration.addAll(configurations);
    }

    /**
     * Adds a res config filter (for instance 'hdpi')
     */
    @NonNull
    @Override
    public Collection<String> getResourceConfigurations() {
        if (mResourceConfiguration == null) {
            mResourceConfiguration = Sets.newHashSet();
        }

        return mResourceConfiguration;
    }

    /**
     * Merges two flavors on top of one another and returns a new object with the result.
     *
     * The behavior is that if a value is present in the overlay, then it is used, otherwise
     * we use the value from the base.
     *
     * @param base the flavor to merge on top of
     * @param overlay the flavor to apply on top of the base.
     *
     * @return a new ProductFlavor that represents the merge.
     */
    @NonNull
    static ProductFlavor mergeFlavors(@NonNull ProductFlavor base, @NonNull ProductFlavor overlay) {
        DefaultProductFlavor flavor = new DefaultProductFlavor("");

        flavor.mMinSdkVersion = chooseNotNull(
                overlay.getMinSdkVersion(),
                base.getMinSdkVersion());
        flavor.mTargetSdkVersion = chooseNotNull(
                overlay.getTargetSdkVersion(),
                base.getTargetSdkVersion());
        flavor.mMaxSdkVersion = chooseNotNull(
                overlay.getMaxSdkVersion(),
                base.getMaxSdkVersion());




        flavor.mVersionCode = chooseNotNull(overlay.getVersionCode(), base.getVersionCode());
        flavor.mVersionName = chooseNotNull(overlay.getVersionName(), base.getVersionName());

        flavor.mApplicationId = chooseNotNull(overlay.getApplicationId(), base.getApplicationId());


        flavor.mSigningConfig = chooseNotNull(
                overlay.getSigningConfig(),
                base.getSigningConfig());

        flavor.addResourceConfigurations(base.getResourceConfigurations());
        flavor.addResourceConfigurations(overlay.getResourceConfigurations());

        flavor.addManifestPlaceholders(base.getManifestPlaceholders());
        flavor.addManifestPlaceholders(overlay.getManifestPlaceholders());

        flavor.addResValues(base.getResValues());
        flavor.addResValues(overlay.getResValues());

        flavor.addBuildConfigFields(base.getBuildConfigFields());
        flavor.addBuildConfigFields(overlay.getBuildConfigFields());

        flavor.setMultiDexEnabled(chooseNotNull(
                overlay.getMultiDexEnabled(), base.getMultiDexEnabled()));

        flavor.setMultiDexKeepFile(chooseNotNull(
                overlay.getMultiDexKeepFile(), base.getMultiDexKeepFile()));

        flavor.setMultiDexKeepProguard(chooseNotNull(
                overlay.getMultiDexKeepProguard(), base.getMultiDexKeepProguard()));

        flavor.setJarJarRuleFiles(ImmutableList.<File>builder()
                .addAll(overlay.getJarJarRuleFiles())
                .addAll(base.getJarJarRuleFiles())
                .build());

        return flavor;
    }

    /**
     * Clone a given product flavor.
     *
     * @param productFlavor the flavor to clone.
     *
     * @return a new instance that is a clone of the flavor.
     */
    @NonNull
    static ProductFlavor clone(@NonNull ProductFlavor productFlavor) {
        DefaultProductFlavor flavor = new DefaultProductFlavor(productFlavor.getName());
        flavor._initWith(productFlavor);
        flavor.mDimension = productFlavor.getDimension();
        flavor.mMinSdkVersion = productFlavor.getMinSdkVersion();
        flavor.mTargetSdkVersion = productFlavor.getTargetSdkVersion();
        flavor.mMaxSdkVersion = productFlavor.getMaxSdkVersion();

        flavor.mVersionCode = productFlavor.getVersionCode();
        flavor.mVersionName = productFlavor.getVersionName();

        flavor.mApplicationId = productFlavor.getApplicationId();


        flavor.mSigningConfig = productFlavor.getSigningConfig();

        flavor.addResourceConfigurations(productFlavor.getResourceConfigurations());
        flavor.addManifestPlaceholders(productFlavor.getManifestPlaceholders());

        flavor.addResValues(productFlavor.getResValues());
        flavor.addBuildConfigFields(productFlavor.getBuildConfigFields());

        flavor.setMultiDexEnabled(productFlavor.getMultiDexEnabled());

        flavor.setMultiDexKeepFile(productFlavor.getMultiDexKeepFile());
        flavor.setMultiDexKeepProguard(productFlavor.getMultiDexKeepProguard());
        flavor.setJarJarRuleFiles(ImmutableList.copyOf(productFlavor.getJarJarRuleFiles()));

        return flavor;
    }

    private static <T> T chooseNotNull(T overlay, T base) {
        return overlay != null ? overlay : base;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DefaultProductFlavor that = (DefaultProductFlavor) o;

        return Objects.equal(mDimension, that.mDimension) &&
                Objects.equal(mApplicationId, that.mApplicationId) &&
                Objects.equal(mMaxSdkVersion, that.mMaxSdkVersion) &&
                Objects.equal(mMinSdkVersion, that.mMinSdkVersion) &&
                Objects.equal(mName, that.mName) &&
                Objects.equal(mResourceConfiguration, that.mResourceConfiguration) &&
                Objects.equal(mSigningConfig, that.mSigningConfig) &&
                Objects.equal(mTargetSdkVersion, that.mTargetSdkVersion) &&
                Objects.equal(mVersionCode, that.mVersionCode) &&
                Objects.equal(mVersionName, that.mVersionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                super.hashCode(),
                mName,
                mDimension,
                mMinSdkVersion,
                mTargetSdkVersion,
                mMaxSdkVersion,
                mVersionCode,
                mVersionName,
                mApplicationId,
                mSigningConfig,
                mResourceConfiguration);
    }

    @Override
    @NonNull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", mName)
                .add("dimension", mDimension)
                .add("minSdkVersion", mMinSdkVersion)
                .add("targetSdkVersion", mTargetSdkVersion)
                .add("versionCode", mVersionCode)
                .add("versionName", mVersionName)
                .add("applicationId", mApplicationId)
                .add("signingConfig", mSigningConfig)
                .add("resConfig", mResourceConfiguration)
                .add("mBuildConfigFields", getBuildConfigFields())
                .add("mResValues", getResValues())
                .add("mProguardFiles", getProguardFiles())
                .add("mConsumerProguardFiles", getConsumerProguardFiles())
                .add("mManifestPlaceholders", getManifestPlaceholders())
                .toString();
    }
}
