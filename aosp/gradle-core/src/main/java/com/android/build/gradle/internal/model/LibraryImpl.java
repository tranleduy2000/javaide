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

package com.android.build.gradle.internal.model;

import com.android.annotations.Nullable;
import com.android.builder.model.Library;
import com.android.builder.model.MavenCoordinates;

import java.io.Serializable;

/**
 * Implementation of Library interface for the model.
 */
class LibraryImpl implements Library, Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final MavenCoordinates requestedCoordinates;

    @Nullable
    private final MavenCoordinates resolvedCoordinates;

    LibraryImpl(
            @Nullable MavenCoordinates requestedCoordinates,
            @Nullable MavenCoordinates resolvedCoordinates) {

        this.requestedCoordinates = requestedCoordinates;
        this.resolvedCoordinates = resolvedCoordinates;
    }

    @Nullable
    @Override
    public MavenCoordinates getRequestedCoordinates() {
        return requestedCoordinates;
    }

    @Nullable
    @Override
    public MavenCoordinates getResolvedCoordinates() {
        return resolvedCoordinates;
    }
}
