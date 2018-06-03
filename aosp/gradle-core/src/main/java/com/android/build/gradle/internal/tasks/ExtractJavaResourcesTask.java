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
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.ide.common.packaging.PackagingUtils;
import com.android.utils.FileUtils;
import com.google.common.io.ByteStreams;

import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extract all packaged jar files java resources into a directory. Each jar file will be extracted
 * in a jar specific folder, and only java resources are extracted.
 */
public class ExtractJavaResourcesTask extends DefaultAndroidTask {

    // the fact we use a SET is not right, we should have an ordered list of jars...
    // VariantConfiguration.getPackaged|ProvidedJars should use List<>
    @InputFiles
    public Set<File> jarInputFiles;

    @OutputDirectory
    public File outputDir;

    @InputFiles
    public Set<File> getJarInputFiles() {
        return jarInputFiles;
    }

    @TaskAction
    public void extractJavaResources(final IncrementalTaskInputs incrementalTaskInputs) {

        incrementalTaskInputs.outOfDate(new org.gradle.api.Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails inputFileDetails) {
                File inputJar = inputFileDetails.getFile();
                String folderName = inputJar.getName() +
                        inputJar.getPath().hashCode();

                File outputFolder = new File(outputDir, folderName);
                if (outputFolder.exists()) {
                    try {
                        FileUtils.deleteFolder(outputFolder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!outputFolder.mkdirs()) {
                    throw new RuntimeException(
                            "Cannot create folder to extract java resources in for "
                                    + inputJar.getAbsolutePath());
                }

                // create the jar file visitor that will check for out-dated resources.

                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(inputJar);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        if (!jarEntry.isDirectory()) {
                            processJarEntry(jarFile, jarEntry, outputFolder);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            // ignore.
                        }
                    }
                }
            }
        });

        incrementalTaskInputs.removed(new org.gradle.api.Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails inputFileDetails) {
                File deletedJar = inputFileDetails.getFile();
                String folderName = deletedJar.getName() +
                        deletedJar.getPath().hashCode();
                File outputFolder = new File(outputDir, folderName);
                if (outputFolder.exists()) {
                    try {
                        FileUtils.deleteFolder(outputFolder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * process one jar entry in an input jar file and optionally stores the entry in the output
     * folder.
     * @param jarFile the input jar file
     * @param jarEntry the jar entry in the jarFile to process
     * @param outputDir the output folder to use to copy/merge the entry in.
     * @throws IOException
     */
    private static void processJarEntry(JarFile jarFile, JarEntry jarEntry, File outputDir) throws IOException {
        File outputFile = new File(outputDir, jarEntry.getName());
        Action action = getAction(jarEntry.getName());
        if (action == Action.COPY) {
            if (!outputFile.getParentFile().exists() &&
                    !outputFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Cannot create directory " + outputFile.getParent());
            }
            if (!outputFile.exists() || outputFile.lastModified()
                    < jarEntry.getTime()) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = jarFile.getInputStream(jarEntry);
                    if (inputStream != null) {
                        outputStream = new BufferedOutputStream(
                                new FileOutputStream(outputFile));
                        ByteStreams.copy(inputStream, outputStream);
                        outputStream.flush();
                    } else {
                        throw new RuntimeException("Cannot copy " + jarEntry.getName());
                    }
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Define all possible actions for a Jar file entry.
     */
    enum Action {
        /**
         * Copy the file to the output destination.
         */
        COPY,
        /**
         * Ignore the file.
         */
        IGNORE
    }

    /**
     * Provides an {@link Action} for the archive entry.
     * @param archivePath the archive entry path in the archive.
     * @return the action to implement.
     */
    @NonNull
    public static Action getAction(@NonNull String archivePath) {
        // Manifest files are never merged.
        if (JarFile.MANIFEST_NAME.equals(archivePath)) {
            return Action.IGNORE;
        }

        // split the path into segments.
        String[] segments = archivePath.split("/");

        // empty path? skip to next entry.
        if (segments.length == 0) {
            return Action.IGNORE;
        }

        // Check each folders to make sure they should be included.
        // Folders like CVS, .svn, etc.. should already have been excluded from the
        // jar file, but we need to exclude some other folder (like /META-INF) so
        // we check anyway.
        for (int i = 0 ; i < segments.length - 1; i++) {
            if (!PackagingUtils.checkFolderForPackaging(segments[i])) {
                return Action.IGNORE;
            }
        }

        // get the file name from the path
        String fileName = segments[segments.length-1];

        return PackagingUtils.checkFileForPackaging(fileName)
                ? Action.COPY
                : Action.IGNORE;
    }

    public static class Config implements TaskConfigAction<ExtractJavaResourcesTask> {

        private final VariantScope scope;

        public Config(VariantScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("extract", "PackagedLibrariesJavaResources");
        }

        @Override
        public Class<ExtractJavaResourcesTask> getType() {
            return ExtractJavaResourcesTask.class;
        }

        @Override
        public void execute(ExtractJavaResourcesTask extractJavaResourcesTask) {
            ConventionMappingHelper.map(extractJavaResourcesTask, "jarInputFiles",
                    new Callable<Set<File>>() {

                    @Override
                    public Set<File> call() throws Exception {
                        return scope.getVariantConfiguration().getPackagedJars();
                    }
                });
            extractJavaResourcesTask.outputDir = scope.getPackagedJarsJavaResDestinationDir();
            extractJavaResourcesTask.setVariantName(scope.getVariantConfiguration().getFullName());
        }
    }
}
