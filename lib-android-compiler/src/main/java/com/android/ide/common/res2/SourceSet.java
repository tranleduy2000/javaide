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

package com.android.ide.common.res2;

import com.android.annotations.NonNull;

import java.io.File;
import java.util.List;

/**
 * A Source sets that contains a list of source files/folders
 */
public interface SourceSet {

    /**
     * Returns a list of Source files or folders.
     * @return a non null list.
     */
    @NonNull
    List<File> getSourceFiles();

    /**
     * Finds and returns a Source file/folder containing a given file.
     *
     * It doesn't actually check if the file exists, instead just cares about the file path.
     *
     * @param file the file to search for
     * @return the source file containing the file or null if none are found.
     */
    File findMatchingSourceFile(File file);
}
