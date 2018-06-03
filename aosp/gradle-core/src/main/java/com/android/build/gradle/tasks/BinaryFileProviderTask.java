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

import org.gradle.api.Task;

import java.io.File;

/**
 * Denotes a {@link Task} that is capable of providing a zip or jar package of compiled java
 * sources.
 */
public interface BinaryFileProviderTask extends Task {

    enum BinaryArtifactType { JAR, JACK }

    final class Artifact {

        @NonNull
        private final BinaryArtifactType binaryArtifactType;

        @NonNull
        private final File artifactFile;

        public Artifact(@NonNull BinaryArtifactType binaryArtifactType, @NonNull File artifactFile) {
            this.binaryArtifactType = binaryArtifactType;
            this.artifactFile = artifactFile;
        }
    }

    @NonNull
    Artifact getArtifact();
}
