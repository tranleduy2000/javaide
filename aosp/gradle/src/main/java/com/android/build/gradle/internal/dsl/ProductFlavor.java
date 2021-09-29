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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.LoggingUtil;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.BuilderConstants;
import com.android.builder.core.DefaultApiVersion;
import com.android.builder.core.DefaultProductFlavor;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ClassField;
import com.google.common.base.Strings;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.internal.reflect.Instantiator;

import java.util.Collection;

/**
 * DSL object used to configure product flavors.
 */
public class ProductFlavor extends DefaultProductFlavor implements CoreProductFlavor {

    @NonNull
    protected final Project project;

    @NonNull
    protected final Logger logger;

    public ProductFlavor(@NonNull String name,
                         @NonNull Project project,
                         @NonNull Instantiator instantiator,
                         @NonNull Logger logger) {
        super(name);
        this.project = project;
        this.logger = logger;
    }

    @Nullable
    private static ApiVersion getApiVersion(@Nullable String value) {
        if (!Strings.isNullOrEmpty(value)) {
            if (Character.isDigit(value.charAt(0))) {
                try {
                    int apiLevel = Integer.valueOf(value);
                    return new DefaultApiVersion(apiLevel);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("'" + value + "' is not a valid API level. ", e);
                }
            }

            return new DefaultApiVersion(value);
        }

        return null;
    }

    public void setMinSdkVersion(int minSdkVersion) {
        setMinSdkVersion(new DefaultApiVersion(minSdkVersion));
    }

    /**
     * Sets minimum SDK version.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html">
     * uses-sdk element documentation</a>.
     */
    public void minSdkVersion(int minSdkVersion) {
        setMinSdkVersion(minSdkVersion);
    }

    public void setMinSdkVersion(@Nullable String minSdkVersion) {
        setMinSdkVersion(getApiVersion(minSdkVersion));
    }

    /**
     * Sets minimum SDK version.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html">
     * uses-sdk element documentation</a>.
     */
    public void minSdkVersion(@Nullable String minSdkVersion) {
        setMinSdkVersion(minSdkVersion);
    }

    @NonNull
    public com.android.builder.model.ProductFlavor setTargetSdkVersion(int targetSdkVersion) {
        setTargetSdkVersion(new DefaultApiVersion(targetSdkVersion));
        return this;
    }

    /**
     * Sets the target SDK version to the given value.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html">
     * uses-sdk element documentation</a>.
     */
    public void targetSdkVersion(int targetSdkVersion) {
        setTargetSdkVersion(targetSdkVersion);
    }

    public void setTargetSdkVersion(@Nullable String targetSdkVersion) {
        setTargetSdkVersion(getApiVersion(targetSdkVersion));
    }

    /**
     * Sets the target SDK version to the given value.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html">
     * uses-sdk element documentation</a>.
     */
    public void targetSdkVersion(@Nullable String targetSdkVersion) {
        setTargetSdkVersion(targetSdkVersion);
    }

    /**
     * Sets the maximum SDK version to the given value.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html">
     * uses-sdk element documentation</a>.
     */
    public void maxSdkVersion(int targetSdkVersion) {
        setMaxSdkVersion(targetSdkVersion);
    }

    /**
     * Signing config used by this product flavor.
     */
    @Override
    @Nullable
    public SigningConfig getSigningConfig() {
        return (SigningConfig) super.getSigningConfig();
    }

// -- DSL Methods. TODO remove once the instantiator does what I expect it to do.

    /**
     * Adds a new field to the generated BuildConfig class.
     * <p>
     * The field is generated as:
     * <p>
     * <type> <name> = <value>;
     * <p>
     * This means each of these must have valid Java content. If the type is a String, then the
     * value should include quotes.
     *
     * @param type  the type of the field
     * @param name  the name of the field
     * @param value the value of the field
     */
    public void buildConfigField(
            @NonNull String type,
            @NonNull String name,
            @NonNull String value) {
        ClassField alreadyPresent = getBuildConfigFields().get(name);
        if (alreadyPresent != null) {
            String flavorName = getName();
            if (BuilderConstants.MAIN.equals(flavorName)) {
                logger.info(
                        "DefaultConfig: buildConfigField '{}' value is being replaced: {} -> {}",
                        name, alreadyPresent.getValue(), value);
            } else {
                logger.info(
                        "ProductFlavor({}): buildConfigField '{}' "
                                + "value is being replaced: {} -> {}",
                        flavorName, name, alreadyPresent.getValue(), value);
            }
        }
        addBuildConfigField(AndroidBuilder.createClassField(type, name, value));
    }

    /**
     * Adds a new generated resource.
     * <p>
     * <p>This is equivalent to specifying a resource in res/values.
     * <p>
     * <p>See <a href="http://developer.android.com/guide/topics/resources/available-resources.html">Resource Types</a>.
     *
     * @param type  the type of the resource
     * @param name  the name of the resource
     * @param value the value of the resource
     */
    public void resValue(
            @NonNull String type,
            @NonNull String name,
            @NonNull String value) {
        ClassField alreadyPresent = getResValues().get(name);
        if (alreadyPresent != null) {
            String flavorName = getName();
            if (BuilderConstants.MAIN.equals(flavorName)) {
                logger.info(
                        "DefaultConfig: resValue '{}' value is being replaced: {} -> {}",
                        name, alreadyPresent.getValue(), value);
            } else {
                logger.info(
                        "ProductFlavor({}): resValue '{}' value is being replaced: {} -> {}",
                        flavorName, name, alreadyPresent.getValue(), value);
            }
        }
        addResValue(AndroidBuilder.createClassField(type, name, value));
    }

