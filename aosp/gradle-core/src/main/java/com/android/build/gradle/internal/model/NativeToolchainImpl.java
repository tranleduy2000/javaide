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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.builder.model.NativeToolchain;

import java.io.File;
import java.io.Serializable;

/**
 * Implementation of NativeToolchain that is serializable.
 */
public class NativeToolchainImpl implements NativeToolchain, Serializable {

    @NonNull
    String name;

    @NonNull
    File cCompilerExecutable;

    @NonNull
    File cppCompilerExecutable;

    public NativeToolchainImpl(@NonNull String name, @NonNull File cCompilerExecutable,
            @NonNull File cppCompilerExecutable) {
        this.name = name;
        this.cCompilerExecutable = cCompilerExecutable;
        this.cppCompilerExecutable = cppCompilerExecutable;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public File getCCompilerExecutable() {
        return cCompilerExecutable;
    }

    @NonNull
    @Override
    public File getCppCompilerExecutable() {
        return cppCompilerExecutable;
    }

    @Override
    public String toString() {
        return "ToolchainImpl{" +
                "name='" + name + '\'' +
                ", cCompilerExecutable=" + cCompilerExecutable +
                ", cppCompilerExecutable=" + cppCompilerExecutable +
                '}';
    }
}
