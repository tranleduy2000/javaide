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

package com.android.build.gradle.internal.tasks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.tasks.JavaResourcesProvider;
import com.android.builder.model.PackagingOptions;
import com.android.builder.signing.SignedJarBuilder;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Merges java resources from temporary expansion folders (created from the packaged jars
 * resources and source folder java resources) into a single output directory that can be used
 * during obfuscation and packaging.
 *
 * This task is the default {@link JavaResourcesProvider} to provide merged java resources to
 * the final variant packaging step. However, if the variant obfuscation is turned on, some of
 * these resources packages might need to be adapted to match the obfuscated code. In such
 * a scenario, the {@link JavaResourcesProvider} will become the task responsible for obfuscation.
 */
@ParallelizableTask
public class MergeJavaResourcesTask extends DefaultAndroidTask implements JavaResourcesProvider {

    @Nested
    @Optional
    @Nullable
    public PackagingOptions packagingOptions;

    @InputDirectory
    @Optional
    @Nullable
    public File getSourceJavaResourcesFolder() {
        return sourceJavaResourcesFolder;
    }

    @InputDirectory
    @Optional
    @Nullable
    public File getPackagedJarsJavaResourcesFolder() {
        return packagedJarsJavaResourcesFolder;
    }

    @Nullable
    private FileFilter packagingOptionsFilter;

    @SuppressWarnings({"UnusedDeclaration"})
    @Nullable
    private File sourceJavaResourcesFolder;
    @SuppressWarnings({"UnusedDeclaration"})
    @Nullable
    private File packagedJarsJavaResourcesFolder;

    @SuppressWarnings({"UnusedDeclaration"})
    @Nullable
    private File outputDir;

    @OutputDirectory
    @Nullable
    public File getOutputDir() {
        return outputDir;
    }

    public List<File> getExpandedFolders() {
        ImmutableList.Builder<File> builder = ImmutableList.builder();
        if (getSourceJavaResourcesFolder() != null) {
            builder.add(getSourceJavaResourcesFolder());
        }
        if (getPackagedJarsJavaResourcesFolder() != null) {
            builder.add(getPackagedJarsJavaResourcesFolder());
        }
        return builder.build();
    }

