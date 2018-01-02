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

package com.android.sdklib.build;

import com.android.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class to search for source files (by extension) in a set of source folders.
 */
public class SourceSearcher {

    @NonNull
    private final List<File> mSourceFolders;

    @NonNull
    private final String[] mExtensions;

    public interface SourceFileProcessor {
        void processFile(@NonNull File sourceFile, @NonNull String extension) throws IOException;
    }

    public SourceSearcher(@NonNull List<File> sourceFolders, @NonNull String... extensions) {
        mSourceFolders = sourceFolders;
        mExtensions = extensions;
    }

    public void search(@NonNull SourceFileProcessor processor)
            throws IOException {
        for (File file : mSourceFolders) {
            processFile(file, processor);
        }
    }

    private void processFile(@NonNull final File file, @NonNull final SourceFileProcessor processor)
            throws IOException {
        if (file.isFile()) {
            // get the extension of the file.
            String ext = checkExtension(file);
            if (ext != null) {
                processor.processFile(file, ext);
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    processFile(child, processor);
                }
            }
        }
    }

    /**
     * Return null if the extension don't match or the extension if there is a match.
     *
     * if there's no extension to look for, then returns an empty string.
     *
     * @param file the file to check
     * @return a string if match, null otherwise
     */
    private String checkExtension(@NonNull File file) {
        if (mExtensions.length == 0) {
            return "";
        }

        String filename = file.getName();
        int pos = filename.indexOf('.');
        if (pos != -1) {
            String extension = filename.substring(pos + 1);
            for (String ext : mExtensions) {
                if (ext.equalsIgnoreCase(extension)) {
                    return ext;
                }
            }
        }

        return null;
    }
}
