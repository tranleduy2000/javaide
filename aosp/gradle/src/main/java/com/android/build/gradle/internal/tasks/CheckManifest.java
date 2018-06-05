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

package com.android.build.gradle.internal.tasks;

import com.android.annotations.NonNull;

import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Class that checks the presence of the manifest.
 */
public class CheckManifest extends DefaultAndroidTask {

    private File manifest;

    private String variantName;

    @InputFile
    public File getManifest() {
        return manifest;
    }

    public void setManifest(@NonNull File manifest) {
        this.manifest = manifest;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(@NonNull String variantName) {
        this.variantName = variantName;
    }

    @TaskAction
    void check() {
        // use getter to resolve convention mapping
        File f = getManifest();
        if (!f.isFile()) {
            throw new IllegalArgumentException(String.format(
                    "Main Manifest missing for variant %s. Expected path: ",
                    getVariantName(), getManifest().getAbsolutePath()));
        }
    }
}
