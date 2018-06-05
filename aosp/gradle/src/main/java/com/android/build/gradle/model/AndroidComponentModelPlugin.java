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

package com.android.build.gradle.model;

import static com.android.builder.core.VariantType.ANDROID_TEST;
import static com.android.builder.core.VariantType.UNIT_TEST;

import com.android.build.gradle.internal.ProductFlavorCombo;
import com.android.build.gradle.managed.AndroidConfig;
import com.android.build.gradle.managed.BuildType;
import com.android.build.gradle.managed.ProductFlavor;
import com.android.builder.core.BuilderConstants;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.StringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.ProjectSourceSet;
import org.gradle.language.base.internal.registry.LanguageRegistration;
import org.gradle.language.base.internal.registry.LanguageRegistry;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryType;
import org.gradle.platform.base.BinaryTypeBuilder;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;
import org.gradle.tooling.BuildException;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Plugin to set up infrastructure for other android plugins.
 */
public class AndroidComponentModelPlugin implements Plugin<Project> {

    /**
     * The name of ComponentSpec created with android component model plugin.
     */
    public static final String COMPONENT_NAME = "android";

    //public static final Pattern GRADLE_ACCEPTABLE_VERSIONS = Pattern.compile("2\\.5.*");
    public static final String GRADLE_ACCEPTABLE_VERSION = "2.5";

    private static final String GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY =
            "com.android.build.gradle.overrideVersionCheck";

    @Override
    public void apply(Project project) {
        checkGradleVersion(project);
        project.getPlugins().apply(ComponentModelBasePlugin.class);
    }

