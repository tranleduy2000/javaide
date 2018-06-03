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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.build.gradle.api.ApkOutputFile;
import com.android.build.gradle.internal.model.FilterDataImpl;
import com.android.builder.model.SigningConfig;
import com.android.builder.packaging.SigningException;
import com.android.builder.signing.SignedJarBuilder;
import com.android.ide.common.signing.KeytoolException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Callables;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package each split resources into a specific signed apk file.
 */
@ParallelizableTask
public class PackageSplitRes extends SplitRelatedTask {

    private Set<String> densitySplits;

    private Set<String> languageSplits;

    private String outputBaseName;

    private SigningConfig signingConfig;

    /**
     * This directories are not officially input/output to the task as they are shared among tasks.
     * To be parallelizable, we must only define our I/O in terms of files...
     */
    private File inputDirectory;

    private File outputDirectory;

    @InputFiles
    public List<File> getInputFiles() {
        final ImmutableList.Builder<File> builder = ImmutableList.builder();
        forEachInputFile(new SplitFileHandler() {
            @Override
            public void execute(String split, File file) {
                builder.add(file);
            }
        });
        return builder.build();
    }

    @OutputFiles
    public List<File> getOutputFiles() {
        ImmutableList.Builder<File> builder = ImmutableList.builder();
        for (ApkOutputFile apk : getOutputSplitFiles()) {
            builder.add(apk.getOutputFile());
        }
        return builder.build();
    }

    @Override
    public File getApkMetadataFile() {
        return null;
    }

    /**
     * Calculates the list of output files, coming from the list of input files, mangling the output
     * file name.
     */
    @Override
    public List<ApkOutputFile> getOutputSplitFiles() {
        final ImmutableList.Builder<ApkOutputFile> builder = ImmutableList.builder();
        forEachInputFile(new SplitFileHandler() {
            @Override
            public void execute(String split, File file) {
                // find the split identification, if null, the split is not requested any longer.
                FilterData filterData = null;
                for (String density : densitySplits) {
                    if (split.startsWith(density)) {
                        filterData = FilterDataImpl.build(
                                OutputFile.FilterType.DENSITY.toString(), density);
                    }

                }

                if (languageSplits.contains(unMangleSplitName(split))) {
                    filterData = FilterDataImpl.build(
                            OutputFile.FilterType.LANGUAGE.toString(), unMangleSplitName(split));
                }
                if (filterData != null) {
                    builder.add(new ApkOutputFile(
                            OutputFile.OutputType.SPLIT,
                            ImmutableList.of(filterData),
                            Callables.returning(
                                    new File(outputDirectory, getOutputFileNameForSplit(split)))));
                }

            }
        });
        return builder.build();
    }

