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
import com.android.builder.dependency.JarDependency;
import com.android.builder.model.MavenCoordinates;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Information about a jar dependency as well as its transitive dependencies.
 */
public class JarInfo {

    @NonNull
    private final File jarFile;
    /** if the dependency is a sub-project, then the gradle project path */
    @Nullable
    private final String gradlePath;
    @NonNull
    final List<JarInfo> dependencies = Lists.newArrayList();
    @NonNull
    private final MavenCoordinates resolvedCoordinates;
    private boolean compiled = false;
    private boolean packaged = false;

    public JarInfo(
            @NonNull File jarFile,
            @NonNull MavenCoordinates resolvedCoordinates,
            @Nullable String gradlePath,
            @NonNull List<JarInfo> dependencies) {
        Preconditions.checkNotNull(jarFile);
        Preconditions.checkNotNull(resolvedCoordinates);
        Preconditions.checkNotNull(dependencies);
        this.jarFile = jarFile;
        this.resolvedCoordinates = resolvedCoordinates;
        this.gradlePath = gradlePath;
        this.dependencies.addAll(dependencies);
    }

    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }

    public void setPackaged(boolean packaged) {
        this.packaged = packaged;
    }

    public boolean isPackaged() {
        return packaged;
    }

    @NonNull
    public File getJarFile() {
        return jarFile;
    }

    @NonNull
    public MavenCoordinates getResolvedCoordinates() {
        return resolvedCoordinates;
    }

    @Nullable
    public String getGradlePath() {
        return gradlePath;
    }

    @NonNull
    public List<JarInfo> getDependencies() {
        return dependencies;
    }

    @NonNull
    public JarDependency createJarDependency() {
        return new JarDependency(
                jarFile,
                compiled,
                packaged,
                true /*proguarded*/,
                resolvedCoordinates,
                gradlePath);
    }

    @Override
    public String toString() {
        return "JarInfo{" +
                "jarFile=" + jarFile +
                ", gradlePath='" + gradlePath + '\'' +
                ", compiled=" + compiled +
                ", packaged=" + packaged +
                ", dependencies=" + dependencies +
                ", resolvedCoordinates=" + resolvedCoordinates +
                '}';
    }
}