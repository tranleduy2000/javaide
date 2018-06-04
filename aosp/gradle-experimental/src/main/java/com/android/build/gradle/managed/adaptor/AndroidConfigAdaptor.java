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

import static com.android.builder.core.VariantType.ANDROID_TEST;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.api.AndroidSourceDirectorySet;
import com.android.build.gradle.api.AndroidSourceFile;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.VariantFilter;
import com.android.build.gradle.internal.BuildTypeData;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.ProductFlavorData;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.dsl.CoreNdkOptions;
import com.android.build.gradle.internal.coverage.JacocoExtension;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.AdbOptions;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.PreprocessingOptions;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.build.gradle.managed.BuildType;
import com.android.build.gradle.managed.ProductFlavor;
import com.android.build.gradle.managed.SigningConfig;
import com.android.build.gradle.model.AndroidComponentModelSourceSet;
import com.android.build.gradle.managed.AndroidConfig;
import com.android.builder.core.BuilderConstants;
import com.android.builder.core.LibraryRequest;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestServer;
import com.android.ide.common.rendering.api.ActionBarCallback;
import com.android.sdklib.repository.FullRevision;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.language.base.FunctionalSourceSet;
import org.gradle.language.base.LanguageSourceSet;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import groovy.lang.Closure;

/**
 * An adaptor to convert a managed.AndroidConfig to an model.AndroidConfig.
 */
public class AndroidConfigAdaptor implements com.android.build.gradle.AndroidConfig {

    private final AndroidConfig model;
    private NamedDomainObjectContainer<AndroidSourceSet> sourceSetsContainer;

    public AndroidConfigAdaptor(
            AndroidConfig model,
            NamedDomainObjectContainer<AndroidSourceSet> sourceSetsContainer) {
        this.model = model;
        this.sourceSetsContainer = sourceSetsContainer;
        applyProjectSourceSet();
    }

    @Override
    public String getBuildToolsVersion() {
        return model.getBuildToolsVersion();
    }

    @Override
    public String getCompileSdkVersion() {
        return model.getCompileSdkVersion();
    }

    @Override
    public FullRevision getBuildToolsRevision() {
        return model.getBuildToolsRevision();
    }

    @Override
    public boolean getEnforceUniquePackageName() {
        return false;
    }

    @Override
    public CoreProductFlavor getDefaultConfig() {
        return new ProductFlavorAdaptor(model.getDefaultConfig());
    }

    @Override
    @NonNull
    public List<DeviceProvider> getDeviceProviders() {
        return model.getDeviceProviders() == null ?
                Lists.<DeviceProvider>newArrayList() :
                model.getDeviceProviders();
    }

    @Override
    @NonNull
    public List<TestServer> getTestServers() {
        return model.getTestServers();
    }

    @Override
    public String getDefaultPublishConfig() {
        return model.getDefaultPublishConfig();
    }

    @Override
    public boolean getPublishNonDefault() {
        return model.getPublishNonDefault();
    }

    @Override
    public Action<VariantFilter> getVariantFilter() {
        return model.getVariantFilter();
    }

    @Override
    public String getResourcePrefix() {
        return model.getResourcePrefix();
    }

    @Override
    public List<String> getFlavorDimensionList() {
        return null;
    }

    @Override
    public boolean getGeneratePureSplits() {
        return model.getGeneratePureSplits();
    }

    @Override
    public PreprocessingOptions getPreprocessingOptions() {
        return model.getPreProcessingOptions();
    }

    @Override
    public Collection<CoreBuildType> getBuildTypes() {
        return ImmutableList.copyOf(Iterables.transform(model.getBuildTypes().values(),
                new Function<BuildType, CoreBuildType>() {
                    @Override
                    public CoreBuildType apply(BuildType buildType) {
                        return new BuildTypeAdaptor(buildType);
                    }
                }));
    }

    @Override
    public Collection<CoreProductFlavor> getProductFlavors() {
        return ImmutableList.copyOf(Iterables.transform(model.getProductFlavors().values(),
                new Function<ProductFlavor, CoreProductFlavor>() {
                    @Override
                    public CoreProductFlavor apply(ProductFlavor flavor) {
                        return new ProductFlavorAdaptor(flavor);
                    }
                }));
    }

    @Override
    public Collection<com.android.builder.model.SigningConfig> getSigningConfigs() {
        return ImmutableList.copyOf(Iterables.transform(model.getSigningConfigs().values(),
                new Function<SigningConfig, com.android.builder.model.SigningConfig>() {
                    @Override
                    public com.android.builder.model.SigningConfig apply(SigningConfig signingConfig) {
                        return new SigningConfigAdaptor(signingConfig);
                    }
                }));
    }

