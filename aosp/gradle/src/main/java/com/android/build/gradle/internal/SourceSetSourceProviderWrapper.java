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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.builder.model.SourceProvider;

import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * An implementation of SourceProvider that's wrapper around a Java SourceSet.
 * This is useful for the case where we store SourceProviders but don't want to
 * query the content of the SourceSet at the moment the SourceProvider is created.
 */
public class SourceSetSourceProviderWrapper implements SourceProvider {

    @NonNull
    private final SourceSet sourceSet;

    public SourceSetSourceProviderWrapper(@NonNull SourceSet sourceSet) {

        this.sourceSet = sourceSet;
    }

    @NonNull
    @Override
    public String getName() {
        return sourceSet.getName();
    }

    @NonNull
    @Override
    public File getManifestFile() {
        throw new IllegalAccessError("Shouldn't access manifest from SourceSetSourceProviderWrapper");
    }

    @NonNull
    @Override
    public Collection<File> getJavaDirectories() {
        return sourceSet.getJava().getSrcDirs();
    }

    @NonNull
    @Override
    public Collection<File> getResourcesDirectories() {
        return sourceSet.getResources().getSrcDirs();
    }

    @NonNull
    @Override
    public Collection<File> getAidlDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getRenderscriptDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getCDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getCppDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getResDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getAssetsDirectories() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<File> getJniLibsDirectories() {
        return Collections.emptyList();
    }
}
