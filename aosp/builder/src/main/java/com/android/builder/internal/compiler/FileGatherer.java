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

package com.android.builder.internal.compiler;

import com.android.annotations.NonNull;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Source Searcher processor, gathering a list of all the files found by the SourceSearcher.
 */
public class FileGatherer implements SourceSearcher.SourceFileProcessor {
    @NonNull
    private final List<File> mFiles = Lists.newArrayList();

    @Override
    public void processFile(@NonNull File sourceFolder, @NonNull File sourceFile) {
        mFiles.add(sourceFile);
    }

    @NonNull
    public List<File> getFiles() {
        return mFiles;
    }
}
