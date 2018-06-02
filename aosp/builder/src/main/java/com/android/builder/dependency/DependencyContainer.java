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

package com.android.builder.dependency;

import com.android.annotations.NonNull;

import java.util.List;

/**
 * An object able to provide the three types of dependencies an Android project can have:
 * - local jar dependencies
 * - artifact jar dependencies
 * - android library dependencies
 */
public interface DependencyContainer {

    /**
     * Returns a list top level dependency. Each library object should contain
     * its own dependencies. This is actually a dependency graph.
     *
     * @return a non null (but possibly empty) list.
     */
    @NonNull
    List<? extends LibraryDependency> getAndroidDependencies();

    @NonNull
    List<JarDependency> getJarDependencies();

    @NonNull
    List<JarDependency> getLocalDependencies();
}
