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

package com.android.builder.dependency;

import com.android.annotations.NonNull;

import java.io.File;

/**
 * Provides a path to the Text Symbol file and to the Android Manifest
 */
public interface SymbolFileProvider extends ManifestProvider {

    /**
     * Returns the location of the text symbol file
     */
    @NonNull
    File getSymbolFile();

    /**
     * Returns whether the library is considered optional, meaning that it may or may not
     * be present in the final APK.
     *
     * If the library is optional, then:
     * - if the consumer is a library, it'll get skipped from resource merging and won't show up
     *   in the consumer R.txt
     * - if the consumer is a separate test project, all the resources gets skipped from merging.
     */
    boolean isOptional();
}
