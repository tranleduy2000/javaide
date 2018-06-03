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

package com.android.build.gradle.tasks

import com.android.build.gradle.internal.tasks.DefaultAndroidTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Runs some sanity checks that our tool chain configuration is correct.
 */
class PreCompilationVerificationTask extends DefaultAndroidTask {

    @Input
    boolean useJack

    @Input
    Object[] testSourceFiles

    @TaskAction
    void verify() {

        // we are asked to set up a compilation task against unit tests, we should make sure
        // the tested variant was not built using Jack as the .class files are not available
        // in that case.
        if (useJack && !getProject().files(testSourceFiles).files.isEmpty()) {
            throw new RuntimeException("Unit tests are not yet supported when Jack is used to " +
                    "compile the variant")
        }
    }
}
