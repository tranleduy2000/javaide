/*
 * Copyright (C) 2012 The Android Open Source Project
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
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.sdk.TargetInfo;
import com.android.sdklib.BuildToolInfo;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.google.common.base.Preconditions;

import java.io.File;

public abstract class BaseTask extends DefaultAndroidTask {

    @Nullable
    private AndroidBuilder androidBuilder;

    @Nullable
    private ILogger iLogger;

    /**
     * Returns the androidBuilder.
     * @throws IllegalStateException if androidBuilder has not been set,
     */
    @NonNull
    protected AndroidBuilder getBuilder() {
        Preconditions.checkState(androidBuilder != null,
                "androidBuilder required for task '%s'.", getName());
        return androidBuilder;
    }

    @NonNull
    protected ILogger getILogger() {
        if (iLogger == null) {
            iLogger = new LoggerWrapper(getLogger());
        }
        return iLogger;
    }

    /**
     * Returns the BuildToolInfo.
     * @throws IllegalStateException if androidBuilder.targetInfo has not been set,
     */
    @NonNull
    protected BuildToolInfo getBuildTools() {
        TargetInfo targetInfo = getBuilder().getTargetInfo();
        Preconditions.checkState(targetInfo != null,
                "androidBuilder.targetInfo required for task '%s'.", getName());
        return targetInfo.getBuildTools();
    }

    public void setAndroidBuilder(@NonNull AndroidBuilder androidBuilder) {
        this.androidBuilder = androidBuilder;
    }
}
