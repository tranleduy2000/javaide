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

import static com.android.SdkConstants.DOT_DEP;
import static com.android.SdkConstants.EXT_FS;
import static com.android.SdkConstants.EXT_RS;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Loads dependencies for Renderscript.
 */
public class RenderScriptChecker {

    @NonNull
    protected final List<File> mSourceFolders;
    @NonNull
    private final File mBinFolder;

    protected Set<File> mOldOutputs;
    protected Set<File> mOldInputs;
    protected List<DependencyFile> mDependencyFiles;

    public RenderScriptChecker(
            @NonNull List<File> sourceFolders,
            @NonNull File binFolder) {
        mSourceFolders = sourceFolders;
        mBinFolder = binFolder;
    }

    public void loadDependencies() throws IOException {
        // get the dependency data from all files under bin/rsDeps/
        File renderscriptDeps = new File(mBinFolder, RenderScriptProcessor.RS_DEPS);

        File[] depsFiles = null;

        if (renderscriptDeps.isDirectory()) {
            depsFiles = renderscriptDeps.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(DOT_DEP);
                }
            });
        }

        int count = depsFiles != null ? depsFiles.length : 0;
        mDependencyFiles = Lists.newArrayListWithCapacity(0);
        mOldOutputs = Sets.newHashSet();
        mOldInputs = Sets.newHashSet();
        if (count > 0) {
            for (File file : depsFiles) {
                DependencyFile depFile = new DependencyFile(file, mSourceFolders);
                depFile.parse();
                mDependencyFiles.add(depFile);
                // record old inputs
                mOldOutputs.addAll(depFile.getOutputFiles());
                // record old inputs
                mOldInputs.addAll(depFile.getInputFiles());
            }
        }
    }

    @NonNull
    public List<File> findInputFiles() throws IOException {
        // gather source files.
        SourceSearcher searcher = new SourceSearcher(mSourceFolders, EXT_RS, EXT_FS);
        FileGatherer fileGatherer = new FileGatherer();
        searcher.search(fileGatherer);
        return fileGatherer.getFiles();
    }

    @Nullable
    public Set<File> getOldOutputs() {
        return mOldOutputs;
    }

    @Nullable
    public Set<File> getOldInputs() {
        return mOldInputs;
    }

    public void cleanDependencies() {
        if (mDependencyFiles != null) {
            for (DependencyFile depFile : mDependencyFiles) {
                depFile.getFile().delete();
            }
        }
    }

    @NonNull
    public List<File> getSourceFolders() {
        return mSourceFolders;
    }
}
