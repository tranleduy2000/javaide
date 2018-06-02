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
import com.android.annotations.Nullable;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.ide.common.process.ProcessException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class to search for source files (by extension) in a set of source folders.
 */
public class SourceSearcher {

    @NonNull
    private final List<File> mSourceFolders;
    private final String[] mExtensions;
    @Nullable
    private WaitableExecutor<Void> mExecutor;

    public interface SourceFileProcessor {
        void processFile(@NonNull File sourceFolder, @NonNull File sourceFile)
                throws ProcessException, IOException;
    }

    public SourceSearcher(@NonNull List<File> sourceFolders, String... extensions) {
        mSourceFolders = sourceFolders;
        mExtensions = extensions;
    }

    public void setUseExecutor(boolean useExecutor) {
        if (useExecutor) {
            mExecutor = new WaitableExecutor<Void>();
        } else {
            mExecutor = null;
        }
    }

    public void search(@NonNull SourceFileProcessor processor)
            throws ProcessException, LoggedErrorException, InterruptedException, IOException {
        for (File file : mSourceFolders) {
            // pass both the root folder (the source folder) and the file/folder to process,
            // in this case the source folder as well.
            processFile(file, file, processor);
        }

        if (mExecutor != null) {
            mExecutor.waitForTasksWithQuickFail(true /*cancelRemaining*/);
        }
    }

    private void processFile(
            @NonNull final File rootFolder,
            @NonNull final File file,
            @NonNull final SourceFileProcessor processor)
            throws ProcessException, IOException {
        if (file.isFile()) {
            // get the extension of the file.
            if (checkExtension(file)) {
                if (mExecutor != null) {
                    mExecutor.execute(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            processor.processFile(rootFolder, file);
                            return null;
                        }
                    });
                } else {
                    processor.processFile(rootFolder, file);
                }
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    processFile(rootFolder, child, processor);
                }
            }
        }
    }

    private boolean checkExtension(File file) {
        if (mExtensions.length == 0) {
            return true;
        }

        String filename = file.getName();
        int pos = filename.indexOf('.');
        if (pos != -1) {
            String extension = filename.substring(pos + 1);
            for (String ext : mExtensions) {
                if (ext.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }

        return false;
    }
}