    @TaskAction
    void extractJavaResources(IncrementalTaskInputs incrementalTaskInputs) {

        if (packagingOptionsFilter == null || getOutputDir() == null) {
            throw new RuntimeException(
                    "Internal error, packagingOptionsFilter or outputDir is null");
        }
        incrementalTaskInputs.outOfDate(new org.gradle.api.Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails inputFileDetails) {
                try {
                    packagingOptionsFilter.handleChanged(
                            getOutputDir(), inputFileDetails.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        incrementalTaskInputs.removed(new org.gradle.api.Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails inputFileDetails) {
                try {
                    packagingOptionsFilter.handleRemoved
                            (getOutputDir(), inputFileDetails.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    /**
     * Defines a file filter contract which will use {@link PackagingOptions} to take appropriate
     * action.
     */
    @VisibleForTesting
    static final class FileFilter implements SignedJarBuilder.IZipEntryFilter {

        /**
         * User's setting for a particular archive entry. This is expressed in the build.gradle
         * DSL and used by this filter to determine file merging behaviors.
         */
        private enum PackagingOption {
            /**
             * no action was described for archive entry.
             */
            NONE,
            /**
             * merge all archive entries with the same archive path.
             */
            MERGE,
            /**
             * pick to first archive entry with that archive path (not stable).
             */
            PICK_FIRST,
            /**
             * exclude all archive entries with that archive path.
             */
            EXCLUDE
        }

        @Nullable
        private final PackagingOptions packagingOptions;
        @NonNull
        private final Set<String> excludes;
        @NonNull
        private final Set<String> pickFirsts;
        @NonNull
        private final MergeJavaResourcesTask owner;

        public FileFilter(@NonNull MergeJavaResourcesTask owner,
                @Nullable PackagingOptions packagingOptions) {
            this.owner = owner;
            this.packagingOptions = packagingOptions;
            excludes = this.packagingOptions != null ? this.packagingOptions.getExcludes() :
                    Collections.<String>emptySet();
            pickFirsts = this.packagingOptions != null ? this.packagingOptions.getPickFirsts() :
                    Collections.<String>emptySet();
        }

        /**
         * Implementation of the {@link SignedJarBuilder.IZipEntryFilter} contract which only
         * cares about copying or ignoring files since merging is handled differently.
         * @param archivePath the archive file path of the entry
         * @return true if the archive entry satisfies the filter, false otherwise.
         * @throws ZipAbortException
         */
        @Override
        public boolean checkEntry(@NonNull String archivePath)
                throws ZipAbortException {
            PackagingOption packagingOption = getPackagingAction(archivePath);
            switch(packagingOption) {
                case EXCLUDE:
                    return false;
                case PICK_FIRST:
                    List<File> allFiles = getAllFiles(archivePath);
                    return allFiles.isEmpty();
                case MERGE:
                case NONE:
                    return true;
                default:
                    throw new RuntimeException("Unhandled PackagingOption " + packagingOption);
            }
        }

        /**
         * Notification of an incremental file changed since last successful run of the task.
         *
         * Usually, we just copy the changed file into the merged folder. However, if the user
         * specified {@link PackagingOption#PICK_FIRST}, the file will only be copied if it the
         * first pick. Also, if the user specified {@link PackagingOption#MERGE}, all the files
         * with the same entry archive path will be re-merged.
         *
         * @param outputDir merged resources folder.
         * @param changedFile changed file located in a temporary expansion folder
         * @throws IOException
         */
        void handleChanged(@NonNull File outputDir, @NonNull File changedFile)
                throws IOException {
            String archivePath = getArchivePath(changedFile);
            PackagingOption packagingOption = getPackagingAction(archivePath);
            switch (packagingOption) {
                case EXCLUDE:
                    return;
                case MERGE:
                    // one of the merged file has changed, re-merge all of them.
                    mergeAll(outputDir, archivePath);
                    return;
                case PICK_FIRST:
                    copy(changedFile, outputDir, archivePath);
                    return;
                case NONE:
                    copy(changedFile, outputDir, archivePath);
            }
        }

        /**
         * Notification of a file removal.
         *
         * file was removed, we need to check that it was not a pickFirst item (since we
         * may now need to pick another one) or a merged item since we would need to re-merge
         * all remaining items.
         *
         * @param outputDir expected merged output directory.
         * @param removedFile removed file from the temporary resources folders.
         * @throws IOException
         */
        public void handleRemoved(@NonNull File outputDir, @NonNull File removedFile)
                throws IOException {


            String archivePath = getArchivePath(removedFile);
            // first delete the output file, it will be eventually replaced.
            File outFile = new File(outputDir, archivePath);
            if (outFile.exists()) {
                if (!outFile.delete()) {
                    throw new IOException("Cannot delete " + outFile.getAbsolutePath());
                }
            }
            FileFilter.PackagingOption itemPackagingOption = getPackagingAction(archivePath);

            switch(itemPackagingOption) {
                case PICK_FIRST:
                    // this was a picked up item, make sure we copy the first still available
                    com.google.common.base.Optional<File> firstPick = getFirstPick(archivePath);
                    if (firstPick.isPresent()) {
                        copy(firstPick.get(), outputDir, archivePath);
                    }
                    return;
                case MERGE:
                    // re-merge all
                    mergeAll(outputDir, archivePath);
                    return;
                case EXCLUDE:
                case NONE:
                    // do nothing
                    return;
                default:
                    throw new RuntimeException("Unhandled package option"
                            + itemPackagingOption);

            }
        }

        private static void copy(@NonNull File inputFile,
                @NonNull File outputDir,
                @NonNull String archivePath) throws IOException {

            File outputFile = new File(outputDir, archivePath);
            createParentFolderIfNecessary(outputFile);
            Files.copy(inputFile, outputFile);
        }

        private void mergeAll(@NonNull File outputDir, @NonNull String archivePath)
                throws IOException {

            File outputFile = new File(outputDir, archivePath);
            if (outputFile.exists() && !outputFile.delete()) {
                throw new RuntimeException("Cannot delete " + outputFile);
            }
            createParentFolderIfNecessary(outputFile);
            List<File> allFiles = getAllFiles(archivePath);
            if (!allFiles.isEmpty()) {
                OutputStream os = null;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(outputFile));
                    // take each file in order and merge them.
                    for (File file : allFiles) {
                        Files.copy(file, os);
                    }
                } finally {
                    if (os != null) {
                        os.close();
                    }
                }
            }
        }

        private static void createParentFolderIfNecessary(@NonNull File outputFile) {
            File parentFolder = outputFile.getParentFile();
            if (!parentFolder.exists()) {
                if (!parentFolder.mkdirs()) {
                    throw new RuntimeException("Cannot create folder " + parentFolder);
                }
            }
        }

        /**
         * Return the first file from the temporary expansion folders that satisfy the archive path.
         * @param archivePath the entry archive path.
         * @return the first file reference of {@link com.google.common.base.Optional#absent()} if
         * none exist in any temporary expansion folders.
         */
        @NonNull
        private com.google.common.base.Optional<File> getFirstPick(
                @NonNull final String archivePath) {

            return com.google.common.base.Optional.fromNullable(
                    forEachExpansionFolder(new FolderAction() {
                        @Nullable
                        @Override
                        public File on(File folder) {
                            File expandedFile = new File(folder, archivePath);
                            if (expandedFile.exists()) {
                                return expandedFile;
                            }
                            return null;
                        }
                    }));
        }

        /**
         * Returns all files from temporary expansion folders with the same archive path.
         * @param archivePath the entry archive path.
         * @return a list possibly empty of {@link File}s that satisfy the archive path.
         */
        @NonNull
        private List<File> getAllFiles(@NonNull final String archivePath) {
            final ImmutableList.Builder<File> matchingFiles = ImmutableList.builder();
            forEachExpansionFolder(new FolderAction() {
                @Nullable
                @Override
                public File on(File folder) {
                    File expandedFile = new File(folder, archivePath);
                    if (expandedFile.exists()) {
                        matchingFiles.add(expandedFile);
                    }
                    return null;
                }
            });
            return matchingFiles.build();
        }

        /**
         * An action on a folder.
         */
        private interface FolderAction {

            /**
             * Perform an action on a folder and stop the processing if something is returned
             * @param folder the folder to perform the action on.
             * @return a file to stop processing or null to continue to the next expansion folder
             * if any.
             */
            @Nullable
            File on(File folder);
        }

        /**
         * Perform the passed action on each expansion folder.
         * @param action the action to perform on each folder.
         * @return a file if any action returned a value, or null if none returned a value.
         */
        @Nullable
        private File forEachExpansionFolder(@NonNull FolderAction action) {
            for (File expansionParentFolder : owner.getExpandedFolders()) {
                File[] expansionFolders = expansionParentFolder.listFiles();
                if (expansionFolders != null) {
                    for (File expansionFolder : expansionFolders) {
                        if (expansionFolder.isDirectory()) {
                            File value = action.on(expansionFolder);
                            if (value != null) {
                                return value;
                            }
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Returns the expansion folder for an expanded file. This represents the location
         * where the packaged jar our source directories java resources were extracted into.
         * @param expandedFile the java resource file.
         * @return the expansion folder used to extract the java resource into.
         */
        @NonNull
        private File getExpansionFolder(@NonNull final File expandedFile) {
            File expansionFolder = forEachExpansionFolder(new FolderAction() {
                @Nullable
                @Override
                public File on(File folder) {
                    return expandedFile.getAbsolutePath().startsWith(folder.getAbsolutePath())
                            ? folder : null;
                    }
                });
            if (expansionFolder == null) {
                throw new RuntimeException("Cannot determine expansion folder for " + expandedFile
                        + " with folders "  + Joiner.on(",").join(owner.getExpandedFolders()));
            }
            return expansionFolder;
        }

        /**
         * Determines the archive entry path relative to its expansion folder. The archive entry
         * path is the path that was used to save the entry in the original .jar file that got
         * expanded in the expansion folder.
         * @param expandedFile the expanded file to find the relative archive entry from.
         * @return the expanded file relative path to its expansion folder.
         */
        @NonNull
        private String getArchivePath(@NonNull File expandedFile) {
            File expansionFolder = getExpansionFolder(expandedFile);
            return expandedFile.getAbsolutePath()
                    .substring(expansionFolder.getAbsolutePath().length() + 1);
        }

        /**
         * Determine the user's intention for a particular archive entry.
         * @param archivePath the archive entry
         * @return a {@link FileFilter.PackagingOption} as provided by the user in the build.gradle
         */
        @NonNull
        private PackagingOption getPackagingAction(@NonNull String archivePath) {
            if (packagingOptions != null) {
                if (pickFirsts.contains(archivePath)) {
                    return PackagingOption.PICK_FIRST;
                }
                if (packagingOptions.getMerges().contains(archivePath)) {
                    return PackagingOption.MERGE;
                }
                if (excludes.contains(archivePath)) {
                    return PackagingOption.EXCLUDE;
                }
            }
            return PackagingOption.NONE;
        }
    }

    @NonNull
    @Override
    public ImmutableList<JavaResourcesLocation> getJavaResourcesLocations() {
        return ImmutableList.of(new JavaResourcesLocation(Type.FOLDER, getOutputDir()));
    }

    public static class Config implements TaskConfigAction<MergeJavaResourcesTask> {

        private final VariantScope scope;

        public Config(VariantScope variantScope) {
            this.scope = variantScope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("merge", "JavaResources");
        }

        @Override
        public Class<MergeJavaResourcesTask> getType() {
            return MergeJavaResourcesTask.class;
        }

        @Override
        public void execute(MergeJavaResourcesTask mergeJavaResourcesTask) {
            mergeJavaResourcesTask.setVariantName(scope.getVariantConfiguration().getFullName());

            ConventionMappingHelper.map(mergeJavaResourcesTask, "sourceJavaResourcesFolder",
                    new Callable<File>() {
                        @Override
                        public File call() throws Exception {
                            return scope.getSourceFoldersJavaResDestinationDir().exists()
                                    ? scope.getSourceFoldersJavaResDestinationDir()
                                    : null;
                        }
                    });

            ConventionMappingHelper.map(mergeJavaResourcesTask, "packagedJarsJavaResourcesFolder",
                    new Callable<File>() {
                        @Override
                        public File call() throws Exception {
                            return scope.getPackagedJarsJavaResDestinationDir().exists()
                                ? scope.getPackagedJarsJavaResDestinationDir()
                                : null;
                        }
                    });

            File outputDir = scope.getJavaResourcesDestinationDir();
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                throw new RuntimeException("Cannot create output directory " + outputDir);
            }
            mergeJavaResourcesTask.outputDir = outputDir;

            PackagingOptions packagingOptions =
                    scope.getGlobalScope().getExtension().getPackagingOptions();
            mergeJavaResourcesTask.packagingOptionsFilter =
                    new FileFilter(mergeJavaResourcesTask, packagingOptions);
            mergeJavaResourcesTask.packagingOptions = packagingOptions;
            scope.setPackagingOptionsFilter(mergeJavaResourcesTask.packagingOptionsFilter);
        }
    }
}

