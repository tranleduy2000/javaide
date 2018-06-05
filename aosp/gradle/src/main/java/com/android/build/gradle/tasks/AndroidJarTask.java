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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;

import org.gradle.api.tasks.bundling.Jar;

/**
 * Decorated {@link Jar} task with android specific behaviors.
 */
public class AndroidJarTask extends Jar implements BinaryFileProviderTask {

    @Override
    @NonNull
    public Artifact getArtifact() {
        return new Artifact(BinaryArtifactType.JAR, getArchivePath());
    }
}
