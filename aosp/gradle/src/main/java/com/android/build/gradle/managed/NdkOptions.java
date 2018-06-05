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

package com.android.build.gradle.managed;

import com.android.annotations.NonNull;

import org.gradle.model.Managed;
import org.gradle.model.Unmanaged;

import java.util.List;
import java.util.Set;

/**
 * DSL object for variant specific NDK-related settings.
 */
@Managed
public interface NdkOptions {

    /**
     * The module name.
     * The resulting shared object will be named "lib${getModuleName()}.so".
     */
    String getModuleName();
    void setModuleName(@NonNull String moduleName);

    /**
     * The ABI Filters.  Leave empty to include all supported ABI.
     */
    @Unmanaged
    Set<String> getAbiFilters();
    void setAbiFilters(@NonNull Set<String> filters);

    /**
     * The C Flags
     */
    @Unmanaged
    List<String> getCFlags();
    void setCFlags(@NonNull List<String> cFlags);

    /**
     * The C++ Flags
     */
    @Unmanaged
    List<String> getCppFlags();
    void setCppFlags(@NonNull List<String> cppFlags);

    /**
     * The linker flags
     */
    @Unmanaged
    List<String> getLdFlags();
    void setLdFlags(@NonNull List<String> ldFlags);

    /**
     * The LD Libs
     */
    @Unmanaged
    List<String> getLdLibs();
    void setLdLibs(@NonNull List<String> ldLibs);

    /**
     * The STL.
     *
     * Supported values are:
     *   - system (default)
     *   - gabi++_static
     *   - gabi++_shared
     *   - stlport_static
     *   - stlport_shared
     *   - gnustl_static
     *   - gnustl_shared
     */
    String getStl();
    void setStl(@NonNull String stl);

    Boolean getRenderscriptNdkMode();
    void setRenderscriptNdkMode(Boolean renderscriptNdkMode);
}
