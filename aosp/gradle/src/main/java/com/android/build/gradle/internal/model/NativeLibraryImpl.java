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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.builder.model.NativeLibrary;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Implementation of NativeLibrary that is serializable.
 */
public class NativeLibraryImpl implements NativeLibrary, Serializable{
    private static final long serialVersionUID = 1L;

    @NonNull
    String name;
    @NonNull
    String toolchainName;
    @NonNull
    String abi;
    @NonNull
    List<File> cIncludeDirs;
    @NonNull
    List<File> cppIncludeDirs;
    @NonNull
    List<File> cSystemIncludeDirs;
    @NonNull
    List<File> cppSystemIncludeDirs;
    @NonNull
    List<String> cDefines;
    @NonNull
    List<String> cppDefines;
    @NonNull
    List<String> cCompilerFlags;
    @NonNull
    List<String> cppCompilerFlags;
    @NonNull
    List<File> debuggableLibraryFolders;

    public NativeLibraryImpl(
            @NonNull String name,
            @NonNull String toolchainName,
            @NonNull String abi,
            @NonNull List<File> cIncludeDirs,
            @NonNull List<File> cppIncludeDirs,
            @NonNull List<File> cSystemIncludeDirs,
            @NonNull List<File> cppSystemIncludeDirs,
            @NonNull List<String> cDefines,
            @NonNull List<String> cppDefines,
            @NonNull List<String> cCompilerFlags,
            @NonNull List<String> cppCompilerFlags,
            @NonNull List<File> debuggableLibraryFolders) {
        this.name = name;
        this.toolchainName = toolchainName;
        this.abi = abi;
        this.cIncludeDirs = cIncludeDirs;
        this.cppIncludeDirs = cppIncludeDirs;
        this.cSystemIncludeDirs = cSystemIncludeDirs;
        this.cppSystemIncludeDirs = cppSystemIncludeDirs;
        this.cDefines = cDefines;
        this.cppDefines = cppDefines;
        this.cCompilerFlags = cCompilerFlags;
        this.cppCompilerFlags = cppCompilerFlags;
        this.debuggableLibraryFolders = debuggableLibraryFolders;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getToolchainName() {
        return toolchainName;
    }

    @NonNull
    @Override
    public String getAbi() {
        return abi;
    }

    @NonNull
    @Override
    public List<File> getCIncludeDirs() {
        return cIncludeDirs;
    }

    @NonNull
    @Override
    public List<File> getCppIncludeDirs() {
        return cppIncludeDirs;
    }

    @NonNull
    @Override
    public List<File> getCSystemIncludeDirs() {
        return cSystemIncludeDirs;
    }

    @NonNull
    @Override
    public List<File> getCppSystemIncludeDirs() {
        return cppSystemIncludeDirs;
    }

    @NonNull
    @Override
    public List<String> getCDefines() {
        return cDefines;
    }

    @NonNull
    @Override
    public List<String> getCppDefines() {
        return cppDefines;
    }

    @NonNull
    @Override
    public List<String> getCCompilerFlags() {
        return cCompilerFlags;
    }

    @NonNull
    @Override
    public List<String> getCppCompilerFlags() {
        return cppCompilerFlags;
    }

    @NonNull
    @Override
    public List<File> getDebuggableLibraryFolders() {
        return debuggableLibraryFolders;
    }

    @Override
    public String toString() {
        return "NativeLibraryImpl{" +
                "name='" + name + '\'' +
                ", toolchainName='" + toolchainName + '\'' +
                ", cIncludeDirs=" + cIncludeDirs +
                ", cppIncludeDirs=" + cppIncludeDirs +
                ", cDefines=" + cDefines +
                ", cppDefines=" + cppDefines +
                ", cCompilerFlags=" + cCompilerFlags +
                ", cppCompilerFlags=" + cppCompilerFlags +
                ", solibSearchPaths=" + debuggableLibraryFolders +
                '}';
    }
}
