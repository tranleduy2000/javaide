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

package com.android.build.gradle.internal.api;

import com.android.build.gradle.api.AndroidSourceFile;

import org.gradle.api.Project;

import java.io.File;

/**
 */
public class DefaultAndroidSourceFile implements AndroidSourceFile {

    private final String name;
    private final Project project;
    private Object source;

    DefaultAndroidSourceFile(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AndroidSourceFile srcFile(Object o) {
        source = o;
        return this;
    }

    @Override
    public File getSrcFile() {
        return project.file(source);
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
