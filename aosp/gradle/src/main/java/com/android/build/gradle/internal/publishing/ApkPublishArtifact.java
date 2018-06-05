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

package com.android.build.gradle.internal.publishing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.tasks.FileSupplier;

/**
 * custom implementation of PublishArtifact for published APKs.
 */
public class ApkPublishArtifact extends BasePublishArtifact {

    public ApkPublishArtifact(
            @NonNull String name,
            @Nullable String classifier,
            @NonNull FileSupplier outputFileSupplier) {
        super(name, classifier, outputFileSupplier);
    }

    @Override
    public String getExtension() {
        return "apk";
    }

    @Override
    public String getType() {
        return "apk";
    }
}
