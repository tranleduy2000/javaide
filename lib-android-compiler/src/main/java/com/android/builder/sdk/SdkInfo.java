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

package com.android.builder.sdk;

import com.android.annotations.NonNull;

import java.io.File;

/**
 * General information about the SDK.
 */
public class SdkInfo {

    @NonNull
    private final File mAnnotationJar;
    @NonNull
    private final File mAdb;

    SdkInfo(@NonNull File annotationJar,
            @NonNull File adb) {
        mAnnotationJar = annotationJar;
        mAdb = adb;
    }

    /**
     * Returns the location of the annotations jar for compilation targets that are <= 15.
     */
    @NonNull
    public File getAnnotationsJar() {
        return mAnnotationJar;
    }

    /**
     * Returns the revision of the installed platform tools component.
     *
     * @return the FullRevision or null if the revision couldn't not be found
     */
//    @Nullable
//    public FullRevision getPlatformToolsRevision() {
//
//    }

    /**
     * Returns the location of the adb tool.
     */
    @NonNull
    public File getAdb() {
        return mAdb;
    }
}
