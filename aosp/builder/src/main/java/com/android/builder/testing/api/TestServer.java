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

package com.android.builder.testing.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.annotations.Beta;

import java.io.File;

/**
 * Base interface for Remote CI Servers.
 */
@Beta
public abstract class TestServer {

    /**
     * Returns the name of the server. Must be unique, not contain spaces, and start with a lower
     * case.
     *
     * @return the name of the provider.
     */
    @NonNull
    public abstract String getName();

    /**
     * Uploads the APKs to the server.
     *
     * @param variantName the name of the variant being tested.
     * @param testApk the APK containing the tests.
     * @param testedApk the APK to be tested. This is optional in case the test apk is self-tested.
     */
    public abstract void uploadApks(@NonNull String variantName,
                                    @NonNull File testApk, @Nullable File testedApk);

    /**
     * Returns true if the server is configured and able to run.
     *
     * @return if the server is configured.
     */
    public abstract boolean isConfigured();
}
