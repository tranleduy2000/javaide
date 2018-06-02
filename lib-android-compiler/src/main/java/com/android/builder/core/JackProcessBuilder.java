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

import com.android.annotations.NonNull;
import com.android.ide.common.process.JavaProcessInfo;
import com.android.ide.common.process.ProcessEnvBuilder;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * A builder to create a Jack-specific ProcessInfoBuilder
 */
public class JackProcessBuilder extends ProcessEnvBuilder<JackProcessBuilder> {

    static final FullRevision JACK_MIN_REV = new FullRevision(21, 1, 0);

    private boolean mDebugLog = false;
    private boolean mVerbose = false;
    private String mClasspath = null;
    private File mDexOutputFolder = null;
    private File mJackOutputFile = null;
    private List<File> mImportFiles = null;
    private List<File> mProguardFiles = null;
    private String mJavaMaxHeapSize = null;
    private File mMappingFile = null;
    private boolean mMultiDex = false;
    private int mMinSdkVersion = 21;
    private File mEcjOptionFile = null;
    private Collection<File> mJarJarRuleFiles = null;
    private File mIncrementalDir = null;

    public JackProcessBuilder() {
    }

    @NonNull
    public JackProcessBuilder setDebugLog(boolean debugLog) {
        mDebugLog = debugLog;
        return this;
    }

    @NonNull
    public JackProcessBuilder setVerbose(boolean verbose) {
        mVerbose = verbose;
        return this;
    }

    @NonNull
    public JackProcessBuilder setJavaMaxHeapSize(String javaMaxHeapSize) {
        mJavaMaxHeapSize = javaMaxHeapSize;
        return this;
    }

    @NonNull
    public JackProcessBuilder setClasspath(String classpath) {
        mClasspath = classpath;
        return this;
    }

    @NonNull
    public JackProcessBuilder setDexOutputFolder(File dexOutputFolder) {
        mDexOutputFolder = dexOutputFolder;
        return this;
    }

    @NonNull
    public JackProcessBuilder setJackOutputFile(File jackOutputFile) {
        mJackOutputFile = jackOutputFile;
        return this;
    }

    @NonNull
    public JackProcessBuilder addImportFiles(@NonNull Collection<File> importFiles) {
        if (mImportFiles == null) {
            mImportFiles = Lists.newArrayListWithExpectedSize(importFiles.size());
        }

        mImportFiles.addAll(importFiles);
        return this;
    }

    @NonNull
    public JackProcessBuilder addProguardFiles(@NonNull Collection<File> proguardFiles) {
        if (mProguardFiles == null) {
            mProguardFiles = Lists.newArrayListWithExpectedSize(proguardFiles.size());
        }

        mProguardFiles.addAll(proguardFiles);
        return this;
    }

    @NonNull
    public JackProcessBuilder setMappingFile(File mappingFile) {
        mMappingFile = mappingFile;
        return this;
    }

    @NonNull
    public JackProcessBuilder setMultiDex(boolean multiDex) {
        mMultiDex = multiDex;
        return this;
    }

    @NonNull
    public JackProcessBuilder setMinSdkVersion(int minSdkVersion) {
        mMinSdkVersion = minSdkVersion;
        return this;
    }

    @NonNull
    public JackProcessBuilder setEcjOptionFile(File ecjOptionFile) {
        mEcjOptionFile = ecjOptionFile;
        return this;
    }

    @NonNull
    public JackProcessBuilder setJarJarRuleFiles(@NonNull Collection<File> jarJarRuleFiles) {
        mJarJarRuleFiles = jarJarRuleFiles;
        return this;
    }

    @NonNull
    public JavaProcessInfo build(@NonNull BuildToolInfo buildToolInfo) throws ProcessException {

        FullRevision revision = buildToolInfo.getRevision();
        if (revision.compareTo(JACK_MIN_REV) < 0) {
            throw new ProcessException(
                    "Jack requires Build Tools " + JACK_MIN_REV.toString() +
                    " or later");
        }

        ProcessInfoBuilder builder = new ProcessInfoBuilder();
        builder.addEnvironments(mEnvironment);

        String jackJar = buildToolInfo.getPath(BuildToolInfo.PathId.JACK);
        if (jackJar == null || !new File(jackJar).isFile()) {
            throw new IllegalStateException("jack.jar is missing");
        }

        builder.setClasspath(jackJar);
        builder.setMain("com.android.jack.Main");

        if (mJavaMaxHeapSize != null) {
            builder.addJvmArg("-Xmx" + mJavaMaxHeapSize);
        } else {
            builder.addJvmArg("-Xmx1024M");
        }

        if (mDebugLog) {
            builder.addArgs("--verbose", "debug");
        } else if (mVerbose) {
            builder.addArgs("--verbose", "info");
        }

        builder.addArgs("--classpath", mClasspath);

        if (mImportFiles != null) {
            for (File lib : mImportFiles) {
                builder.addArgs("--import", lib.getAbsolutePath());
            }
        }

        builder.addArgs("--output-dex", mDexOutputFolder.getAbsolutePath());

        builder.addArgs("--output-jack", mJackOutputFile.getAbsolutePath());

        builder.addArgs("-D", "jack.import.resource.policy=keep-first");

        builder.addArgs("-D", "jack.reporter=sdk");

        if (mProguardFiles != null && !mProguardFiles.isEmpty()) {
            for (File file : mProguardFiles) {
                builder.addArgs("--config-proguard", file.getAbsolutePath());
            }
        }

        if (mMappingFile != null) {
            builder.addArgs("-D", "jack.obfuscation.mapping.dump=true");
            builder.addArgs("-D", "jack.obfuscation.mapping.dump.file=" + mMappingFile.getAbsolutePath());
        }

        if (mMultiDex) {
            builder.addArgs("--multi-dex");
            if (mMinSdkVersion < 21) {
                builder.addArgs("legacy");
            } else {
                builder.addArgs("native");
            }
        }

        if (mJarJarRuleFiles != null) {
            for (File jarjarRuleFile : mJarJarRuleFiles) {
                builder.addArgs("--config-jarjar", jarjarRuleFile.getAbsolutePath());
            }
        }

        builder.addArgs("@" + mEcjOptionFile.getAbsolutePath());

        return builder.createJavaProcess();
    }
}
