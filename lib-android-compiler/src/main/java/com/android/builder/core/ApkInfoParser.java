/*
 * Copyright (C) 2014 The Android Open Source Project
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
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.ide.common.process.BaseProcessOutputHandler;
import com.android.ide.common.process.CachedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.utils.SdkUtils;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse an APK with aapt to gather information
 */
public class ApkInfoParser {

    private static final Pattern PATTERN = Pattern.compile(
            "^package: name='([^']+)' versionCode='([0-9]*)' versionName='([^']*)'.*$");

    @NonNull
    private final File mAaptFile;
    @NonNull
    private final ProcessExecutor mProcessExecutor;

    /**
     * Information about an APK
     */
    public static final class ApkInfo {
        @NonNull
        private final String mPackageName;
        @Nullable
        private final Integer mVersionCode;
        @Nullable
        private final String mVersionName;

        private ApkInfo(@NonNull String packageName, Integer versionCode, String versionName) {
            mPackageName = packageName;
            mVersionCode = versionCode;
            mVersionName = versionName;
        }

        @NonNull
        public String getPackageName() {
            return mPackageName;
        }

        @Nullable
        public Integer getVersionCode() {
            return mVersionCode;
        }

        @Nullable
        public String getVersionName() {
            return mVersionName;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("packageName", mPackageName)
                    .add("versionCode", mVersionCode)
                    .add("versionName", mVersionName)
                    .toString();
        }
    }

    /**
     * Constructs a new parser
     * @param aaptFile aapt file to use to gather the info
     * @param processExecutor a process Executor to call aapt
     */
    public ApkInfoParser(
            @NonNull File aaptFile,
            @NonNull ProcessExecutor processExecutor) {
        mAaptFile = aaptFile;
        mProcessExecutor = processExecutor;
    }

    /**
     * Computes and returns the info for an APK
     * @param apkFile the APK to parse
     * @return a non-null ApkInfo object.
     * @throws ProcessException
     */
    @NonNull
    public ApkInfo parseApk(@NonNull File apkFile)
            throws ProcessException {

        if (!mAaptFile.isFile()) {
            throw new IllegalStateException(
                    "aapt is missing from location: " + mAaptFile.getAbsolutePath());
        }

        return getApkInfo(getAaptOutput(apkFile));
    }

    /**
     * Parses the aapt output and returns an ApkInfo object.
     * @param aaptOutput the aapt output as a list of lines.
     * @return an ApkInfo object.
     */
    @VisibleForTesting
    @NonNull
    static ApkInfo getApkInfo(@NonNull List<String> aaptOutput) {

        String pkgName = null, versionCode = null, versionName = null;

        for (String line : aaptOutput) {
            Matcher m = PATTERN.matcher(line);
            if (m.matches()) {
                pkgName = m.group(1);
                versionCode = m.group(2);
                versionName = m.group(3);
                break;
            }
        }

        if (pkgName == null) {
            throw new RuntimeException("Failed to find apk information with aapt");
        }

        Integer intVersionCode = null;
        try {
            intVersionCode = Integer.parseInt(versionCode);
        } catch(NumberFormatException ignore) {
            // leave the version code as null.
        }

        return new ApkInfo(pkgName, intVersionCode, versionName);
    }

    /**
     * Returns the aapt output.
     * @param apkFile the apk file to call aapt on.
     * @return the output as a list of files.
     * @throws ProcessException
     */
    @NonNull
    private List<String> getAaptOutput(@NonNull File apkFile)
            throws ProcessException {
        ProcessInfoBuilder builder = new ProcessInfoBuilder();

        builder.setExecutable(mAaptFile);
        builder.addArgs("dump", "badging", apkFile.getPath());

        CachedProcessOutputHandler processOutputHandler = new CachedProcessOutputHandler();

        mProcessExecutor.execute(
                builder.createProcess(), processOutputHandler)
                .rethrowFailure().assertNormalExitValue();

        BaseProcessOutputHandler.BaseProcessOutput output = processOutputHandler.getProcessOutput();

        return Splitter.on(SdkUtils.getLineSeparator()).splitToList(output.getStandardOutputAsString());
    }
}
