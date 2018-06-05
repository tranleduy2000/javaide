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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * Coordinates that uniquely identifies a project in a Maven repository.
 */
public interface MavenCoordinates {
    /**
     * Returns the name of the project's group, similar to the Java packaging structure.
     */
    @NonNull
    String getGroupId();

    /**
     * Returns the name that the project is known by.
     */
    @NonNull
    String getArtifactId();

    /**
     * Returns the version of the project.
     */
    @NonNull
    String getVersion();

    /**
     * Returns the project's artifact type. It defaults to "jar" if not explicitly set.
     */
    @NonNull
    String getPackaging();

    /**
     * Returns the project's classifier. The classifier allows to distinguish artifacts that were
     * built from the same POM but differ in their content.
     */
    @Nullable
    String getClassifier();
}
