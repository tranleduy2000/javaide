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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.MavenCoordinates;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class JavaLibraryImpl extends LibraryImpl implements JavaLibrary, Serializable {
    private final File jarFile;

    public JavaLibraryImpl(
            @NonNull File jarFile,
            @Nullable MavenCoordinates requestedCoordinates,
            @Nullable MavenCoordinates resolvedCoordinates) {
        super(requestedCoordinates, resolvedCoordinates);
        this.jarFile = jarFile;
    }

    @NonNull
    @Override
    public File getJarFile() {
        return jarFile;
    }

    @NonNull
    @Override
    public List<? extends JavaLibrary> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JavaLibraryImpl{");
        sb.append("jarFile=").append(jarFile);
        sb.append('}');
        return sb.toString();
    }
}
