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

package com.android.build.gradle.internal.dsl;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * Options for the adb tool.
 */
public class AdbOptions implements com.android.builder.model.AdbOptions {

    int timeOutInMs;

    List<String> installOptions;

    @Override
    public int getTimeOutInMs() {
        return timeOutInMs;
    }

    public void setTimeOutInMs(int timeOutInMs) {
        this.timeOutInMs = timeOutInMs;
    }

    public void timeOutInMs(int timeOutInMs) {
        setTimeOutInMs(timeOutInMs);
    }

    @Override
    public Collection<String> getInstallOptions() {
        return installOptions;
    }

    public void setInstallOptions(String option) {
        installOptions = ImmutableList.of(option);
    }

    public void setInstallOptions(String... options) {
        installOptions = ImmutableList.copyOf(options);
    }

    public void installOptions(String option) {
        installOptions = ImmutableList.of(option);
    }

    public void installOptions(String... options) {
        installOptions = ImmutableList.copyOf(options);
    }
}
