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

import static com.android.SdkConstants.EXT_FS;
import static com.android.SdkConstants.EXT_RS;
import static com.android.SdkConstants.EXT_RSH;

import com.android.annotations.NonNull;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Checks whether Renderscript compilation is needed. This is entirely based
 * on using dependency files and manually looking up the list of current inputs, and old
 * outputs timestamp.
 *
 * TODO: add checks on input/output checksum to detect true changes.
 * TODO: (better) delete Ant and use Gradle.
 *
 * This should be only needed in Ant.
 */
public class ManualRenderScriptChecker extends RenderScriptChecker {

    @NonNull
    private final List<File> mInputFiles = Lists.newArrayList();

    public ManualRenderScriptChecker(
            @NonNull List<File> sourceFolders,
            @NonNull File binFolder) {
        super(sourceFolders, binFolder);
    }

    public boolean mustCompile() throws IOException {
        mInputFiles.clear();

        loadDependencies();

        if (mDependencyFiles.isEmpty()) {
            mInputFiles.addAll(findInputFiles());
            return !mInputFiles.isEmpty();
        }

        // get the current files to compile, while checking then against the old inputs
        // to detect new inputs
        SourceSearcher searcher = new SourceSearcher(mSourceFolders, EXT_RS, EXT_FS, EXT_RSH);
        InputProcessor inputProcessor = new InputProcessor(mOldInputs);
        searcher.search(inputProcessor);

        // at this point we have gathered the input files, so we can record them in case we have to
        // compile later.
        mInputFiles.addAll(inputProcessor.sourceFiles);

        if (inputProcessor.mustCompile) {
            return true;
        }

        // no new files? check if we have less input files.
        if (mOldInputs.size() !=
                inputProcessor.sourceFiles.size() + inputProcessor.headerFiles.size()) {
            return true;
        }

        // since there's no change in the input, look for change in the output.
        for (File file : mOldOutputs) {
            if (!file.isFile()) {
                // deleted output file?
                return true;
            }
        }

        // finally look at file changes.
        for (DependencyFile depFile : mDependencyFiles) {
            if (depFile.needCompilation()) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    public List<File> getInputFiles() {
        return mInputFiles;
    }

    private static class InputProcessor implements SourceSearcher.SourceFileProcessor {

        @NonNull
        private final Set<File> mOldInputs;

        List<File> sourceFiles = Lists.newArrayList();
        List<File> headerFiles = Lists.newArrayList();
        boolean mustCompile = false;

        InputProcessor(@NonNull Set<File> oldInputs) {
            mOldInputs = oldInputs;
        }

        @Override
        public void processFile(@NonNull File sourceFile, @NonNull String extension)
                throws IOException {
            if (EXT_RSH.equals(extension)) {
                headerFiles.add(sourceFile);
            } else {
                sourceFiles.add(sourceFile);
            }

            // detect new inputs.
            if (!mOldInputs.contains(sourceFile)) {
                mustCompile = true;
            }
        }
    }
}
