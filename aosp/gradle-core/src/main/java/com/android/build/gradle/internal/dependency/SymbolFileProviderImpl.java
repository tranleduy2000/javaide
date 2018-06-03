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

package com.android.build.gradle.internal.dependency;

import com.android.annotations.NonNull;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.dependency.SymbolFileProvider;

import org.gradle.api.tasks.InputFile;

import java.io.File;

/**
 * Implementation of SymbolFileProvider that can be used as a Task input.
 */
public class SymbolFileProviderImpl implements SymbolFileProvider {

    private final File manifest;
    private final File symbolFile;
    private final boolean isOptional;

    public SymbolFileProviderImpl(@NonNull LibraryDependency library) {
        manifest = library.getManifest();
        symbolFile = library.getSymbolFile();
        isOptional = library.isOptional();
    }

    @InputFile
    @Override
    @NonNull
    public File getManifest() {
        return manifest;
    }

    @InputFile
    @Override
    @NonNull
    public File getSymbolFile() {
        return symbolFile;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }
}
