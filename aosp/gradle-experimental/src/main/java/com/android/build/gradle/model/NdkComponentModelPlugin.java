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

package com.android.build.gradle.model;

import static com.android.build.gradle.model.AndroidComponentModelPlugin.COMPONENT_NAME;

import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.NdkOptionsHelper;
import com.android.build.gradle.internal.ProductFlavorCombo;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.managed.BuildType;
import com.android.build.gradle.managed.NdkConfig;
import com.android.build.gradle.managed.ProductFlavor;
import com.android.build.gradle.ndk.internal.NdkConfiguration;
import com.android.build.gradle.ndk.internal.NdkExtensionConvention;
import com.android.build.gradle.ndk.internal.NdkNamingScheme;
import com.android.build.gradle.ndk.internal.ToolchainConfiguration;
import com.android.builder.core.BuilderConstants;
import com.android.builder.core.VariantConfiguration;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.gradle.api.Action;
import org.gradle.api.BuildableModelElement;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectIdentifier;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.c.plugins.CPlugin;
import org.gradle.language.cpp.plugins.CppPlugin;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeLibraryBinarySpec;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.platform.base.binary.BaseBinarySpec;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Plugin for Android NDK applications.
 */
public class NdkComponentModelPlugin implements Plugin<Project> {
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getPluginManager().apply(AndroidComponentModelPlugin.class);
        project.getPluginManager().apply(CPlugin.class);
        project.getPluginManager().apply(CppPlugin.class);
    }

    @SuppressWarnings({"MethodMayBeStatic", "unused"})
    public static class Rules extends RuleSource {

        @Mutate
        public void initializeNdkConfig(@Path("android.ndk") NdkConfig ndk) {
            NdkOptionsHelper.init(ndk);
            ndk.setModuleName("");
            ndk.setToolchain("");
            ndk.setToolchainVersion("");
            ndk.setStl("");
            ndk.setRenderscriptNdkMode(false);
        }

        @Finalize
        public void setDefaultNdkExtensionValue(@Path("android.ndk") NdkConfig ndkConfig) {
            NdkExtensionConvention.setExtensionDefault(ndkConfig);
        }

        @Validate
        public void checkNdkDir(NdkHandler ndkHandler, @Path("android.ndk") NdkConfig ndkConfig) {
            if (!ndkConfig.getModuleName().isEmpty() && !ndkHandler.isNdkDirConfigured()) {
                throw new InvalidUserDataException(
                        "NDK location not found. Define location with ndk.dir in the "
                                + "local.properties file or with an ANDROID_NDK_HOME environment "
                                + "variable.");
            }
            if (ndkHandler.isNdkDirConfigured()) {
                if (!ndkHandler.getNdkDirectory().exists()) {
                    throw new InvalidUserDataException(
                            "Specified NDK location does not exists.  Please ensure ndk.dir in "
                                    + "local.properties file or ANDROID_NDK_HOME is configured "
                                    + "correctly.");

                }
            }
        }

        @Mutate
        public void addDefaultNativeSourceSet(
                @Path("android.sources") AndroidComponentModelSourceSet sources) {
            sources.addDefaultSourceSet("jni", AndroidLanguageSourceSet.class);
        }

        @Model(ModelConstants.NDK_HANDLER)
        public NdkHandler ndkHandler(
                ProjectIdentifier projectId,
                @Path("android.compileSdkVersion") String compileSdkVersion,
                @Path("android.ndk") NdkConfig ndkConfig) {
            while (projectId.getParentIdentifier() != null) {
                projectId = projectId.getParentIdentifier();
            }

            return new NdkHandler(projectId.getProjectDir(), compileSdkVersion,
                    ndkConfig.getToolchain(), ndkConfig.getToolchainVersion());
        }

        @Defaults
        public void initBuildTypeNdk(@Path("android.buildTypes") ModelMap<BuildType> buildTypes) {
            buildTypes.beforeEach(new Action<BuildType>() {
                @Override
                public void execute(BuildType buildType) {
                    NdkOptionsHelper.init(buildType.getNdk());
                }
            });

            buildTypes.named(
                    BuilderConstants.DEBUG,
                    new Action<BuildType>() {
                        @Override
                        public void execute(BuildType buildType) {
                            if (buildType.getNdk().getDebuggable() == null) {
                                buildType.getNdk().setDebuggable(true);
                            }
                        }
                    });
        }

        @Defaults
        public void initProductFlavorNdk(
                @Path("android.productFlavors") ModelMap<ProductFlavor> productFlavors) {
            productFlavors.beforeEach(new Action<ProductFlavor>() {
                @Override
                public void execute(ProductFlavor productFlavor) {
                    NdkOptionsHelper.init(productFlavor.getNdk());
                }
            });
        }

        @Mutate
        public void createAndroidPlatforms(PlatformContainer platforms, NdkHandler ndkHandler) {
            if (!ndkHandler.isNdkDirConfigured()) {
                return;
            }
            // Create android platforms.
            ToolchainConfiguration.configurePlatforms(platforms, ndkHandler);
        }

        @Mutate
        public void createToolchains(
                NativeToolChainRegistry toolchainRegistry,
                @Path("android.ndk") NdkConfig ndkConfig,
                NdkHandler ndkHandler) {
            if (!ndkHandler.isNdkDirConfigured()) {
                return;
            }
            // Create toolchain for each ABI.
            ToolchainConfiguration.configureToolchain(
                    toolchainRegistry,
                    ndkConfig.getToolchain(),
                    ndkHandler);
        }

        @Mutate
        public void createNativeBuildTypes(BuildTypeContainer nativeBuildTypes,
                @Path("android.buildTypes") ModelMap<BuildType> androidBuildTypes) {
            for (BuildType buildType : androidBuildTypes.values()) {
                nativeBuildTypes.maybeCreate(buildType.getName());
            }
        }

        @Mutate
        public void createNativeFlavors(FlavorContainer nativeFlavors,
                List<ProductFlavorCombo<ProductFlavor>> androidFlavorGroups) {
            if (androidFlavorGroups.isEmpty()) {
                // Create empty native flavor to override Gradle's default name.
                nativeFlavors.maybeCreate("");
            } else {
                for (ProductFlavorCombo group : androidFlavorGroups) {
                    nativeFlavors.maybeCreate(group.getName());
                }
            }
        }

        @Mutate
        public void createNativeLibrary(
                final ComponentSpecContainer specs,
                @Path("android.ndk") final NdkConfig ndkConfig,
                final NdkHandler ndkHandler,
                @Path("android.sources") final AndroidComponentModelSourceSet sources,
                @Path("buildDir") final File buildDir) {
            if (!ndkHandler.isNdkDirConfigured()) {
                return;
            }
            if (!ndkConfig.getModuleName().isEmpty()) {
                specs.create(
                        ndkConfig.getModuleName(),
                        NativeLibrarySpec.class,
                        new Action<NativeLibrarySpec>() {
                            @Override
                            public void execute(final NativeLibrarySpec nativeLib) {
                                ((DefaultAndroidComponentSpec) specs.get(COMPONENT_NAME))
                                        .setNativeLibrary(nativeLib);
                                NdkConfiguration.configureProperties(
                                        nativeLib,
                                        sources,
                                        buildDir,
                                        ndkHandler);
                            }
                        });
                DefaultAndroidComponentSpec androidSpecs =
                        (DefaultAndroidComponentSpec) specs.get(COMPONENT_NAME);
                androidSpecs.setNativeLibrary(
                        (NativeLibrarySpec) specs.get(ndkConfig.getModuleName()));
            }
        }

        @Mutate
        public void createAdditionalTasksForNatives(
                final ModelMap<Task> tasks,
                ModelMap<AndroidComponentSpec> specs,
                @Path("android.ndk") final NdkConfig ndkConfig,
                final NdkHandler ndkHandler,
                BinaryContainer binaries,
                @Path("buildDir") final File buildDir) {
            if (!ndkHandler.isNdkDirConfigured()) {
                return;
            }
            final DefaultAndroidComponentSpec androidSpec =
                    (DefaultAndroidComponentSpec) specs.get(COMPONENT_NAME);
            if (androidSpec.getNativeLibrary() != null) {
                binaries.withType(DefaultAndroidBinary.class, new Action<DefaultAndroidBinary>() {
                    @Override
                    public void execute(DefaultAndroidBinary binary) {
                        for (NativeBinarySpec nativeBinary : binary.getNativeBinaries()) {
                            NdkConfiguration.createTasks(
                                    tasks,
                                    (SharedLibraryBinarySpec) nativeBinary,
                                    buildDir,
                                    binary.getMergedNdkConfig(),
                                    ndkHandler);
                        }
                    }
                });
            }
        }

        @Mutate
        public void configureNativeBinary(
                BinaryContainer binaries,
                ComponentSpecContainer specs,
                @Path("android.ndk") final NdkConfig ndkConfig,
                @Path("buildDir") final File buildDir,
                final NdkHandler ndkHandler) {
            if (!ndkConfig.getModuleName().isEmpty()) {
                final NativeLibrarySpec library = specs.withType(NativeLibrarySpec.class)
                        .get(ndkConfig.getModuleName());
                binaries.withType(
                        DefaultAndroidBinary.class,
                        new Action<DefaultAndroidBinary>() {
                            @Override
                            public void execute(DefaultAndroidBinary binary) {
                                binary.computeMergedNdk(
                                        ndkConfig,
                                        binary.getProductFlavors(),
                                        binary.getBuildType());

                                Collection<SharedLibraryBinarySpec> nativeBinaries =
                                        getNativeBinaries(
                                                library,
                                                binary.getBuildType(),
                                                binary.getProductFlavors());
                                for (SharedLibraryBinarySpec nativeBin : nativeBinaries) {
                                    if (binary.getMergedNdkConfig().getAbiFilters().isEmpty() ||
                                            binary.getMergedNdkConfig().getAbiFilters().contains(
                                                    nativeBin.getTargetPlatform().getName())) {
                                        NdkConfiguration.configureBinary(
                                                nativeBin,
                                                buildDir,
                                                binary.getMergedNdkConfig(),
                                                ndkHandler);
                                        binary.getNativeBinaries().add(nativeBin);
                                    }
                                }
                            }
                        });
            }
        }

        @Finalize
        public void attachNativeTasksToAndroidBinary(ModelMap<AndroidBinary> binaries) {
            binaries.afterEach(new Action<AndroidBinary>() {
                @Override
                public void execute(AndroidBinary androidBinary) {
                    DefaultAndroidBinary binary = (DefaultAndroidBinary) androidBinary;
                    for (NativeLibraryBinarySpec nativeBinary : binary.getNativeBinaries()) {
                        if (binary.getTargetAbi().isEmpty() || binary.getTargetAbi().contains(
                                nativeBinary.getTargetPlatform().getName())) {
                            binary.getBuildTask().dependsOn(NdkNamingScheme.getNdkBuildTaskName(nativeBinary));
                        }
                    }
                }
            });
        }

        @Mutate
        public void removeNativeBinaryFromAssembleTask(ModelMap<AndroidComponentSpec> components) {
            // Setting each native binary to not buildable to prevent the native tasks to be
            // automatically added to the "assemble" task.
            components.afterEach(new Action<AndroidComponentSpec>() {
                @Override
                public void execute(AndroidComponentSpec spec) {
                    NativeLibrarySpec nativeLibrary =
                            ((DefaultAndroidComponentSpec)spec).getNativeLibrary();
                    if (nativeLibrary != null) {
                        nativeLibrary.getBinaries().afterEach(
                                new Action<BinarySpec>() {
                                    @Override
                                    public void execute(BinarySpec binary) {
                                        ((BaseBinarySpec) binary).setBuildable(false);
                                    }
                                });
                    }
                }
            });
        }

        /**
         * Remove unintended tasks created by Gradle native plugin from task list.
         *
         * Gradle native plugins creates static library tasks automatically.  This method removes
         * them to avoid cluttering the task list.
         */
        @Mutate
        public void hideNativeTasks(TaskContainer tasks, BinaryContainer binaries) {
            // Gradle do not support a way to remove created tasks.  The best workaround is to clear the
            // group of the task and have another task depends on it.  Therefore, we have to create
            // a dummy task to depend on all the tasks that we do not want to show up on the task
            // list. The dummy task dependsOn itself, effectively making it non-executable and
            // invisible unless the --all option is use.
            final Task nonExecutableTask = tasks.create("nonExecutableTask");
            nonExecutableTask.dependsOn(nonExecutableTask);
            nonExecutableTask
                    .setDescription("Dummy task to hide other unwanted tasks in the task list.");

            binaries.withType(NativeLibraryBinarySpec.class, new Action<NativeLibraryBinarySpec>() {
                @Override
                public void execute(NativeLibraryBinarySpec binary) {
                    Task buildTask = binary.getBuildTask();
                    nonExecutableTask.dependsOn(buildTask);
                    buildTask.setGroup(null);
                }
            });
        }
    }


    public static void configureScopeForNdk(VariantScope scope) {
        VariantConfiguration config = scope.getVariantConfiguration();
        ImmutableSet.Builder<File> builder = ImmutableSet.builder();
        for (Abi abi : NdkHandler.getAbiList()) {
            scope.addNdkDebuggableLibraryFolders(
                    abi,
                    new File(
                            scope.getGlobalScope().getBuildDir(),
                            NdkNamingScheme.getDebugLibraryDirectoryName(
                                    config.getBuildType().getName(),
                                    config.getFlavorName(),
                                    abi.getName())));

            // Return the parent directory of the binaries' output.
            // If output directory is "/path/to/lib/platformName".  We want to return
            // "/path/to/lib".
            builder.add(new File(
                    scope.getGlobalScope().getBuildDir(),
                    NdkNamingScheme.getOutputDirectoryName(
                            config.getBuildType().getName(),
                            config.getFlavorName(),
                            abi.getName())).getParentFile());
        }
        scope.setNdkSoFolder(builder.build());
    }


    private static Collection<SharedLibraryBinarySpec> getNativeBinaries(
            NativeLibrarySpec library,
            final BuildType buildType,
            final List<ProductFlavor> productFlavors) {
        final ProductFlavorCombo<ProductFlavor> flavorGroup =
                new ProductFlavorCombo<ProductFlavor>(productFlavors);
        return ImmutableList.copyOf(Iterables.filter(
                library.getBinaries().withType(SharedLibraryBinarySpec.class).values(),
                new Predicate<SharedLibraryBinarySpec>() {
                    @Override
                    public boolean apply(SharedLibraryBinarySpec binary) {
                        return binary.getBuildType().getName().equals(buildType.getName())
                                && (binary.getFlavor().getName().equals(flavorGroup.getName())
                                || (productFlavors.isEmpty()
                                && binary.getFlavor().getName().equals("default")));
                    }
                }));
    }

    /**
     * Return library binaries for a VariantConfiguration.
     */
    public Collection<? extends BuildableModelElement> getBinaries(final VariantConfiguration variantConfig) {
        if (variantConfig.getType().isForTesting()) {
            // Do not return binaries for test variants as test source set is not supported at the
            // moment.
            return Collections.emptyList();
        }
        BinaryContainer binaries = (BinaryContainer) project.getExtensions().getByName("binaries");
        return binaries.withType(AndroidBinary.class).matching(
                new Spec<AndroidBinary>() {
                    @Override
                    public boolean isSatisfiedBy(AndroidBinary binary) {
                        return (binary.getName().equals(variantConfig.getFullName()));
                    }
                }
        );
    }
}
