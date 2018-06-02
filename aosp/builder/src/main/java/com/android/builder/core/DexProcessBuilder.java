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

package com.android.builder.core;

import static com.google.common.base.Preconditions.checkState;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.process.JavaProcessInfo;
import com.android.ide.common.process.ProcessEnvBuilder;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A builder to create a dex-specific ProcessInfoBuilder
 */
public class DexProcessBuilder extends ProcessEnvBuilder<DexProcessBuilder> {
    private static final FullRevision MIN_BUILD_TOOLS_REVISION_FOR_DEX_INPUT_LIST = new FullRevision(21, 0, 0);
    private static final FullRevision MIN_MULTIDEX_BUILD_TOOLS_REV = new FullRevision(21, 0, 0);
    private static final FullRevision MIN_MULTI_THREADED_DEX_BUILD_TOOLS_REV = new FullRevision(22, 0, 2);

    @NonNull
    private final File mOutputFile;
    private boolean mVerbose = false;
    private boolean mIncremental = false;
    private boolean mNoOptimize = false;
    private boolean mMultiDex = false;
    private File mMainDexList = null;
    private Set<File> mInputs = Sets.newHashSet();
    private File mTempInputFolder = null;
    private List<String> mAdditionalParams = null;

    public DexProcessBuilder(@NonNull File outputFile) {
        mOutputFile = outputFile;
    }

    @NonNull
    public DexProcessBuilder setVerbose(boolean verbose) {
        mVerbose = verbose;
        return this;
    }

    @NonNull
    public DexProcessBuilder setIncremental(boolean incremental) {
        mIncremental = incremental;
        return this;
    }

    @NonNull
    public DexProcessBuilder setNoOptimize(boolean noOptimize) {
        mNoOptimize = noOptimize;
        return this;
    }

    @NonNull
    public DexProcessBuilder setMultiDex(boolean multiDex) {
        mMultiDex = multiDex;
        return this;
    }

    @NonNull
    public DexProcessBuilder setMainDexList(File mainDexList) {
        mMainDexList = mainDexList;
        return this;
    }

    @NonNull
    public DexProcessBuilder addInput(File input) {
        mInputs.add(input);
        return this;
    }

    @NonNull
    public DexProcessBuilder addInputs(@NonNull Collection<File> inputs) {
        mInputs.addAll(inputs);
        return this;
    }

    @NonNull
    public DexProcessBuilder setTempInputFolder(File tempInputFolder) {
        mTempInputFolder = tempInputFolder;
        return this;
    }

    @NonNull
    public DexProcessBuilder additionalParameters(@NonNull List<String> params) {
        if (mAdditionalParams == null) {
            mAdditionalParams = Lists.newArrayListWithExpectedSize(params.size());
        }

        mAdditionalParams.addAll(params);

        return this;
    }

    @NonNull
    public JavaProcessInfo build(
            @NonNull BuildToolInfo buildToolInfo,
            @NonNull DexOptions dexOptions) throws ProcessException {

        checkState(
                !mMultiDex
                        || buildToolInfo.getRevision().compareTo(MIN_MULTIDEX_BUILD_TOOLS_REV) >= 0,
                "Multi dex requires Build Tools " +
                        MIN_MULTIDEX_BUILD_TOOLS_REV.toString() +
                        " / Current: " +
                        buildToolInfo.getRevision().toShortString());


        ProcessInfoBuilder builder = new ProcessInfoBuilder();
        builder.addEnvironments(mEnvironment);

        String dx = buildToolInfo.getPath(BuildToolInfo.PathId.DX_JAR);
        if (dx == null || !new File(dx).isFile()) {
            throw new IllegalStateException("dx.jar is missing");
        }

        builder.setClasspath(dx);
        builder.setMain("com.android.dx.command.Main");

        if (dexOptions.getJavaMaxHeapSize() != null) {
            builder.addJvmArg("-Xmx" + dexOptions.getJavaMaxHeapSize());
        } else {
            builder.addJvmArg("-Xmx1024M");
        }

        builder.addArgs("--dex");

        if (mVerbose) {
            builder.addArgs("--verbose");
        }

        if (dexOptions.getJumboMode()) {
            builder.addArgs("--force-jumbo");
        }

        if (mIncremental) {
            builder.addArgs("--incremental", "--no-strict");
        }

        if (mNoOptimize) {
            builder.addArgs("--no-optimize");
        }

        // only change thread count is build tools is 22.0.2+
        if (buildToolInfo.getRevision().compareTo(MIN_MULTI_THREADED_DEX_BUILD_TOOLS_REV) >= 0) {
            Integer threadCount = dexOptions.getThreadCount();
            if (threadCount == null) {
                builder.addArgs("--num-threads=4");
            } else {
                builder.addArgs("--num-threads=" + threadCount);
            }
        }

        if (mMultiDex) {
            builder.addArgs("--multi-dex");

            if (mMainDexList != null ) {
                builder.addArgs("--main-dex-list", mMainDexList.getAbsolutePath());
            }
        }

        if (mAdditionalParams != null) {
            for (String arg : mAdditionalParams) {
                builder.addArgs(arg);
            }
        }


        builder.addArgs("--output", mOutputFile.getAbsolutePath());

        // input
        builder.addArgs(getFilesToAdd(buildToolInfo));

        return builder.createJavaProcess();
    }

    @NonNull
    private List<String> getFilesToAdd(@NonNull BuildToolInfo buildToolInfo) throws
            ProcessException {
        // remove non-existing files.
        Set<File> existingFiles = Sets.filter(mInputs, new Predicate<File>() {
            @Override
            public boolean apply(@Nullable File input) {
                return input != null && input.exists();
            }
        });

        if (existingFiles.isEmpty()) {
            throw new ProcessException("No files to pass to dex.");
        }

        // sort the inputs
        List<File> sortedList = Lists.newArrayList(existingFiles);
        Collections.sort(sortedList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                boolean file2IsDir = file2.isDirectory();
                if (file.isDirectory()) {
                    return file2IsDir ? 0 : -1;
                } else if (file2IsDir) {
                    return 1;
                }

                long diff = file.length() - file2.length();
                return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
            }
        });

        // convert to String-based paths.
        List<String> filePathList = Lists.newArrayListWithCapacity(sortedList.size());
        for (File f : sortedList) {
            filePathList.add(f.getAbsolutePath());
        }

        if (mTempInputFolder != null && buildToolInfo.getRevision()
                .compareTo(MIN_BUILD_TOOLS_REVISION_FOR_DEX_INPUT_LIST) >= 0) {
            File inputListFile = new File(mTempInputFolder, "inputList.txt");
            // Write each library line by line to file
            try {
                Files.asCharSink(inputListFile, Charsets.UTF_8).writeLines(filePathList);
            } catch (IOException e) {
                throw new ProcessException(e);
            }
            return Collections.singletonList("--input-list=" + inputListFile.getAbsolutePath());
        } else {
            return filePathList;
        }
    }
}
