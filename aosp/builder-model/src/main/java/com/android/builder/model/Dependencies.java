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

package com.android.builder.model;

import com.android.annotations.NonNull;

import java.util.Collection;

/**
 * A set of dependencies for an {@link AndroidArtifact}.
 */
public interface Dependencies {

    /**
     * The list of Android library dependencies. This includes both modules and external
     * dependencies.
     *
     * @return the list of libraries.
     */
    @NonNull
    Collection<AndroidLibrary> getLibraries();

    /**
     * The list of Java library dependencies. This only includes external dependencies.
     *
     * @return the list of Java library dependencies.
     */
    @NonNull
    Collection<JavaLibrary> getJavaLibraries();

    /**
     * The list of project dependencies. This is only for non Android module dependencies (which
     * right now is Java-only modules).
     *
     * @return the list of projects.
     */
    @NonNull
    Collection<String> getProjects();
}
