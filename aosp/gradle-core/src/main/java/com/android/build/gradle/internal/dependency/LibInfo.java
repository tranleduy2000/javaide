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

package com.android.build.gradle.internal.dependency;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.model.MavenCoordinates;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Version of LibraryDependency that includes transitive jar dependencies as JarInfo.
 *
 * This is used as a temporary object as the dependencies are processed. This object contains
 * more information and can be mutated ({@link #setIsOptional(boolean)}) during the process.
 *
 * In the end the object will be converted into an immutable LibraryDependency instance.
 */
public class LibInfo extends LibraryDependencyImpl {

    @NonNull
    private final Collection<JarInfo> jarDependencies;

    private boolean mutableIsOptional = false;

    public LibInfo(@NonNull File bundle,
            @NonNull File explodedBundle,
            @NonNull List<LibraryDependency> dependencies,
            @NonNull Collection<JarInfo> jarDependencies,
            @Nullable String name,
            @Nullable String variantName,
            @Nullable String projectPath,
            @Nullable MavenCoordinates requestedCoordinates,
            @Nullable MavenCoordinates resolvedCoordinates) {
        super(bundle,
                explodedBundle,
                dependencies,
                name,
                variantName,
                projectPath,
                requestedCoordinates,
                resolvedCoordinates,
                false);
        this.jarDependencies = jarDependencies;
    }

    /**
     * Mark the dependency as optional.
     */
    public void setIsOptional(boolean isOptional) {
        this.mutableIsOptional = isOptional;
        for (LibInfo libInfo : getLibInfoDependencies()) {
            libInfo.setIsOptional(isOptional);
        }
    }

    @Override
    public boolean isOptional() {
        return mutableIsOptional;
    }

    @NonNull
    public List<LibInfo> getLibInfoDependencies() {
        ImmutableList.Builder<LibInfo> libInfoBuilder = ImmutableList.builder();
        for (LibraryDependency libraryDependency : getDependencies()) {
            if (libraryDependency instanceof LibInfo) {
                libInfoBuilder.add((LibInfo) libraryDependency);
            } else {
                throw new RuntimeException("Mixed LibInfo and LibraryDependencies instances !");
            }
        }
        return libInfoBuilder.build();
    }

    @NonNull
    public Collection<JarInfo> getJarDependencies() {
        return jarDependencies;
    }
}
