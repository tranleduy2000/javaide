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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * Entry point for the model of the Android Projects. This models a single module, whether
 * the module is an app project or a library project.
 */
public interface AndroidProject {
    //  Injectable properties to use with -P
    // Sent by Studio 1.0 ONLY
    String PROPERTY_BUILD_MODEL_ONLY =  "android.injected.build.model.only";
    // Sent by Studio 1.1+
    String PROPERTY_BUILD_MODEL_ONLY_ADVANCED =  "android.injected.build.model.only.advanced";

    String PROPERTY_INVOKED_FROM_IDE = "android.injected.invoked.from.ide";

    String PROPERTY_SIGNING_STORE_FILE = "android.injected.signing.store.file";
    String PROPERTY_SIGNING_STORE_PASSWORD = "android.injected.signing.store.password";
    String PROPERTY_SIGNING_KEY_ALIAS = "android.injected.signing.key.alias";
    String PROPERTY_SIGNING_KEY_PASSWORD = "android.injected.signing.key.password";
    String PROPERTY_SIGNING_STORE_TYPE = "android.injected.signing.store.type";

    String PROPERTY_APK_LOCATION = "android.injected.apk.location";

    String ARTIFACT_MAIN = "_main_";
    String ARTIFACT_ANDROID_TEST = "_android_test_";
    String ARTIFACT_UNIT_TEST = "_unit_test_";

    String FD_INTERMEDIATES = "intermediates";
    String FD_OUTPUTS = "outputs";
    String FD_GENERATED = "generated";


    /**
     * Returns the model version. This is a string in the format X.Y.Z
     *
     * @return a string containing the model version.
     */
    @NonNull
    String getModelVersion();

    /**
     * Returns the model api version.
     * <p/>
     * This is different from {@link #getModelVersion()} in a way that new model
     * version might increment model version but keep existing api. That means that
     * code which was built against particular 'api version' might be safely re-used for all
     * new model versions as long as they don't change the api.
     * <p/>
     * Every new model version is assumed to return an 'api version' value which
     * is equal or greater than the value used by the previous model version.
     *
     * @return model's api version
     */
    int getApiVersion();

    /**
     * Returns the name of the module.
     *
     * @return the name of the module.
     */
    @NonNull
    String getName();

    /**
     * Returns whether this is a library.
     * @return true for a library module.
     */
    boolean isLibrary();

    /**
     * Returns the {@link ProductFlavorContainer} for the 'main' default config.
     *
     * @return the product flavor.
     */
    @NonNull
    ProductFlavorContainer getDefaultConfig();

    /**
     * Returns a list of all the {@link BuildType} in their container.
     *
     * @return a list of build type containers.
     */
    @NonNull
    Collection<BuildTypeContainer> getBuildTypes();

    /**
     * Returns a list of all the {@link ProductFlavor} in their container.
     *
     * @return a list of product flavor containers.
     */
    @NonNull
    Collection<ProductFlavorContainer> getProductFlavors();

    /**
     * Returns a list of all the variants.
     *
     * This does not include test variant. Test variants are additional artifacts in their
     * respective variant info.
     *
     * @return a list of the variants.
     */
    @NonNull
    Collection<Variant> getVariants();

    /**
     * Returns a list of all the flavor dimensions, may be empty.
     *
     * @return a list of the flavor dimensions.
     */
    @NonNull
    Collection<String> getFlavorDimensions();

    /**
     * Returns a list of extra artifacts meta data. This does not include the main artifact.
     *
     * @return a list of extra artifacts
     */
    @NonNull
    Collection<ArtifactMetaData> getExtraArtifacts();

    /**
     * Returns the compilation target as a string. This is the full extended target hash string.
     * (see com.android.sdklib.IAndroidTarget#hashString())
     *
     * @return the target hash string
     */
    @NonNull
    String getCompileTarget();

    /**
     * Returns the boot classpath matching the compile target. This is typically android.jar plus
     * other optional libraries.
     *
     * @return a list of jar files.
     */
    @NonNull
    Collection<String> getBootClasspath();

    /**
     * Returns a list of folders or jar files that contains the framework source code.
     */
    @NonNull
    Collection<File> getFrameworkSources();

    /**
     * Returns the collection of toolchains used to create any native libraries.
     *
     * @return collection of toolchains.
     */
    @NonNull
    Collection<NativeToolchain> getNativeToolchains();

    /**
     * Returns a list of {@link SigningConfig}.
     */
    @NonNull
    Collection<SigningConfig> getSigningConfigs();

    /**
     * Returns the aapt options.
     */
    @NonNull
    AaptOptions getAaptOptions();

    /**
     * Returns the lint options.
     */
    @NonNull
    LintOptions getLintOptions();

    /**
     * Returns the dependencies that were not successfully resolved. The returned list gets
     * populated only if the system property {@link #PROPERTY_BUILD_MODEL_ONLY} has been
     * set to {@code true}.
     * <p>
     * Each value of the collection has the format group:name:version, for example:
     * com.google.guava:guava:15.0.2
     *
     * @return the dependencies that were not successfully resolved.
     * @deprecated use {@link #getSyncIssues()}
     */
    @Deprecated
    @NonNull
    Collection<String> getUnresolvedDependencies();

    /**
     * Returns issues found during sync.  The returned list gets
     * populated only if the system property {@link #PROPERTY_BUILD_MODEL_ONLY} has been
     * set to {@code true}.
     */
    @NonNull
    Collection<SyncIssue> getSyncIssues();

    /**
     * Returns the compile options for Java code.
     */
    @NonNull
    JavaCompileOptions getJavaCompileOptions();

    /**
     * Returns the build folder of this project.
     */
    @NonNull
    File getBuildFolder();

    /**
     * Returns the resource prefix to use, if any. This is an optional prefix which can
     * be set and which is used by the defaults to automatically choose new resources
     * with a certain prefix, warn if resources are not using the given prefix, etc.
     * This helps work with resources in the app namespace where there could otherwise
     * be unintentional duplicated resource names between unrelated libraries.
     *
     * @return the optional resource prefix, or null if not set
     */
    @Nullable
    String getResourcePrefix();
}