    /**
     * Adds a new ProGuard configuration file.
     * <p>
     * <p><code>proguardFile getDefaultProguardFile('proguard-android.txt')</code></p>
     * <p>
     * <p>There are 2 default rules files
     * <ul>
     * <li>proguard-android.txt
     * <li>proguard-android-optimize.txt
     * </ul>
     * <p>They are located in the SDK. Using <code>getDefaultProguardFile(String filename)</code> will return the
     * full path to the files. They are identical except for enabling optimizations.
     */
    public void proguardFile(@NonNull Object proguardFile) {
        getProguardFiles().add(project.file(proguardFile));
    }

    /**
     * Adds new ProGuard configuration files.
     * <p>
     * <p>There are 2 default rules files
     * <ul>
     * <li>proguard-android.txt
     * <li>proguard-android-optimize.txt
     * </ul>
     * <p>They are located in the SDK. Using <code>getDefaultProguardFile(String filename)</code> will return the
     * full path to the files. They are identical except for enabling optimizations.
     */
    public void proguardFiles(@NonNull Object... proguardFileArray) {
        getProguardFiles().addAll(project.files(proguardFileArray).getFiles());
    }

    /**
     * Sets the ProGuard configuration files.
     * <p>
     * <p>There are 2 default rules files
     * <ul>
     * <li>proguard-android.txt
     * <li>proguard-android-optimize.txt
     * </ul>
     * <p>They are located in the SDK. Using <code>getDefaultProguardFile(String filename)</code> will return the
     * full path to the files. They are identical except for enabling optimizations.
     */
    public void setProguardFiles(@NonNull Iterable<?> proguardFileIterable) {
        getProguardFiles().clear();
        for (Object proguardFile : proguardFileIterable) {
            getProguardFiles().add(project.file(proguardFile));
        }
    }

    public void consumerProguardFiles(Object... proguardFileArray) {
        getConsumerProguardFiles().addAll(project.files(proguardFileArray).getFiles());
    }

    /**
     * Specifies a proguard rule file to be included in the published AAR.
     * <p>
     * <p>This proguard rule file will then be used by any application project that consume the AAR
     * (if proguard is enabled).
     * <p>
     * <p>This allows AAR to specify shrinking or obfuscation exclude rules.
     * <p>
     * <p>This is only valid for Library project. This is ignored in Application project.
     */
    public void setConsumerProguardFiles(@NonNull Iterable<?> proguardFileIterable) {
        getConsumerProguardFiles().clear();
        for (Object proguardFile : proguardFileIterable) {
            getConsumerProguardFiles().add(project.file(proguardFile));
        }
    }

    /**
     * Adds a resource configuration filter.
     * <p>
     * <p>If a qualifier value is passed, then all other resources using a qualifier of the same type
     * but of different value will be ignored from the final packaging of the APK.
     * <p>
     * <p>For instance, specifying 'hdpi', will ignore all resources using mdpi, xhdpi, etc...
     */
    public void resConfig(@NonNull String config) {
        addResourceConfiguration(config);
    }

    /**
     * Adds several resource configuration filters.
     * <p>
     * <p>If a qualifier value is passed, then all other resources using a qualifier of the same type
     * but of different value will be ignored from the final packaging of the APK.
     * <p>
     * <p>For instance, specifying 'hdpi', will ignore all resources using mdpi, xhdpi, etc...
     */
    public void resConfigs(@NonNull String... config) {
        addResourceConfigurations(config);
    }

    /**
     * Adds several resource configuration filters.
     * <p>
     * <p>If a qualifier value is passed, then all other resources using a qualifier of the same type
     * but of different value will be ignored from the final packaging of the APK.
     * <p>
     * <p>For instance, specifying 'hdpi', will ignore all resources using mdpi, xhdpi, etc...
     */
    public void resConfigs(@NonNull Collection<String> config) {
        addResourceConfigurations(config);
    }

    /**
     * Name of the dimension this product flavor belongs to. Has been replaced by
     * <code>dimension</code>
     */
    @Deprecated
    public String getFlavorDimension() {
        LoggingUtil.displayDeprecationWarning(logger, project,
                "'flavorDimension' will be removed by Android Gradle Plugin 2.0, " +
                        "it has been replaced by 'dimension'.");
        return getDimension();
    }

    @Deprecated
    public void setFlavorDimension(String dimension) {
        LoggingUtil.displayDeprecationWarning(logger, project,
                "'flavorDimension' will be removed by Android Gradle Plugin 2.0, " +
                        "it has been replaced by 'dimension'.");
        setDimension(dimension);
    }

    public void jarJarRuleFile(Object file) {
        getJarJarRuleFiles().add(project.file(file));
    }

    public void jarJarRuleFiles(Object... files) {
        getJarJarRuleFiles().clear();
        for (Object file : files) {
            getJarJarRuleFiles().add(project.file(file));
        }
    }
}