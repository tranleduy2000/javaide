/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.build.gradle.managed.NdkBuildType;
import com.android.build.gradle.managed.NdkOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Helper functions for configuring an NdkOptions.
 */
public class NdkOptionsHelper {

    /**
     * Initialize an NdkOptions.
     *
     * Should be unnecessary when Gradle supports managed List of String.
     */
    public static void init(NdkOptions ndk) {
        ndk.setCFlags(Lists.<String>newArrayList());
        ndk.setCppFlags(Lists.<String>newArrayList());
        ndk.setLdFlags(Lists.<String>newArrayList());
        ndk.setLdLibs(Lists.<String>newArrayList());
        ndk.setAbiFilters(Sets.<String>newHashSet());
    }

    /**
     * Merge one NdkOptions to another.
     * @param base NdkOptions to merge to.  base is mutated to contain the merged result.
     * @param other NdkOptions to merge.  Options in other has priority over base if both are set.
     */
    public static void merge(NdkOptions base, NdkOptions other) {
        if (other.getModuleName() != null) {
            base.setModuleName(other.getModuleName());
        }

        base.getAbiFilters().addAll(other.getAbiFilters());
        base.getCFlags().addAll(other.getCFlags());
        base.getCppFlags().addAll(other.getCppFlags());
        base.getLdFlags().addAll(other.getLdFlags());
        base.getLdLibs().addAll(other.getLdLibs());

        if (other.getStl() != null) {
            base.setStl(other.getStl());
        }
        if (other.getRenderscriptNdkMode() != null) {
            base.setRenderscriptNdkMode(other.getRenderscriptNdkMode());
        }
    }

    /**
     * Merge one NdkBuildType to another
     * @param base NdkBuildType to merge to.  base is mutated to contain the merged result.
     * @param other NdkBuildType to merge.  Options in other has priority over base if both are set.
     */
    public static void merge(NdkBuildType base, NdkBuildType other) {
        merge(base, (NdkOptions) other);
        if (other.getDebuggable() != null) {
            base.setDebuggable(other.getDebuggable());
        }
    }
}
