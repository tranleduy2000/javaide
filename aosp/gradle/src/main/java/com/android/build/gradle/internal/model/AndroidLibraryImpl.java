/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.annotations.Nullable;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.MavenCoordinates;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class AndroidLibraryImpl extends LibraryImpl implements AndroidLibrary, Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final String project;
    @Nullable
    private final String variant;
    @NonNull
    private final File bundle;
    @NonNull
    private final File folder;
    @NonNull
    private final File manifest;
    @NonNull
    private final File jarFile;
    @NonNull
    private final Collection<File> localJars;
    @NonNull
    private final File resFolder;
    @NonNull
    private final File assetsFolder;
    @NonNull
    private final File jniFolder;
    @NonNull
    private final File aidlFolder;
    @NonNull
    private final File renderscriptFolder;
    @NonNull
    private final File proguardRules;
    @NonNull
    private final File lintJar;
    @NonNull
    private final File annotations;
    @NonNull
    private final File publicResources;
    @NonNull
    private final List<AndroidLibrary> dependencies;
    private final boolean isOptional;

    AndroidLibraryImpl(
            @NonNull LibraryDependency libraryDependency,
            @NonNull List<AndroidLibrary> dependencies,
            @NonNull Collection<File> localJarOverride,
            @Nullable String project,
            @Nullable String variant,
            @Nullable MavenCoordinates requestedCoordinates,
            @Nullable MavenCoordinates resolvedCoordinates) {
        super(requestedCoordinates, resolvedCoordinates);
        this.dependencies = dependencies;
        bundle = libraryDependency.getBundle();
        folder = libraryDependency.getFolder();
        manifest = libraryDependency.getManifest();
        jarFile = libraryDependency.getJarFile();
        localJars = Lists.newArrayList(localJarOverride);
        resFolder = libraryDependency.getResFolder();
        assetsFolder = libraryDependency.getAssetsFolder();
        jniFolder = libraryDependency.getJniFolder();
        aidlFolder = libraryDependency.getAidlFolder();
        renderscriptFolder = libraryDependency.getRenderscriptFolder();
        proguardRules = libraryDependency.getProguardRules();
        lintJar = libraryDependency.getLintJar();
        annotations = libraryDependency.getExternalAnnotations();
        publicResources = libraryDependency.getPublicResources();
        isOptional = libraryDependency.isOptional();

        this.project = project;
        this.variant = variant;
    }

    @Nullable
    @Override
    public String getProject() {
        return project;
    }

    @Nullable
    @Override
    public String getProjectVariant() {
        return variant;
    }

    @NonNull
    @Override
    public File getBundle() {
        return bundle;
    }

    @NonNull
    @Override
    public File getFolder() {
        return folder;
    }

    @NonNull
    @Override
    public List<? extends AndroidLibrary> getLibraryDependencies() {
        return dependencies;
    }

    @NonNull
    @Override
    public File getManifest() {
        return manifest;
    }

    @NonNull
    @Override
    public File getJarFile() {
        return jarFile;
    }

    @NonNull
    @Override
    public Collection<File> getLocalJars() {
        return localJars;
    }

    @NonNull
    @Override
    public File getResFolder() {
        return resFolder;
    }

    @NonNull
    @Override
    public File getAssetsFolder() {
        return assetsFolder;
    }

    @NonNull
    @Override
    public File getJniFolder() {
        return jniFolder;
    }

    @NonNull
    @Override
    public File getAidlFolder() {
        return aidlFolder;
    }

    @NonNull
    @Override
    public File getRenderscriptFolder() {
        return renderscriptFolder;
    }

    @NonNull
    @Override
    public File getProguardRules() {
        return proguardRules;
    }

    @NonNull
    @Override
    public File getLintJar() {
        return lintJar;
    }

    @NonNull
    @Override
    public File getExternalAnnotations() {
        return annotations;
    }

    @Override
    @NonNull
    public File getPublicResources() {
        return publicResources;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }
}
