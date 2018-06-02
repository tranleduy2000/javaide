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

package com.android.ide.common.res2;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Provides functionality the resource merger needs for preprocessing resources during merge.
 */
public interface ResourcePreprocessor {
    /** Checks if the given file should be replaced by N generated files. */
    boolean needsPreprocessing(File file);

    /** Returns the paths that should be generated for the given file. */
    Collection<File> getFilesToBeGenerated(File original);

    /** Actually generate the file based on the original file. */
    void generateFile(File toBeGenerated, File original) throws IOException;
}
