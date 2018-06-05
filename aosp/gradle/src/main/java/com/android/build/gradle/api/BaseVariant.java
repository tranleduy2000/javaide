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

package com.android.build.gradle.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.NdkCompile;
import com.android.build.gradle.tasks.RenderscriptCompile;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SourceProvider;

import org.gradle.api.Task;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Collection;
import java.util.List;


/**
 * A Build variant and all its public data. This is the base class for items common to apps,
 * test apps, and libraries
 */
public interface BaseVariant {

    /**
     * Returns the name of the variant. Guaranteed to be unique.
     */
    @NonNull
    String getName();

    /**
     * Returns a description for the build variant.
     */
    @NonNull
    String getDescription();

    /**
     * Returns a subfolder name for the variant. Guaranteed to be unique.
     *
     * This is usually a mix of build type and flavor(s) (if applicable).
     * For instance this could be:
     * "debug"
     * "debug/myflavor"
     * "release/Flavor1Flavor2"
     */
    @NonNull
    String getDirName();

    /**
     * Returns the base name for the output of the variant. Guaranteed to be unique.
     */
    @NonNull
    String getBaseName();

    /**
     * Returns the flavor name of the variant. This is a concatenation of all the
     * applied flavors
     * @return the name of the flavors, or an empty string if there is not flavors.
     */
    @NonNull
    String getFlavorName();

    /**
     * Returns the variant outputs. There should always be at least one output.
     * @return a non-null list of variants.
     */
    @NonNull
    List<BaseVariantOutput> getOutputs();

    /**
     * Returns the {@link com.android.builder.core.DefaultBuildType} for this build variant.
     */
    @NonNull
    BuildType getBuildType();

    /**
     * Returns a {@link com.android.builder.core.DefaultProductFlavor} that represents the merging
     * of the default config and the flavors of this build variant.
     */
    @NonNull
    ProductFlavor getMergedFlavor();

    /**
     * Returns the list of {@link com.android.builder.core.DefaultProductFlavor} for this build variant.
     *
     * This is always non-null but could be empty.
     */
    @NonNull
    List<ProductFlavor> getProductFlavors();

    /**
     * Returns a list of sorted SourceProvider in order of ascending order, meaning, the earlier
     * items are meant to be overridden by later items.
     *
     * @return a list of source provider
     */
    @NonNull
    List<SourceProvider> getSourceSets();

    /**
     * Returns the applicationId of the variant.
     */
    @NonNull
    String getApplicationId();

    /**
     * Returns the pre-build anchor task
     */
    @NonNull
    Task getPreBuild();

    /**
     * Returns the check manifest task.
     */
    @NonNull
    Task getCheckManifest();

    /**
     * Returns the AIDL compilation task.
     */
    @NonNull
    AidlCompile getAidlCompile();

    /**
     * Returns the Renderscript compilation task.
     */
    @NonNull
    RenderscriptCompile getRenderscriptCompile();

    /**
     * Returns the resource merging task.
     */
    @Nullable
    MergeResources getMergeResources();

    /**
     * Returns the asset merging task.
     */
    @Nullable
    MergeAssets getMergeAssets();

    /**
     * Returns the BuildConfig generation task.
     */
    @Nullable
    GenerateBuildConfig getGenerateBuildConfig();

    /**
     * Returns the Java Compilation task if javac was configured to compile the source files.
     * @deprecated prefer {@link #getJavaCompiler} which always return the java compiler task
     * irrespective of which tool chain (javac or jack) used.
     */
    @Nullable
    @Deprecated
    JavaCompile getJavaCompile() throws IllegalStateException;

    /**
     * Returns the Java Compiler task which can be either javac or jack depending on the project
     * configuration.
     */
    @NonNull
    AbstractCompile getJavaCompiler();

    /**
     * Returns the NDK Compilation task.
     */
    @NonNull
    NdkCompile getNdkCompile();

    /**
     * Returns the obfuscation task. This can be null if obfuscation is not enabled.
     */
    @Nullable
    Task getObfuscation();

    /**
     * Returns the obfuscation mapping file. This can be null if obfuscation is not enabled.
     */
    @Nullable
    File getMappingFile();

    /**
     * Returns the Java resource processing task.
     */
    @NonNull
    AbstractCopyTask getProcessJavaResources();

    /**
     * Returns the assemble task for all this variant's output
     */
    @Nullable
    Task getAssemble();

    /**
     * Adds new Java source folders to the model.
     *
     * These source folders will not be used for the default build
     * system, but will be passed along the default Java source folders
     * to whoever queries the model.
     *
     * @param sourceFolders the source folders where the generated source code is.
     */
    void addJavaSourceFoldersToModel(@NonNull File... sourceFolders);

    /**
     * Adds new Java source folders to the model.
     *
     * These source folders will not be used for the default build
     * system, but will be passed along the default Java source folders
     * to whoever queries the model.
     *
     * @param sourceFolders the source folders where the generated source code is.
     */
    void addJavaSourceFoldersToModel(@NonNull Collection<File> sourceFolders);

    /**
     * Adds to the variant a task that generates Java source code.
     *
     * This will make the generate[Variant]Sources task depend on this task and add the
     * new source folders as compilation inputs.
     *
     * The new source folders are also added to the model.
     *
     * @param task the task
     * @param sourceFolders the source folders where the generated source code is.
     */
    void registerJavaGeneratingTask(@NonNull Task task, @NonNull File... sourceFolders);

    /**
     * Adds to the variant a task that generates Java source code.
     *
     * This will make the generate[Variant]Sources task depend on this task and add the
     * new source folders as compilation inputs.
     *
     * The new source folders are also added to the model.
     *
     * @param task the task
     * @param sourceFolders the source folders where the generated source code is.
     */
    void registerJavaGeneratingTask(@NonNull Task task, @NonNull Collection<File> sourceFolders);

    /**
     * Adds to the variant a task that generates Resources.
     *
     * This will make the generate[Variant]Resources task depend on this task and add the
     * new Resource folders as Resource merge inputs.
     *
     * The Resource folders are also added to the model.
     *
     * @param task the task
     * @param resFolders the folders where the generated resources are.
     */
    void registerResGeneratingTask(@NonNull Task task, @NonNull File... resFolders);

    /**
     * Adds to the variant a task that generates Resources.
     *
     * This will make the generate[Variant]Resources task depend on this task and add the
     * new Resource folders as Resource merge inputs.
     *
     * The Resource folders are also added to the model.
     *
     * @param task the task
     * @param resFolders the folders where the generated resources are.
     */
    void registerResGeneratingTask(@NonNull Task task, @NonNull Collection<File> resFolders);

    /**
     * Adds a variant-specific BuildConfig field.
     * @param type the type of the field
     * @param name the name of the field
     * @param value the value of the field
     */
    void buildConfigField(@NonNull String type, @NonNull String name, @NonNull String value);

    /**
     * Adds a variant-specific res value.
     * @param type the type of the field
     * @param name the name of the field
     * @param value the value of the field
     */
    void resValue(@NonNull String type, @NonNull String name, @NonNull String value);

    /**
     * If true, variant outputs will be considered signed. Only set if you manually set the outputs
     * to point to signed files built by other tasks.
     */
    void setOutputsAreSigned(boolean isSigned);

    /**
     * @see #setOutputsAreSigned(boolean)
     */
    boolean getOutputsAreSigned();
}
