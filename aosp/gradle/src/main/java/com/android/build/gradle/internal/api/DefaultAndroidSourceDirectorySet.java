/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.build.gradle.internal.api;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.AndroidSourceDirectorySet;
import com.google.common.collect.Lists;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import groovy.lang.Closure;

/**
 * Default implementation of the AndroidSourceDirectorySet.
 */
public class DefaultAndroidSourceDirectorySet implements AndroidSourceDirectorySet {

    private final String name;
    private final Project project;
    private List<Object> source = Lists.newArrayList();
    private final PatternSet filter = new PatternSet();

    public DefaultAndroidSourceDirectorySet(@NonNull String name,
            @NonNull Project project) {
        this.name = name;
        this.project = project;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet srcDir(Object srcDir) {
        source.add(srcDir);
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet srcDirs(Object... srcDirs) {
        Collections.addAll(source, srcDirs);
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet setSrcDirs(Iterable<?> srcDirs) {
        source.clear();
        for (Object srcDir : srcDirs) {
            source.add(srcDir);
        }
        return this;
    }

    @Override
    @NonNull
    public FileTree getSourceFiles() {
        FileTree src = null;
        Set<File> sources = getSrcDirs();
        if (!sources.isEmpty()) {
            src = project.files(new ArrayList<Object>(sources)).getAsFileTree().matching(filter);
        }
        return src == null ? project.files().getAsFileTree() : src;
    }

    @Override
    @NonNull
    public Set<File> getSrcDirs() {
        return project.files(source.toArray()).getFiles();
    }

    @Override
    @NonNull
    public PatternFilterable getFilter() {
        return filter;
    }


    @Override
    @NonNull
    public String toString() {
        return source.toString();
    }

    @Override
    public Set<String> getIncludes() {
        return filter.getIncludes();
    }

    @Override
    public Set<String> getExcludes() {
        return filter.getExcludes();
    }

    @Override
    public PatternFilterable setIncludes(Iterable<String> includes) {
        filter.setIncludes(includes);
        return this;
    }

    @Override
    public PatternFilterable setExcludes(Iterable<String> excludes) {
        filter.setExcludes(excludes);
        return this;
    }

    @Override
    public PatternFilterable include(String... includes) {
        filter.include(includes);
        return this;
    }

    @Override
    public PatternFilterable include(Iterable<String> includes) {
        filter.include(includes);
        return this;
    }

    @Override
    public PatternFilterable include(Spec<FileTreeElement> includeSpec) {
        filter.include(includeSpec);
        return this;
    }

    @Override
    public PatternFilterable include(Closure includeSpec) {
        filter.include(includeSpec);
        return this;
    }

    @Override
    public PatternFilterable exclude(Iterable<String> excludes) {
        filter.exclude(excludes);
        return this;
    }

    @Override
    public PatternFilterable exclude(String... excludes) {
        filter.exclude(excludes);
        return this;
    }

    @Override
    public PatternFilterable exclude(Spec<FileTreeElement> excludeSpec) {
        filter.exclude(excludeSpec);
        return this;
    }

    @Override
    public PatternFilterable exclude(Closure excludeSpec) {
        filter.exclude(excludeSpec);
        return this;
    }
}
