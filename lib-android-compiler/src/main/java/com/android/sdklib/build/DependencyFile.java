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
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Parse a dependency file.
 */
public class DependencyFile {

    @NonNull
    private final File mDependencyFile;

    @NonNull
    private final List<File> mSourceFolders;

    private boolean mIsParsed = false;

    private List<File> mOutputFiles;
    private List<File> mInputFiles;
    private List<File> mSdkInputFiles;

    public DependencyFile(@NonNull File dependencyFile, @NonNull List<File> sourceFolders) {
        mDependencyFile = dependencyFile;
        mSourceFolders = sourceFolders;
    }

    @NonNull
    public File getFile() {
        return mDependencyFile;
    }

    @NonNull
    public List<File> getInputFiles() {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }
        return mInputFiles;
    }

    @NonNull
    public List<File> getSdkInputFiles() {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }
        return mSdkInputFiles;
    }

    @NonNull
    public List<File> getOutputFiles() {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }
        return mOutputFiles;
    }

    /**
     * Shortcut access to the first output file. This is useful for generator that only output
     * one file.
     */
    public File getFirstOutput() {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }

        if (!mOutputFiles.isEmpty()) {
            return mOutputFiles.get(0);
        }

        return null;
    }

    /**
     * Returns whether the given file is a dependency for this source file.
     * <p/>Note that the source file itself is not tested against. Therefore if
     * {@code file.equals(getSourceFile()} returns {@code true}, this method will return
     * {@code false}.
     * @param file the file to check against
     * @return true if the given file is a dependency for this source file.
     */
    public boolean hasInput(@NonNull File file) {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }

        return mInputFiles.contains(file);
    }

    /**
     * Returns whether the given file is an ouput of this source file.
     * @param file the file to test.
     * @return true if the file is an output file.
     */
    public boolean hasOutput(@NonNull File file) {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }
        return mOutputFiles.contains(file);
    }

    /**
     * Parses the dependency file(s)
     *
     */
    public void parse() throws IOException {
        if (!mDependencyFile.isFile()) {
            mInputFiles = Collections.emptyList();
            mOutputFiles = Collections.emptyList();
            mIsParsed = true;
            return;
        }

        //contents = file.getContents();
        List<String> lines = Files.readLines(mDependencyFile, Charsets.UTF_8);

        // we're going to be pretty brutal here.
        // The format is something like:
        // output1 output2 [...]: source dep1 dep2 [...]
        // expect it's likely split on several lines. So let's move it back on a single line
        // first
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.endsWith("\\")) {
                line = line.substring(0, line.length() - 1);
            }

            sb.append(line);
        }

        // split the left and right part
        String[] files = sb.toString().split(":");

        // get the output files:
        String[] outputs = files[0].trim().split(" ");
        mOutputFiles = getList(outputs);

        // and the dependency files:
        String[] inputs = files[1].trim().split(" ");


        if (inputs.length == 0) {
            mInputFiles = Collections.emptyList();
            mSdkInputFiles = Collections.emptyList();
        }

        mInputFiles = Lists.newArrayListWithExpectedSize(inputs.length);
        mSdkInputFiles = Lists.newArrayListWithExpectedSize(inputs.length);

        for (String path : inputs) {
            File f = new File(path);
            if (checkParentFile(f, mSourceFolders)) {
                mInputFiles.add(f);
            } else {
                mSdkInputFiles.add(f);
            }
        }

        mIsParsed = true;
    }

    /**
     * Checks whether a need for compilation is needed.
     *
     * THIS ONLY CHECK TIMESTAMP AND IS NOT A VALID WAY OF DOING THIS CHECK
     *
     * @return true if file timestamp detect a need for compilation
     *
     * @deprecated Use Gradle instead!
     *
     */
    @Deprecated
    public boolean needCompilation() {
        if (!mIsParsed) {
            throw new IllegalStateException("Parsing was not done");
        }

        // compares the earliest output time with the latest input time.
        // This is very basic, but temporary until we get better control in Gradle.

        long inputTime = 0;

        for (File file : mInputFiles) {
            long time = file.lastModified();
            if (time > inputTime) {
                inputTime = time;
            }
        }

        long outputTime = Long.MAX_VALUE;
        for (File file : mOutputFiles) {
            long time = file.lastModified();
            if (time < outputTime) {
                outputTime = time;
            }
        }

        return outputTime < inputTime;
    }

    private List<File> getList(@NonNull String[] paths) {
        if (paths.length == 0) {
            return Collections.emptyList();
        }

        List<File> list = Lists.newArrayListWithCapacity(paths.length);

        for (String path : paths) {
            list.add(new File(path));
        }

        return list;
    }

    @Override
    public String toString() {
        return "DependencyFile{" +
                "mDependencyFile=" + mDependencyFile +
                ", mIsParsed=" + mIsParsed +
                ", mOutputFiles=" + mOutputFiles +
                ", mInputFiles=" + mInputFiles +
                '}';
    }

    private static boolean checkParentFile(@NonNull File child, @NonNull List<File> parents) {
        for (File parent : parents) {
            if (parent.equals(child)) {
                return true;
            }
        }

        File childParent = child.getParentFile();
        if (childParent == null) {
            return false;
        }

        return checkParentFile(childParent, parents);
    }
}
