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
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.IncrementalTask;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.model.AndroidProject;
import com.android.builder.png.QueuedCruncher;
import com.android.builder.png.VectorDrawableRenderer;
import com.android.ide.common.internal.PngCruncher;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.res2.FileStatus;
import com.android.ide.common.res2.FileValidity;
import com.android.ide.common.res2.GeneratedResourceSet;
import com.android.ide.common.res2.MergedResourceWriter;
import com.android.ide.common.res2.MergingException;
import com.android.ide.common.res2.ResourceMerger;
import com.android.ide.common.res2.ResourcePreprocessor;
import com.android.ide.common.res2.ResourceSet;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.FileUtils;
import com.google.common.collect.Lists;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.StopExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@ParallelizableTask
public class MergeResources extends IncrementalTask {

    /**
     * The first version of build tools that normalizes resources when packaging the APK.
     *
     * <p>This means that e.g. drawable-hdpi becomes drawable-hdpi-v4 to make it clear it was not
     * available before API 4.
     */
    public static final FullRevision NORMALIZE_RESOURCES_BUILD_TOOLS = new FullRevision(21, 0, 0);

    // ----- PUBLIC TASK API -----

    /**
     * Directory to write the merged resources to
     */
    private File outputDir;

    // ----- PRIVATE TASK API -----

    /**
     * Optional file to write any publicly imported resource types and names to
     */
    private File publicFile;

    private boolean process9Patch;

    private boolean crunchPng;

    private boolean useNewCruncher;

    private boolean insertSourceMarkers = true;

    private boolean normalizeResources;

    private ResourcePreprocessor preprocessor;

    // actual inputs
    private List<ResourceSet> inputResourceSets;

    private final FileValidity<ResourceSet> fileValidity = new FileValidity<ResourceSet>();

    // fake input to detect changes. Not actually used by the task
    @InputFiles
    public Iterable<File> getRawInputFolders() {
        return flattenSourceSets(getInputResourceSets());
    }

