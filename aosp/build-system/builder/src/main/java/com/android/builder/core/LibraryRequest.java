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

package com.android.builder.core;

import com.android.annotations.NonNull;

/**
 * a request for an optional library.
 */
public class LibraryRequest {

    @NonNull
    private final String mName;
    private final boolean mRequired;

    public LibraryRequest(@NonNull String name, boolean required) {
        mName = name;
        mRequired = required;
    }

    /**
     * The name of the library. This is the unique name that will show up in the manifest.
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * Whether the library is required by the app or just optional.
     */
    public boolean isRequired() {
        return mRequired;
    }
}
