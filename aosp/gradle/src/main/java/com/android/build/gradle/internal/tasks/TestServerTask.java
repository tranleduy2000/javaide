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

package com.android.build.gradle.internal.tasks;

import com.android.annotations.NonNull;
import com.android.builder.testing.api.TestServer;
import com.google.common.base.Preconditions;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Task sending APKs out to a {@link TestServer}
 */
public class TestServerTask extends DefaultAndroidTask {

    private File testApk;

    File testedApk;

    TestServer testServer;

    @TaskAction
    public void sendToServer() {
        testServer.uploadApks(getVariantName(), getTestApk(), getTestedApk());
    }

    @InputFile
    public File getTestApk() {
        return testApk;
    }

    public void setTestApk(File testApk) {
        this.testApk = testApk;
    }

    @InputFile @Optional
    public File getTestedApk() {
        return testedApk;
    }

    public void setTestedApk(File testedApk) {
        this.testedApk = testedApk;
    }

    @NonNull
    @Override
    @Input
    public String getVariantName() {
        return Preconditions.checkNotNull(super.getVariantName(),
                "Test server task must have a variant name.");
    }

    public TestServer getTestServer() {
        return testServer;
    }

    public void setTestServer(TestServer testServer) {
        this.testServer = testServer;
    }
}