    @Input
    public String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }

    @Override
    protected boolean isIncremental() {
        return true;
    }

    private PngCruncher getCruncher() {
        if (getUseNewCruncher()) {
            if (getBuilder().getTargetInfo().getBuildTools().getRevision().getMajor() >= 22) {
                return QueuedCruncher.Builder.INSTANCE.newCruncher(
                        getBuilder().getTargetInfo().getBuildTools().getPath(
                                BuildToolInfo.PathId.AAPT), getILogger());
            }
            getLogger().info("New PNG cruncher will be enabled with build tools 22 and above.");
        }
        return getBuilder().getAaptCruncher(new LoggedProcessOutputHandler(getBuilder().getLogger()));
    }

    @Override
    protected void doFullTaskAction() throws IOException {
        // this is full run, clean the previous output
        File destinationDir = getOutputDir();
        FileUtils.emptyFolder(destinationDir);

        List<ResourceSet> resourceSets = getConfiguredResourceSets();

        // create a new merger and populate it with the sets.
        ResourceMerger merger = new ResourceMerger();

        try {
            for (ResourceSet resourceSet : resourceSets) {
                resourceSet.loadFromFiles(getILogger());
                merger.addDataSet(resourceSet);
            }

            // get the merged set and write it down.
            MergedResourceWriter writer = new MergedResourceWriter(
                    destinationDir, getCruncher(),
                    getCrunchPng(), getProcess9Patch(), getPublicFile(), preprocessor);
            writer.setInsertSourceMarkers(getInsertSourceMarkers());

            merger.mergeData(writer, false /*doCleanUp*/);

            // No exception? Write the known state.
            merger.writeBlobTo(getIncrementalFolder(), writer);
            throw new StopExecutionException("Stop for now.");
        } catch (MergingException e) {
            System.out.println(e.getMessage());
            merger.cleanBlob(getIncrementalFolder());
            throw new ResourceException(e.getMessage(), e);
        }
    }

    @Override
    protected void doIncrementalTaskAction(Map<File, FileStatus> changedInputs) throws IOException {
        // create a merger and load the known state.
        ResourceMerger merger = new ResourceMerger();
        try {
            if (!merger.loadFromBlob(getIncrementalFolder(), true /*incrementalState*/)) {
                doFullTaskAction();
                return;
            }

            for (ResourceSet resourceSet : merger.getDataSets()) {
                resourceSet.setNormalizeResources(normalizeResources);
                resourceSet.setPreprocessor(preprocessor);
            }

            List<ResourceSet> resourceSets = getConfiguredResourceSets();

            // compare the known state to the current sets to detect incompatibility.
            // This is in case there's a change that's too hard to do incrementally. In this case
            // we'll simply revert to full build.
            if (!merger.checkValidUpdate(resourceSets)) {
                getLogger().info("Changed Resource sets: full task run!");
                doFullTaskAction();
                return;
            }

            // The incremental process is the following:
            // Loop on all the changed files, find which ResourceSet it belongs to, then ask
            // the resource set to update itself with the new file.
            for (Map.Entry<File, FileStatus> entry : changedInputs.entrySet()) {
                File changedFile = entry.getKey();

                merger.findDataSetContaining(changedFile, fileValidity);
                if (fileValidity.getStatus() == FileValidity.FileStatus.UNKNOWN_FILE) {
                    doFullTaskAction();
                    return;
                } else if (fileValidity.getStatus() == FileValidity.FileStatus.VALID_FILE) {
                    if (!fileValidity.getDataSet().updateWith(
                            fileValidity.getSourceFile(), changedFile, entry.getValue(),
                            getILogger())) {
                        getLogger().info(
                                String.format("Failed to process %s event! Full task run",
                                        entry.getValue()));
                        doFullTaskAction();
                        return;
                    }
                }
            }

            MergedResourceWriter writer = new MergedResourceWriter(
                    getOutputDir(), getCruncher(),
                    getCrunchPng(), getProcess9Patch(), getPublicFile(), preprocessor);
            writer.setInsertSourceMarkers(getInsertSourceMarkers());
            merger.mergeData(writer, false /*doCleanUp*/);
            // No exception? Write the known state.
            merger.writeBlobTo(getIncrementalFolder(), writer);
        } catch (MergingException e) {
            merger.cleanBlob(getIncrementalFolder());
            throw new ResourceException(e.getMessage(), e);
        } finally {
            // some clean up after the task to help multi variant/module builds.
            fileValidity.clear();
        }
    }

    @NonNull
    private List<ResourceSet> getConfiguredResourceSets() {
        List<ResourceSet> resourceSets = Lists.newArrayList(getInputResourceSets());
        List<ResourceSet> generatedSets = Lists.newArrayListWithCapacity(resourceSets.size());

        for (ResourceSet resourceSet : resourceSets) {
            resourceSet.setNormalizeResources(normalizeResources);
            resourceSet.setPreprocessor(preprocessor);
            ResourceSet generatedSet = new GeneratedResourceSet(resourceSet);
            resourceSet.setGeneratedSet(generatedSet);
            generatedSets.add(generatedSet);
        }

        // Put all generated sets at the start of the list.
        resourceSets.addAll(0, generatedSets);
        return resourceSets;
    }

    @Input
    public boolean isProcess9Patch() {
        return process9Patch;
    }

    @Input
    public boolean isCrunchPng() {
        return crunchPng;
    }

    @Input
    public boolean isUseNewCruncher() {
        return useNewCruncher;
    }

    @Input
    public boolean isInsertSourceMarkers() {
        return insertSourceMarkers;
    }

    @Input
    public boolean isNormalizeResources() {
        return normalizeResources;
    }

    public void setNormalizeResources(boolean normalizeResources) {
        this.normalizeResources = normalizeResources;
    }

    public List<ResourceSet> getInputResourceSets() {
        return inputResourceSets;
    }

    public void setInputResourceSets(
            List<ResourceSet> inputResourceSets) {
        this.inputResourceSets = inputResourceSets;
    }

    public boolean getUseNewCruncher() {
        return useNewCruncher;
    }

    public void setUseNewCruncher(boolean useNewCruncher) {
        this.useNewCruncher = useNewCruncher;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public boolean getCrunchPng() {
        return crunchPng;
    }

    public void setCrunchPng(boolean crunchPng) {
        this.crunchPng = crunchPng;
    }

    public boolean getProcess9Patch() {
        return process9Patch;
    }

    public void setProcess9Patch(boolean process9Patch) {
        this.process9Patch = process9Patch;
    }

    @Optional
    @OutputFile
    public File getPublicFile() {
        return publicFile;
    }

    public void setPublicFile(File publicFile) {
        this.publicFile = publicFile;
    }

    public boolean getInsertSourceMarkers() {
        return insertSourceMarkers;
    }

    public void setInsertSourceMarkers(boolean insertSourceMarkers) {
        this.insertSourceMarkers = insertSourceMarkers;
    }

    public static class ConfigAction implements TaskConfigAction<MergeResources> {

        @NonNull
        private VariantScope scope;

        @NonNull
        private String taskNamePrefix;

        @Nullable
        private File outputLocation;

        private boolean includeDependencies;

        private boolean process9Patch;

        public ConfigAction(
                @NonNull VariantScope scope,
                @NonNull String taskNamePrefix,
                @Nullable File outputLocation,
                boolean includeDependencies,
                boolean process9Patch) {
            this.scope = scope;
            this.taskNamePrefix = taskNamePrefix;
            this.outputLocation = outputLocation;
            this.includeDependencies = includeDependencies;
            this.process9Patch = process9Patch;

            scope.setMergeResourceOutputDir(outputLocation);
        }

        @Override
        public String getName() {
            return scope.getTaskName(taskNamePrefix, "Resources");
        }

        @Override
        public Class<MergeResources> getType() {
            return MergeResources.class;
        }

        @Override
        public void execute(MergeResources mergeResourcesTask) {
            final BaseVariantData<? extends BaseVariantOutputData> variantData =
                    scope.getVariantData();
            final AndroidConfig extension = scope.getGlobalScope().getExtension();

            mergeResourcesTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            mergeResourcesTask.setVariantName(scope.getVariantConfiguration().getFullName());
            mergeResourcesTask.setIncrementalFolder(new File(
                    scope.getGlobalScope().getBuildDir() + "/" + AndroidProject.FD_INTERMEDIATES +
                            "/incremental/" + taskNamePrefix + "Resources/" +
                            variantData.getVariantConfiguration().getDirName()));

            mergeResourcesTask.process9Patch = process9Patch;
            mergeResourcesTask.crunchPng = extension.getAaptOptions()
                    .getCruncherEnabled();
            mergeResourcesTask.normalizeResources =
                    extension.getBuildToolsRevision()
                            .compareTo(NORMALIZE_RESOURCES_BUILD_TOOLS) < 0;

            // Only one pre-processor for now. The code will need slight changes when we add more.
            mergeResourcesTask.preprocessor = new VectorDrawableRenderer(
                    scope.getGeneratedPngsOutputDir(),
                    extension.getPreprocessingOptions().getTypedDensities(),
                    mergeResourcesTask.getILogger());

            ConventionMappingHelper.map(mergeResourcesTask, "useNewCruncher",
                    new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return extension.getAaptOptions()
                                    .getUseNewCruncher();
                        }
                    });

            ConventionMappingHelper.map(mergeResourcesTask, "inputResourceSets",
                    new Callable<List<ResourceSet>>() {
                        @Override
                        public List<ResourceSet> call() throws Exception {
                            List<File> generatedResFolders = Lists.newArrayList(
                                    scope.getRenderscriptResOutputDir(),
                                    scope.getGeneratedResOutputDir());
                            if (variantData.getExtraGeneratedResFolders() != null) {
                                generatedResFolders.addAll(
                                        variantData.getExtraGeneratedResFolders());
                            }
                            if (variantData.generateApkDataTask != null &&
                                    variantData.getVariantConfiguration().getBuildType()
                                            .isEmbedMicroApp()) {
                                generatedResFolders.add(
                                        variantData.generateApkDataTask.getResOutputDir());
                            }
                            return variantData.getVariantConfiguration()
                                    .getResourceSets(generatedResFolders, includeDependencies);
                        }
                    });
            ConventionMappingHelper.map(mergeResourcesTask, "outputDir", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return outputLocation != null ? outputLocation
                            : scope.getDefaultMergeResourcesOutputDir();
                }
            });

            variantData.mergeResourcesTask = mergeResourcesTask;
        }
    }
}


