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
package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.LoggingUtil;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dependency.SymbolFileProviderImpl;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantOutputScope;
import com.android.build.gradle.internal.tasks.IncrementalTask;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.core.AaptPackageProcessBuilder;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantType;
import com.android.builder.dependency.LibraryDependency;
import com.android.ide.common.blame.ParsingProcessOutputHandler;
import com.android.ide.common.blame.parser.ToolOutputParser;
import com.android.ide.common.blame.parser.aapt.AaptOutputParser;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.utils.FileUtils;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@ParallelizableTask
public class ProcessAndroidResources extends IncrementalTask {

    private File manifestFile;

    private File resDir;

    private File assetsDir;

    private File sourceOutputDir;

    private File textSymbolOutputDir;

    private File packageOutputFile;

    private File proguardOutputFile;

    private Collection<String> resourceConfigs;

    private String preferredDensity;

    private List<SymbolFileProviderImpl> libraries;

    private String packageForR;

    private Collection<String> splits;

    private boolean enforceUniquePackageName;

    private VariantType type;

    private boolean debuggable;

    private boolean pseudoLocalesEnabled;

    private AaptOptions aaptOptions;


    @Override
    protected void doFullTaskAction() throws IOException {
        // we have to clean the source folder output in case the package name changed.
        File srcOut = getSourceOutputDir();
        if (srcOut != null) {
            FileUtils.emptyFolder(srcOut);
        }

        File resOutBaseNameFile = getPackageOutputFile();

        // we have to check the resource output folder in case some splits were removed, we should
        // manually remove them.
        File packageOutputFolder = getResDir();
        if (resOutBaseNameFile != null) {
            for (File file : packageOutputFolder.listFiles()) {
                if (!isSplitPackage(file, resOutBaseNameFile)) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }

        AaptPackageProcessBuilder aaptPackageCommandBuilder =
                new AaptPackageProcessBuilder(getManifestFile(), getAaptOptions())
                        .setAssetsFolder(getAssetsDir())
                        .setResFolder(getResDir())
                        .setLibraries(getLibraries())
                        .setPackageForR(getPackageForR())
                        .setSourceOutputDir(absolutePath(srcOut))
                        .setSymbolOutputDir(absolutePath(getTextSymbolOutputDir()))
                        .setResPackageOutput(absolutePath(resOutBaseNameFile))
                        .setProguardOutput(absolutePath(getProguardOutputFile()))
                        .setType(getType())
                        .setDebuggable(getDebuggable())
                        .setPseudoLocalesEnabled(getPseudoLocalesEnabled())
                        .setResourceConfigs(getResourceConfigs())
                        .setSplits(getSplits())
                        .setPreferredDensity(getPreferredDensity());

        @NonNull
        AndroidBuilder builder = getBuilder();
        ProcessOutputHandler processOutputHandler = new ParsingProcessOutputHandler(
                new ToolOutputParser(new AaptOutputParser(), getILogger()),
                builder.getErrorReporter());
        try {
            builder.processResources(
                    aaptPackageCommandBuilder,
                    getEnforceUniquePackageName(),
                    processOutputHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSplitPackage(File file, File resBaseName) {
        if (file.getName().startsWith(resBaseName.getName())) {
            for (String split : splits) {
                if (file.getName().contains(split)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private static String absolutePath(@Nullable File file) {
        return file == null ? null : file.getAbsolutePath();
    }

    public static class ConfigAction implements TaskConfigAction<ProcessAndroidResources> {

        private VariantOutputScope scope;
        private File symbolLocation;
        private boolean generateResourcePackage;

        public ConfigAction(
                VariantOutputScope scope, File symbolLocation, boolean generateResourcePackage) {
            this.scope = scope;
            this.symbolLocation = symbolLocation;
            this.generateResourcePackage = generateResourcePackage;
        }

        @Override
        public String getName() {
            return scope.getTaskName("process", "Resources");
        }

        @Override
        public Class<ProcessAndroidResources> getType() {
            return ProcessAndroidResources.class;
        }

        @Override
        public void execute(ProcessAndroidResources processResources) {
            final BaseVariantOutputData variantOutputData = scope.getVariantOutputData();
            final BaseVariantData<? extends BaseVariantOutputData> variantData =
                    scope.getVariantScope().getVariantData();
            variantOutputData.processResourcesTask = processResources;
            final GradleVariantConfiguration config = variantData.getVariantConfiguration();

            processResources.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            processResources.setVariantName(config.getFullName());

            if (variantData.getSplitHandlingPolicy() ==
                    BaseVariantData.SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY) {
                Set<String> allFilters = new HashSet<String>();
                allFilters.addAll(
                        variantData.getFilters(com.android.build.OutputFile.FilterType.DENSITY));
                allFilters.addAll(
                        variantData.getFilters(com.android.build.OutputFile.FilterType.LANGUAGE));
                processResources.splits = allFilters;
            }

            // only generate code if the density filter is null, and if we haven't generated
            // it yet (if you have abi + density splits, then several abi output will have no
            // densityFilter)
            if (variantOutputData.getMainOutputFile()
                    .getFilter(com.android.build.OutputFile.DENSITY) == null
                    && variantData.generateRClassTask == null) {
                variantData.generateRClassTask = processResources;
                processResources.enforceUniquePackageName = scope.getGlobalScope().getExtension()
                        .getEnforceUniquePackageName();

                ConventionMappingHelper.map(processResources, "libraries",
                        new Callable<List<SymbolFileProviderImpl>>() {
                            @Override
                            public List<SymbolFileProviderImpl> call() throws Exception {
                                return getTextSymbolDependencies(config.getAllLibraries());
                            }
                        });
                ConventionMappingHelper.map(processResources, "packageForR",
                        new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return config.getOriginalApplicationId();
                            }
                        });

                // TODO: unify with generateBuilderConfig, compileAidl, and library packaging somehow?
                processResources
                        .setSourceOutputDir(scope.getVariantScope().getRClassSourceOutputDir());
                processResources.setTextSymbolOutputDir(symbolLocation);

                if (config.getBuildType().isMinifyEnabled()) {
                    if (config.getBuildType().isShrinkResources() && config.getUseJack()) {
                        LoggingUtil.displayWarning(Logging.getLogger(getClass()),
                                scope.getGlobalScope().getProject(),
                                "shrinkResources does not yet work with useJack=true");
                    }
                    processResources.setProguardOutputFile(
                            scope.getVariantScope().getProcessAndroidResourcesProguardOutputFile());

                } else if (config.getBuildType().isShrinkResources()) {
                    LoggingUtil.displayWarning(Logging.getLogger(getClass()),
                            scope.getGlobalScope().getProject(),
                            "To shrink resources you must also enable ProGuard");
                }
            }

            ConventionMappingHelper.map(processResources, "manifestFile", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return variantOutputData.manifestProcessorTask.getOutputFile();
                }
            });

            ConventionMappingHelper.map(processResources, "resDir", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return scope.getVariantScope().getFinalResourcesDir();
                }
            });

            ConventionMappingHelper.map(processResources, "assetsDir", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return variantData.mergeAssetsTask.getOutputDir();
                }
            });

            if (generateResourcePackage) {
                processResources.setPackageOutputFile(scope.getProcessResourcePackageOutputFile());
            }

            processResources.setType(config.getType());
            processResources.setDebuggable(config.getBuildType().isDebuggable());
            processResources.setAaptOptions(scope.getGlobalScope().getExtension().getAaptOptions());
            processResources
                    .setPseudoLocalesEnabled(config.getBuildType().isPseudoLocalesEnabled());

            ConventionMappingHelper.map(processResources, "resourceConfigs",
                    new Callable<Collection<String>>() {
                        @Override
                        public Collection<String> call() throws Exception {
                            Collection<String> resConfigs =
                                    config.getMergedFlavor().getResourceConfigurations();
                            if (resConfigs.size() == 1 &&
                                    Iterators.getOnlyElement(resConfigs.iterator())
                                            .equals("auto")) {
                                return variantData.discoverListOfResourceConfigs();
                            }
                            return config.getMergedFlavor().getResourceConfigurations();
                        }
                    });

            ConventionMappingHelper.map(processResources, "preferredDensity",
                    new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            return variantOutputData.getMainOutputFile()
                                    .getFilter(com.android.build.OutputFile.DENSITY);
                        }
                    });


        }

        @NonNull
        private static List<SymbolFileProviderImpl> getTextSymbolDependencies(
                List<LibraryDependency> libraries) {

            List<SymbolFileProviderImpl> list = Lists.newArrayListWithCapacity(libraries.size());

            for (LibraryDependency lib : libraries) {
                list.add(new SymbolFileProviderImpl(lib));
            }

            return list;
        }
    }

    @InputFile
    public File getManifestFile() {
        return manifestFile;
    }

    public void setManifestFile(File manifestFile) {
        this.manifestFile = manifestFile;
    }

    @NonNull
    @InputDirectory
    public File getResDir() {
        return resDir;
    }

    public void setResDir(@NonNull File resDir) {
        this.resDir = resDir;
    }

    @OutputDirectory
    @Optional
    public File getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(File assetsDir) {
        this.assetsDir = assetsDir;
    }

    @OutputDirectory
    @Optional
    public File getSourceOutputDir() {
        return sourceOutputDir;
    }

    public void setSourceOutputDir(File sourceOutputDir) {
        this.sourceOutputDir = sourceOutputDir;
    }

    @OutputDirectory
    @Optional
    public File getTextSymbolOutputDir() {
        return textSymbolOutputDir;
    }

    public void setTextSymbolOutputDir(File textSymbolOutputDir) {
        this.textSymbolOutputDir = textSymbolOutputDir;
    }

    @OutputFile
    @Optional
    public File getPackageOutputFile() {
        return packageOutputFile;
    }

    public void setPackageOutputFile(File packageOutputFile) {
        this.packageOutputFile = packageOutputFile;
    }

    @OutputFile
    @Optional
    public File getProguardOutputFile() {
        return proguardOutputFile;
    }

    public void setProguardOutputFile(File proguardOutputFile) {
        this.proguardOutputFile = proguardOutputFile;
    }

    @Input
    public Collection<String> getResourceConfigs() {
        return resourceConfigs;
    }

    public void setResourceConfigs(Collection<String> resourceConfigs) {
        this.resourceConfigs = resourceConfigs;
    }

    @Input
    @Optional
    public String getPreferredDensity() {
        return preferredDensity;
    }

    public void setPreferredDensity(String preferredDensity) {
        this.preferredDensity = preferredDensity;
    }

    @Input
    String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }

    @Nested
    @Optional
    public List<SymbolFileProviderImpl> getLibraries() {
        return libraries;
    }

    public void setLibraries(
            List<SymbolFileProviderImpl> libraries) {
        this.libraries = libraries;
    }

    @Input
    @Optional
    public String getPackageForR() {
        return packageForR;
    }

    public void setPackageForR(String packageForR) {
        this.packageForR = packageForR;
    }

    @Nested
    @Optional
    public Collection<String> getSplits() {
        return splits;
    }

    public void setSplits(Collection<String> splits) {
        this.splits = splits;
    }

    @Input
    public boolean getEnforceUniquePackageName() {
        return enforceUniquePackageName;
    }

    public void setEnforceUniquePackageName(boolean enforceUniquePackageName) {
        this.enforceUniquePackageName = enforceUniquePackageName;
    }

    /** Does not change between incremental builds, so does not need to be @Input. */
    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    @Input
    public boolean getDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    @Input
    public boolean getPseudoLocalesEnabled() {
        return pseudoLocalesEnabled;
    }

    public void setPseudoLocalesEnabled(boolean pseudoLocalesEnabled) {
        this.pseudoLocalesEnabled = pseudoLocalesEnabled;
    }

    @Nested
    public AaptOptions getAaptOptions() {
        return aaptOptions;
    }

    public void setAaptOptions(AaptOptions aaptOptions) {
        this.aaptOptions = aaptOptions;
    }
}