    @TaskAction
    protected void doFullTaskAction() {
        forEachInputFile(
                new SplitFileHandler() {
                    @Override
                    public void execute(String split, File file) {
                            File outFile = new File(outputDirectory,
                                    getOutputFileNameForSplit(split));
                        try {
                            getBuilder().signApk(file, signingConfig, outFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (KeytoolException e) {
                            throw new RuntimeException(e);
                        } catch (SigningException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        } catch (SignedJarBuilder.IZipEntryFilter.ZipAbortException e) {
                            throw new RuntimeException(e);
                        } catch (com.android.builder.signing.SigningException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private interface SplitFileHandler {
        void execute(String split, File file);
    }

    /**
     * Runs the handler for each task input file, providing the split identifier (possibly with a
     * suffix generated by aapt) and the input file handle.
     */
    private void forEachInputFile(SplitFileHandler handler) {
        Pattern resourcePattern = Pattern.compile("resources-" + outputBaseName + ".ap__(.*)");

        // make a copy of the expected densities and languages filters.
        List<String> densitiesCopy = Lists.newArrayList(densitySplits);
        List<String> languagesCopy = Lists.newArrayList(languageSplits);

        // resources- and .ap_ should be shared in a setting somewhere. see BasePlugin:1206
        File[] fileLists = inputDirectory.listFiles();
        if (fileLists != null) {
            for (File file : fileLists) {
                Matcher match = resourcePattern.matcher(file.getName());
                // each time we match, we remove the associated filter from our copies.
                if (match.matches() && !match.group(1).isEmpty()
                        && isValidSplit(densitiesCopy, languagesCopy, match.group(1))) {
                    handler.execute(match.group(1), file);
                }
            }
        }
        // manually invoke the handler for filters we did not find associated files, apply best
        // guess on the actual file names.
        for (String density : densitiesCopy) {
            handler.execute(density,
                    new File(inputDirectory, "resources-" + outputBaseName + ".ap__" + density));
        }
        for (String language : languagesCopy) {
            handler.execute(language,
                    new File(inputDirectory, "resources-" + outputBaseName + ".ap__" + language));

        }
    }

    /**
     * Returns true if the passed split identifier is a valid identifier (valid mean it is a
     * requested split for this task). A density split identifier can be suffixed with characters
     * added by aapt.
     */
    private static boolean isValidSplit(
            List<String> densities,
            List<String> languages,
            @NonNull String splitWithOptionalSuffix) {
        for (String density : densities) {
            if (splitWithOptionalSuffix.startsWith(density)) {
                densities.remove(density);
                return true;
            }
        }
        String mangledName = unMangleSplitName(splitWithOptionalSuffix);
        if (languages.contains(mangledName)) {
            languages.remove(mangledName);
            return true;
        }
        return false;
    }

    public String getOutputFileNameForSplit(final String split) {
        String archivesBaseName = (String)getProject().getProperties().get("archivesBaseName");
        String apkName = archivesBaseName + "-" + outputBaseName + "_" + split;
        return apkName + (signingConfig == null ? "-unsigned.apk" : "-unaligned.apk");
    }

    @Override
    public List<FilterData> getSplitsData() {
        ImmutableList.Builder<FilterData> filterDataBuilder = ImmutableList.builder();
        addAllFilterData(filterDataBuilder, densitySplits, OutputFile.FilterType.DENSITY);
        addAllFilterData(filterDataBuilder, languageSplits, OutputFile.FilterType.LANGUAGE);
        return filterDataBuilder.build();
    }

    /**
     * Un-mangle a split name as created by the aapt tool to retrieve a split name as configured in
     * the project's build.gradle.
     *
     * when dealing with several split language in a single split, each language (+ optional region)
     * will be seperated by an underscore.
     *
     * note that there is currently an aapt bug, remove the 'r' in the region so for instance,
     * fr-rCA becomes fr-CA, temporarily put it back until it is fixed.
     *
     * @param splitWithOptionalSuffix the mangled split name.
     */
    public static String unMangleSplitName(String splitWithOptionalSuffix) {
        String mangledName = splitWithOptionalSuffix.replaceAll("_", ",");
        return mangledName.contains("-r") ? mangledName : mangledName.replace("-", "-r");
    }

    @Input
    public Set<String> getDensitySplits() {
        return densitySplits;
    }

    public void setDensitySplits(Set<String> densitySplits) {
        this.densitySplits = densitySplits;
    }

    @Input
    public Set<String> getLanguageSplits() {
        return languageSplits;
    }

    public void setLanguageSplits(Set<String> languageSplits) {
        this.languageSplits = languageSplits;
    }

    @Input
    public String getOutputBaseName() {
        return outputBaseName;
    }

    public void setOutputBaseName(String outputBaseName) {
        this.outputBaseName = outputBaseName;
    }

    @Nested
    @Optional
    public SigningConfig getSigningConfig() {
        return signingConfig;
    }

    public void setSigningConfig(SigningConfig signingConfig) {
        this.signingConfig = signingConfig;
    }

    public File getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
