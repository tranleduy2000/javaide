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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;
import com.google.common.collect.Sets;

import org.gradle.api.tasks.Input;

import java.util.Set;

/**
 * DSL objects for configuring APK packaging options.
 */
public class PackagingOptions implements com.android.builder.model.PackagingOptions {

    private Set<String> excludes = Sets.newHashSet("LICENSE.txt", "LICENSE");
    private Set<String> pickFirsts = Sets.newHashSet();
    private Set<String> merges = Sets.newHashSet();

    /**
     * Returns the list of excluded paths.
     *
     * <p>Contains "LICENSE.txt" and "LICENSE" by default, since they often cause
     * packaging conflicts.
     */
    @Override
    @NonNull
    @Input
    public Set<String> getExcludes() {
        return Sets.newHashSet(excludes);
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = Sets.newHashSet(excludes);
    }

    /**
     * Adds an excluded paths.
     * @param path the path, as packaged in the APK
     */
    public void exclude(String path) {
        excludes.add(path);
    }

    /**
     * Returns the list of paths where the first occurrence is packaged in the APK.
     */
    @Override
    @NonNull
    @Input
    public Set<String> getPickFirsts() {
        return Sets.newHashSet(pickFirsts);
    }

    /**
     * Adds a firstPick path. First pick paths do get packaged in the APK, but only the first
     * occurrence gets packaged.
     * @param path the path to add.
     */
    public void pickFirst(String path) {
        pickFirsts.add(path);
    }

    public void setPickFirsts(Set<String> pickFirsts) {
        this.pickFirsts = Sets.newHashSet(pickFirsts);
    }

    /**
     * Returns the list of paths where all occurrences are concatenated and packaged in the APK.
     */
    @Override
    @NonNull
    @Input
    public Set<String> getMerges() {
        return Sets.newHashSet(merges);
    }

    public void setMerges(Set<String> merges) {
        this.merges = Sets.newHashSet(merges);
    }

    /**
     * Adds a merge path.
     * @param path the path, as packaged in the APK
     */
    public void merge(String path) {
        merges.add(path);
    }
}
