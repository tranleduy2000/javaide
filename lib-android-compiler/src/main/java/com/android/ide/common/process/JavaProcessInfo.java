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

package com.android.ide.common.process;

import com.android.annotations.NonNull;

import java.util.List;

/**
 * Information to run an external java process.
 */
public interface JavaProcessInfo extends ProcessInfo {

    @NonNull
    String getClasspath();

    /**
     * The main Java Class. This is optional.
     */
    @NonNull
    String getMainClass();

    /**
     * The JVM args to use.
     */
    @NonNull
    List<String> getJvmArgs();
}
