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

package com.android.build.gradle.tasks

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.FilterData
import com.android.build.OutputFile.FilterType
import com.android.build.OutputFile.OutputType
import com.android.build.gradle.api.ApkOutputFile
import com.android.build.gradle.internal.model.FilterDataImpl
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Callables
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Task to zip align all the splits
 */
@ParallelizableTask
class SplitZipAlign extends SplitRelatedTask {

    @InputFiles
    List<File> densityOrLanguageInputFiles = new ArrayList<>();

    @InputFiles
    List<File> abiInputFiles = new ArrayList<>()

    @Input
    String outputBaseName;

    @Input
    Set<String> densityFilters;

    @Input
    Set<String> abiFilters;

    @Input
    Set<String> languageFilters;

    File outputDirectory;

    @InputFile
    File zipAlignExe

    @OutputFiles
    public List<File> getOutputFiles() {
        getOutputSplitFiles()*.getOutputFile()
    }

    @OutputFile
    @Nullable
    File apkMetadataFile

    @NonNull
    List<File> getInputFiles() {
        return getDensityOrLanguageInputFiles() + getAbiInputFiles();
    }

    @NonNull
    public synchronized  ImmutableList<ApkOutputFile> getOutputSplitFiles() {

        ImmutableList.Builder<ApkOutputFile> outputFiles = ImmutableList.builder();
        Closure addingLogic = { String split, File file ->
            outputFiles.add(new ApkOutputFile(OutputType.SPLIT,
                    ImmutableList.<FilterData>of(
                            FilterDataImpl.build(
                                    getFilterType(split).toString(), getFilter(split))),
                    Callables.<File>returning(
                            new File(outputDirectory,
                                    "${project.archivesBaseName}-${outputBaseName}_${split}.apk"))))
        }

        forEachUnalignedInput(addingLogic)
        forEachUnsignedInput(addingLogic)
        return outputFiles.build()
    }

    FilterType getFilterType(String filter) {
        String languageName = PackageSplitRes.unMangleSplitName(filter);
        if (languageFilters.contains(languageName)) {
            return FilterType.LANGUAGE
        }
        if (abiFilters.contains(filter)) {
            return FilterType.ABI
        }
        return FilterType.DENSITY
    }

    String getFilter(String filterWithPossibleSuffix) {
        FilterType type = getFilterType(filterWithPossibleSuffix)
        if (type == FilterType.DENSITY) {
            for (String density : densityFilters) {
                if (filterWithPossibleSuffix.startsWith(density)) {
                    return density
                }
            }
        }
        if (type == FilterType.LANGUAGE) {
            return PackageSplitRes.unMangleSplitName(filterWithPossibleSuffix)
        }
        return filterWithPossibleSuffix
    }

    /**
     * Returns true if the passed string is one of the filter we must process potentially followed
     * by a prefix (some density filters get V4, V16, etc... appended).
     */
    boolean isFilter(String potentialFilterWithSuffix) {
        for (String density : densityFilters) {
            if (potentialFilterWithSuffix.startsWith(density)) {
                return true
            }
        }
        if (abiFilters.contains(potentialFilterWithSuffix)) {
            return true
        }
        if (languageFilters.contains(
                PackageSplitRes.unMangleSplitName(potentialFilterWithSuffix))) {
            return true
        }
        return false
    }

    private void forEachUnalignedInput(Closure closure) {
        Pattern unalignedPattern = Pattern.compile(
                "${project.archivesBaseName}-${outputBaseName}_(.*)-unaligned.apk")

        for (File file : getInputFiles()) {
            Matcher unaligned = unalignedPattern.matcher(file.getName())
            if (unaligned.matches() && isFilter(unaligned.group(1))) {
                closure(unaligned.group(1), file);
            }
        }
    }

    private void forEachUnsignedInput(Closure closure) {
        Pattern unsignedPattern = Pattern.compile(
                "${project.archivesBaseName}-${outputBaseName}_(.*)-unsigned.apk")

        for (File file : getInputFiles()) {
            Matcher unsigned = unsignedPattern.matcher(file.getName())
            if (unsigned.matches() && isFilter(unsigned.group(1))) {
                closure(unsigned.group(1), file)
            }
        }
    }

    @TaskAction
    void splitZipAlign() {

        Closure zipAlignIt = { String split, File file ->
            File out = new File(getOutputDirectory(),
                    "${project.archivesBaseName}-${outputBaseName}_${split}.apk")
            project.exec {
                executable = getZipAlignExe()
                args '-f', '4'
                args file.absolutePath
                args out
            }
        }
        forEachUnalignedInput(zipAlignIt)
        forEachUnsignedInput(zipAlignIt)
        saveApkMetadataFile()
    }

    @Override
    List<FilterData> getSplitsData() {
        ImmutableList.Builder<FilterData> filterDataBuilder = ImmutableList.builder();
        addAllFilterData(filterDataBuilder, densityFilters, FilterType.DENSITY);
        addAllFilterData(filterDataBuilder, languageFilters, FilterType.LANGUAGE);
        addAllFilterData(filterDataBuilder, abiFilters, FilterType.ABI);
        return filterDataBuilder.build();
    }
}
