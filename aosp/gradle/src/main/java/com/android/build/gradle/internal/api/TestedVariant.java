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

package com.android.build.gradle.internal.api;

import com.android.annotations.Nullable;
import com.android.build.gradle.TestedAndroidConfig;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.api.UnitTestVariant;

/**
 * API for tested variant api object.
 */
public interface TestedVariant {

    void setTestVariant(@Nullable TestVariant testVariant);

    /**
     * Returns the build variant that will test this build variant.
     *
     * <p>The android test variant exists only for one build type, by default "debug". This is
     * controlled by {@link TestedAndroidConfig#getTestBuildType}.
     */
    @Nullable
    TestVariant getTestVariant();


    /**
     * Returns the build variant that contains the unit tests for this variant.
     */
    @Nullable
    UnitTestVariant getUnitTestVariant();

    void setUnitTestVariant(@Nullable UnitTestVariant testVariant);
}

