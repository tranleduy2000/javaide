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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.model.JavaLibraryImpl;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaLibrary;
import com.google.common.collect.Sets;

import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Implementation of {@link com.android.builder.model.Dependencies} over a Gradle
 * Configuration object. This is used to lazily query the list of files from the config object.
 */
public class ConfigurationDependencies implements Dependencies {

    @NonNull
    private final Configuration configuration;

    public ConfigurationDependencies(@NonNull Configuration configuration) {

        this.configuration = configuration;
    }

    @NonNull
    @Override
    public Collection<AndroidLibrary> getLibraries() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Collection<JavaLibrary> getJavaLibraries() {
        Set<File> files = configuration.getFiles();
        if (files.isEmpty()) {
            return Collections.emptySet();
        }
        Set<JavaLibrary> javaLibraries = Sets.newHashSet();
        for (File file : files) {
            javaLibraries.add(new JavaLibraryImpl(file, null, null));
        }
        return javaLibraries;
    }

    @NonNull
    @Override
    public Collection<String> getProjects() {
        return Collections.emptyList();
    }
}
