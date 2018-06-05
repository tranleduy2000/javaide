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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;
import com.android.resources.Density;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * DSL object for configuring per-density splits options.
 *
 * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/user-guide/apk-splits">APK Splits</a>.
 */
public class DensitySplitOptions extends SplitOptions {

    private boolean strict = true;
    private boolean auto = false;
    private Set<String> compatibleScreens;

    @Override
    protected Set<String> getDefaultValues() {
        Density[] values = Density.values();
        Set<String> fullList = Sets.newHashSetWithExpectedSize(values.length - 2);
        for (Density value : values) {
            if (value != Density.NODPI && value != Density.ANYDPI && value.isRecommended()) {
                fullList.add(value.getResourceValue());
            }
        }

        return fullList;
    }

    @Override
    protected ImmutableSet<String> getAllowedValues() {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Density value : Density.values()) {
            if (value != Density.NODPI && value != Density.ANYDPI) {
                builder.add(value.getResourceValue());
            }
        }

        return builder.build();
    }

    /**
     * TODO: Document.
     */
    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public void setCompatibleScreens(@NonNull List<String> sizes) {
        compatibleScreens = Sets.newHashSet(sizes);
    }

    /**
     * Adds a new compatible screen.
     *
     * <p>See {@link #getCompatibleScreens()}.
     */
    public void compatibleScreens(@NonNull String... sizes) {
        if (compatibleScreens == null) {
            compatibleScreens = Sets.newHashSet(sizes);
            return;
        }

        compatibleScreens.addAll(Arrays.asList(sizes));
    }

    /**
     * A list of compatible screens.
     *
     * <p>This will inject a matching <code>&lt;compatible-screens&gt;&lt;screen ...&gt;</code>
     * node in the manifest. This is optional.
     */
    @NonNull
    public Set<String> getCompatibleScreens() {
        if (compatibleScreens == null) {
            return Collections.emptySet();
        }
        return compatibleScreens;
    }

    /**
     * Sets whether the build system should determine the splits based on the "language-*" folders
     * in the resources. If the auto mode is set to true, the include list will be ignored.
     * @param auto true to automatically set the splits list based on the folders presence, false
     *             to use the include list.
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * Returns whether to use the automatic discovery mechanism for supported languages (true) or
     * the manual include list (false).
     * @return true for automatic, false for manual mode.
     */
    public boolean isAuto() {
        return auto;
    }
}