    @Override
    public NamedDomainObjectContainer<AndroidSourceSet> getSourceSets() {
        return sourceSetsContainer;
    }

    @Override
    public Boolean getPackageBuildConfig() {
        return true;
    }

    public AndroidComponentModelSourceSet getSources() {
        return model.getSources();
    }

    public void setSources(AndroidComponentModelSourceSet sources) {
        model.setSources(sources);
    }

    public CoreNdkOptions getNdk() {
        return new NdkOptionsAdaptor(model.getNdk());
    }

    @Override
    public AdbOptions getAdbOptions() {
        return model.getAdbOptions();
    }

    @Override
    public AaptOptions getAaptOptions() {
        return model.getAaptOptions();
    }

    @Override
    public CompileOptions getCompileOptions() {
        return model.getCompileOptions();
    }

    @Override
    public DexOptions getDexOptions() {
        return model.getDexOptions();
    }

    @Override
    public JacocoExtension getJacoco() {
        return model.getJacoco();
    }

    @Override
    public LintOptions getLintOptions() {
        return model.getLintOptions();
    }

    @Override
    public PackagingOptions getPackagingOptions() {
        return model.getPackagingOptions();
    }


    @Override
    public TestOptions getTestOptions() {
        return model.getTestOptions();
    }

    @Override
    public Splits getSplits() {
        return model.getSplits();
    }

    @Override
    public Collection<LibraryRequest> getLibraryRequests() {
        return model.getLibraryRequests();
    }

    private void applyProjectSourceSet() {
        for (FunctionalSourceSet source : getSources()) {
            String name = source.getName();
            AndroidSourceSet androidSource = name.equals(BuilderConstants.MAIN) ?
                    sourceSetsContainer.maybeCreate(getDefaultConfig().getName()) :
                    sourceSetsContainer.maybeCreate(name);

            convertSourceFile(androidSource.getManifest(), source, "manifest");
            convertSourceSet(androidSource.getResources(), source, "resource");
            convertSourceSet(androidSource.getJava(), source, "java");
            convertSourceSet(androidSource.getRes(), source, "res");
            convertSourceSet(androidSource.getAssets(), source, "assets");
            convertSourceSet(androidSource.getAidl(), source, "aidl");
            convertSourceSet(androidSource.getRenderscript(), source, "renderscript");
            convertSourceSet(androidSource.getJni(), source, "jni");
            convertSourceSet(androidSource.getJniLibs(), source, "jniLibs");
        }
    }

    @Nullable
    private static AndroidSourceSet findAndroidSourceSet(
            VariantManager variantManager,
            String name) {
        BuildTypeData buildTypeData = variantManager.getBuildTypes().get(name);
        if (buildTypeData != null) {
            return buildTypeData.getSourceSet();
        }

        boolean isTest = name.startsWith(ANDROID_TEST.getPrefix());
        name = name.replaceFirst(ANDROID_TEST.getPrefix(), "");
        ProductFlavorData productFlavorData = variantManager.getProductFlavors().get(name);
        if (productFlavorData != null) {
            return isTest ? productFlavorData.getTestSourceSet(ANDROID_TEST) : productFlavorData.getSourceSet();
        }
        return null;
    }

    /**
     * Convert a FunctionalSourceSet to an AndroidSourceFile.
     */
    private static void convertSourceFile(
            AndroidSourceFile androidFile,
            FunctionalSourceSet source,
            String sourceName) {
        LanguageSourceSet languageSourceSet = source.findByName(sourceName);
        if (languageSourceSet == null) {
            return;
        }
        SourceDirectorySet dir = languageSourceSet.getSource();
        if (dir == null) {
            return;
        }
        // We use the first file in the file tree until Gradle has a way to specify one source file
        // instead of an entire source set.
        Set<File> files = dir.getAsFileTree().getFiles();
        if (!files.isEmpty()) {
            androidFile.srcFile(Iterables.getOnlyElement(files));
        }
    }

    /**
     * Convert a FunctionalSourceSet to an AndroidSourceDirectorySet.
     */
    private static void convertSourceSet(
            AndroidSourceDirectorySet androidDir,
            FunctionalSourceSet source,
            String sourceName) {
        LanguageSourceSet languageSourceSet = source.findByName(sourceName);
        if (languageSourceSet == null) {
            return;
        }
        SourceDirectorySet dir = languageSourceSet.getSource();
        if (dir == null) {
            return;
        }
        androidDir.setSrcDirs(dir.getSrcDirs());
        androidDir.include(dir.getIncludes());
        androidDir.exclude(dir.getExcludes());
    }
}
