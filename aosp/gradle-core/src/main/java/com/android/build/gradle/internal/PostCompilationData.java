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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Callables;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class to hold data to setup the many optional post-compilation steps.
 */
public class PostCompilationData {

    @Nullable
    private List<?> classGeneratingTasks;

    @Nullable
    private List<?> libraryGeneratingTasks;

    @Nullable
    private Callable<List<File>> inputFiles;

    @Nullable
    private Callable<File> inputDir;

    @Nullable
    private Callable<File> javaResourcesInputDir;

    @Nullable
    private Callable<List<File>> inputLibraries;

    @NonNull
    public List<?> getClassGeneratingTasks() {
        Preconditions.checkState(classGeneratingTasks != null);
        return classGeneratingTasks;
    }

    public void setClassGeneratingTasks(@NonNull List<?> classGeneratingTasks) {
        this.classGeneratingTasks = classGeneratingTasks;
    }

    @NonNull
    public List<?> getLibraryGeneratingTasks() {
        Preconditions.checkState(libraryGeneratingTasks != null);
        return libraryGeneratingTasks;
    }

    public void setLibraryGeneratingTasks(@NonNull List<?> libraryGeneratingTasks) {
        this.libraryGeneratingTasks = libraryGeneratingTasks;
    }

    @Nullable
    public Callable<List<File>> getInputFilesCallable() {
        return inputFiles;
    }

    public void setInputFiles(@Nullable List<File> inputFiles) {
        this.inputFiles = Callables.returning(inputFiles);
    }

    public void setInputFilesCallable(@Nullable Callable<List<File>> inputFiles) {
        this.inputFiles = inputFiles;
    }

    @Nullable
    public Callable<File> getInputDirCallable() {
        return inputDir;
    }

    public void setInputDir(@NonNull File inputDir) {
        this.inputDir = Callables.returning(inputDir);
    }

    public void setInputDirCallable(@Nullable Callable<File> inputDir) {
        this.inputDir = inputDir;
    }

    @Nullable
    public Callable<File> getJavaResourcesInputDirCallable() {
        return javaResourcesInputDir;
    }

    public void setJavaResourcesInputDir(@NonNull File javaResourcesInputDir) {
        this.javaResourcesInputDir = Callables.returning(javaResourcesInputDir);
    }

    public void setJavaResourcesInputDirCallable(@Nullable Callable<File> javaResourcesInputDir) {
        this.javaResourcesInputDir = javaResourcesInputDir;
    }

    @Nullable
    public Callable<List<File>> getInputLibrariesCallable() {
        return inputLibraries;
    }

    public void setInputLibraries(@NonNull List<File> inputLibraries) {
        this.inputLibraries = Callables.returning(inputLibraries);
    }

    public void setInputLibrariesCallable(@Nullable Callable<List<File>> inputLibraries) {
        this.inputLibraries = inputLibraries;
    }
}