    private static void checkGradleVersion(Project project) {
        String gradleVersion = project.getGradle().getGradleVersion();
        if (!gradleVersion.startsWith(GRADLE_ACCEPTABLE_VERSION)) {
            boolean allowNonMatching = Boolean.getBoolean(GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
            File file = new File("gradle" + File.separator + "wrapper" + File.separator +
                    "gradle-wrapper.properties");
            String errorMessage = String.format(
                    "Gradle version %s is required. Current version is %s. " +
                            "If using the gradle wrapper, try editing the distributionUrl in %s " +
                            "to gradle-%s-all.zip",
                    GRADLE_ACCEPTABLE_VERSION, gradleVersion, file.getAbsolutePath(),
                    GRADLE_ACCEPTABLE_VERSION);
            if (allowNonMatching) {
                project.getLogger().warn(errorMessage);
                project.getLogger().warn("As %s is set, continuing anyways.",
                        GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
            } else {
                throw new BuildException(errorMessage, null);
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static class Rules extends RuleSource {

        @LanguageType
        public void registerLanguage(LanguageTypeBuilder<AndroidLanguageSourceSet> builder) {
            builder.setLanguageName("android");
            builder.defaultImplementation(AndroidLanguageSourceSet.class);
        }

        /**
         * Create "android" model block.
         */
        @Model("android")
        public void android(AndroidConfig androidModel) {
        }

        @Defaults
        public void androidModelSources(AndroidConfig androidModel,
                @Path("androidSources") AndroidComponentModelSourceSet sources) {
            androidModel.setSources(sources);
        }

        @Finalize
        public void finalizeAndroidModel(AndroidConfig androidModel) {
            if (androidModel.getBuildToolsRevision() == null
                    && androidModel.getBuildToolsVersion() != null) {
                androidModel.setBuildToolsRevision(
                        FullRevision.parseRevision(androidModel.getBuildToolsVersion()));
            }

            if (androidModel.getCompileSdkVersion() != null
                    && !androidModel.getCompileSdkVersion().startsWith("android-")
                    && Ints.tryParse(androidModel.getCompileSdkVersion()) != null) {
                androidModel.setCompileSdkVersion("android-" + androidModel.getCompileSdkVersion());
            }

        }

        @Defaults
        public void createDefaultBuildTypes(
                @Path("android.buildTypes") ModelMap<BuildType> buildTypes) {
            buildTypes.create(BuilderConstants.DEBUG, new Action<BuildType>() {
                @Override
                public void execute(BuildType buildType) {
                    buildType.setDebuggable(true);
                    buildType.setEmbedMicroApp(false);
                }
            });
            buildTypes.create(BuilderConstants.RELEASE);
        }

        @Model
        public List<ProductFlavorCombo<ProductFlavor>> createProductFlavorCombo(
                @Path("android.productFlavors") ModelMap<ProductFlavor> productFlavors) {
            // TODO: Create custom product flavor container to manually configure flavor dimensions.
            Set<String> flavorDimensionList = Sets.newHashSet();
            for (ProductFlavor flavor : productFlavors.values()) {
                if (flavor.getDimension() != null) {
                    flavorDimensionList.add(flavor.getDimension());
                }
            }

            return ProductFlavorCombo.createCombinations(
                    Lists.newArrayList(flavorDimensionList),
                    productFlavors.values());
        }

        @ComponentType
        public void defineComponentType(ComponentTypeBuilder<AndroidComponentSpec> builder) {
            builder.defaultImplementation(DefaultAndroidComponentSpec.class);
        }

        @Mutate
        public void createAndroidComponents(ModelMap<AndroidComponentSpec> androidComponents) {
            androidComponents.create(COMPONENT_NAME);
        }

        @Model
        public AndroidComponentModelSourceSet androidSources(ServiceRegistry serviceRegistry) {
            Instantiator instantiator = serviceRegistry.get(Instantiator.class);
            return new AndroidComponentModelSourceSet(instantiator);
        }

        /**
         * Create all source sets for each AndroidBinary.
         */
        @Mutate
        public void createVariantSourceSet(
                @Path("android.sources") final AndroidComponentModelSourceSet sources,
                @Path("android.buildTypes") final ModelMap<BuildType> buildTypes,
                @Path("android.productFlavors") ModelMap<ProductFlavor> flavors,
                List<ProductFlavorCombo<ProductFlavor>> flavorGroups, ProjectSourceSet projectSourceSet,
                LanguageRegistry languageRegistry) {
            sources.setProjectSourceSet(projectSourceSet);
            for (LanguageRegistration languageRegistration : languageRegistry) {
                sources.registerLanguage(languageRegistration);
            }

            // Create main source set.
            sources.create("main");
            sources.create(ANDROID_TEST.getPrefix());
            sources.create(UNIT_TEST.getPrefix());

            for (BuildType buildType : buildTypes.values()) {
                sources.maybeCreate(buildType.getName());

                for (ProductFlavorCombo group: flavorGroups) {
                    sources.maybeCreate(group.getName());
                    if (!group.getFlavorList().isEmpty()) {
                        sources.maybeCreate(
                                group.getName() + StringHelper.capitalize(buildType.getName()));
                    }

                }

            }
            if (flavorGroups.size() != flavors.size()) {
                // If flavorGroups and flavors are the same size, there is at most 1 flavor
                // dimension.  So we don't need to reconfigure the source sets for flavorGroups.
                for (ProductFlavor flavor: flavors.values()) {
                    sources.maybeCreate(flavor.getName());
                }
            }
        }

        @Finalize
        public void setDefaultSrcDir(
                @Path("android.sources") AndroidComponentModelSourceSet sourceSet) {
            sourceSet.setDefaultSrcDir();
        }

        @BinaryType
        public void defineBinaryType(BinaryTypeBuilder<AndroidBinary> builder) {
            builder.defaultImplementation(DefaultAndroidBinary.class);
        }

        @ComponentBinaries
        public void createBinaries(
                final ModelMap<AndroidBinary> binaries,
                @Path("android") final AndroidConfig androidConfig,
                @Path("android.buildTypes") final ModelMap<BuildType> buildTypes,
                final List<ProductFlavorCombo<ProductFlavor>> flavorCombos,
                final AndroidComponentSpec spec) {
            if (flavorCombos.isEmpty()) {
                flavorCombos.add(new ProductFlavorCombo<ProductFlavor>());
            }

            for (final BuildType buildType : buildTypes.values()) {
                for (final ProductFlavorCombo<ProductFlavor> flavorCombo : flavorCombos) {
                    binaries.create(getBinaryName(buildType, flavorCombo),
                            new Action<AndroidBinary>() {
                                @Override
                                public void execute(AndroidBinary androidBinary) {
                                    DefaultAndroidBinary binary = (DefaultAndroidBinary) androidBinary;
                                    binary.setBuildType(buildType);
                                    binary.setProductFlavors(flavorCombo.getFlavorList());
                                }
                            });
                }
            }
        }

        private static String getBinaryName(BuildType buildType, ProductFlavorCombo flavorCombo) {
            if (flavorCombo.getFlavorList().isEmpty()) {
                return buildType.getName();
            } else {
                return flavorCombo.getName() + StringHelper.capitalize(buildType.getName());
            }

        }
    }
}
