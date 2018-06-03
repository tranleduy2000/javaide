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

package com.android.build.gradle.internal.dependency
import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.gradle.internal.ConfigurationProvider
import com.android.builder.core.VariantType
import com.android.builder.dependency.DependencyContainer
import com.android.builder.dependency.JarDependency
import com.android.builder.dependency.LibraryDependency
import com.google.common.base.Objects
import com.google.common.collect.Sets
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * Object that represents the dependencies of a "config", in the sense of defaultConfigs, build
 * type and flavors.
 *
 * The dependencies are expressed as composite Gradle configuration objects that extends
 * all the configuration objects of the "configs".
 *
 * It optionally contains the dependencies for a test config for the given config.
 */
public class VariantDependencies implements DependencyContainer {

    final String name

    @NonNull
    private final Configuration compileConfiguration
    @NonNull
    private final Configuration packageConfiguration
    @NonNull
    private final Configuration publishConfiguration

    @Nullable
    private final Configuration mappingConfiguration
    @Nullable
    private final Configuration classesConfiguration
    @Nullable
    private final Configuration metadataConfiguration

    @NonNull
    private final List<LibraryDependencyImpl> libraries = []
    @NonNull
    private final List<JarDependency> jars = []
    @NonNull
    private final List<JarDependency> localJars = []

    /**
     *  Whether we have a direct dependency on com.android.support:support-annotations; this
     * is used to drive whether we extract annotations when building libraries for example
     */
    boolean annotationsPresent

    DependencyChecker checker

    static VariantDependencies compute(
            @NonNull Project project,
            @NonNull String name,
            boolean publishVariant,
            @NonNull VariantType variantType,
            @Nullable VariantDependencies parentVariant,
            @NonNull ConfigurationProvider... providers) {
        Set<Configuration> compileConfigs = Sets.newHashSetWithExpectedSize(providers.length * 2)
        Set<Configuration> apkConfigs = Sets.newHashSetWithExpectedSize(providers.length)

        for (ConfigurationProvider provider : providers) {
            if (provider != null) {
                compileConfigs.add(provider.compileConfiguration)
                if (provider.providedConfiguration != null) {
                    compileConfigs.add(provider.providedConfiguration)
                }

                apkConfigs.add(provider.compileConfiguration)
                apkConfigs.add(provider.packageConfiguration)
            }
        }

        if (parentVariant != null) {
            compileConfigs.add(parentVariant.getCompileConfiguration())
            apkConfigs.add(parentVariant.getPackageConfiguration())
        }

        Configuration compile = project.configurations.maybeCreate("_${name}Compile")
        compile.visible = false
        compile.description = "## Internal use, do not manually configure ##"
        compile.setExtendsFrom(compileConfigs)

        Configuration apk = project.configurations.maybeCreate(variantType == VariantType.LIBRARY
                ? "_${name}Publish"
                : "_${name}Apk")

        apk.visible = false
        apk.description = "## Internal use, do not manually configure ##"
        apk.setExtendsFrom(apkConfigs)

        Configuration publish = null, mapping = null, classes = null, metadata = null;
        if (publishVariant) {
            publish = project.configurations.maybeCreate(name)
            publish.description = "Published Configuration for Variant ${name}"
            // if the variant is not a library, then the publishing configuration should
            // not extend from the apkConfigs. It's mostly there to access the artifact from
            // another project but it shouldn't bring any dependencies with it.
            if (variantType == VariantType.LIBRARY) {
                publish.setExtendsFrom(apkConfigs)
            }

            // create configuration for -metadata.
            metadata = project.configurations.create("$name-metadata")
            metadata.description = "Published APKs metadata for Variant $name"

            // create configuration for -mapping and -classes.
            mapping = project.configurations.maybeCreate("$name-mapping")
            mapping.description = "Published mapping configuration for Variant $name"

            classes = project.configurations.maybeCreate("$name-classes")
            classes.description = "Published classes configuration for Variant $name"
            // because we need the transitive dependencies for the classes, extend the compile config.
            classes.setExtendsFrom(compileConfigs)
        }

        return new VariantDependencies(
                name,
                compile,
                apk,
                publish,
                mapping,
                classes,
                metadata,
                variantType != VariantType.UNIT_TEST);
    }

    private VariantDependencies(@NonNull  String name,
                                @NonNull  Configuration compileConfiguration,
                                @NonNull  Configuration packageConfiguration,
                                @Nullable Configuration publishConfiguration,
                                @Nullable Configuration mappingConfiguration,
                                @Nullable Configuration classesConfiguration,
                                @Nullable Configuration metadataConfiguration,
                                boolean skipClassesInAndroid) {
        this.name = name
        this.compileConfiguration = compileConfiguration
        this.packageConfiguration = packageConfiguration
        this.publishConfiguration = publishConfiguration
        this.mappingConfiguration = mappingConfiguration
        this.classesConfiguration = classesConfiguration
        this.metadataConfiguration = metadataConfiguration
        this.checker = new DependencyChecker(this, skipClassesInAndroid)
    }

    public String getName() {
        return name
    }

    @NonNull
    Configuration getCompileConfiguration() {
        return compileConfiguration
    }

    @NonNull
    Configuration getPackageConfiguration() {
        return packageConfiguration
    }

    @Nullable
    Configuration getPublishConfiguration() {
        return publishConfiguration
    }

    @Nullable
    Configuration getMappingConfiguration() {
        return mappingConfiguration
    }

    @Nullable
    Configuration getClassesConfiguration() {
        return classesConfiguration
    }

    @Nullable
    Configuration getMetadataConfiguration() {
        return metadataConfiguration
    }

    void addLibraries(@NonNull List<LibraryDependencyImpl> list) {
        libraries.addAll(list)
    }

    void addJars(@NonNull Collection<JarDependency> list) {
        jars.addAll(list)
    }

    void addLocalJars(@NonNull Collection<JarDependency> list) {
        localJars.addAll(list)
    }

    @NonNull
    List<LibraryDependencyImpl> getLibraries() {
        return libraries
    }

    @NonNull
    @Override
    List<? extends LibraryDependency> getAndroidDependencies() {
        return libraries
    }

    @NonNull
    @Override
    List<JarDependency> getJarDependencies() {
        return jars
    }

    @NonNull
    @Override
    List<JarDependency> getLocalDependencies() {
        return localJars
    }

    public boolean hasNonOptionalLibraries() {
        for (LibraryDependency libraryDependency : libraries) {
            if (!libraryDependency.isOptional()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .toString();
    }
}
