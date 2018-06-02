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

package com.android.builder.sdk;

import static com.android.SdkConstants.FN_AAPT;
import static com.android.SdkConstants.FN_AIDL;
import static com.android.SdkConstants.FN_BCC_COMPAT;
import static com.android.SdkConstants.FN_RENDERSCRIPT;
import static com.android.SdkConstants.FN_ZIPALIGN;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.builder.internal.FakeAndroidTarget;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Singleton-based implementation of SdkLoader for a platform-based SDK.
 *
 * Platform-based SDK are in the Android source tree in AOSP, using a different
 * folder layout for all the files.
 */
public class PlatformLoader implements SdkLoader {

    private static PlatformLoader sLoader;

    @NonNull
    private final File mTreeLocation;

    private File mHostToolsFolder;
    private SdkInfo mSdkInfo;
    @NonNull
    private final ImmutableList<File> mRepositories;

    public static synchronized SdkLoader getLoader(
            @NonNull File treeLocation) {
        if (sLoader == null) {
            sLoader = new PlatformLoader(treeLocation);
        } else if (!treeLocation.equals(sLoader.mTreeLocation)) {
            throw new IllegalStateException("Already created an SDK Loader with different SDK Path");
        }

        return sLoader;
    }

    public static synchronized void unload() {
        sLoader = null;
    }

    @NonNull
    @Override
    public TargetInfo getTargetInfo(@NonNull String targetHash,
            @NonNull FullRevision buildToolRevision, @NonNull ILogger logger) {
        init(logger);

        IAndroidTarget androidTarget = new FakeAndroidTarget(mTreeLocation.getPath(), targetHash);

        File hostTools = getHostToolsFolder();

        BuildToolInfo buildToolInfo = new BuildToolInfo(
                buildToolRevision,
                mTreeLocation,
                new File(hostTools, FN_AAPT),
                new File(hostTools, FN_AIDL),
                new File(mTreeLocation, "prebuilts/sdk/tools/dx"),
                new File(mTreeLocation, "prebuilts/sdk/tools/lib/dx.jar"),
                new File(hostTools, FN_RENDERSCRIPT),
                new File(mTreeLocation, "prebuilts/sdk/renderscript/include"),
                new File(mTreeLocation, "prebuilts/sdk/renderscript/clang-include"),
                new File(hostTools, FN_BCC_COMPAT),
                new File(hostTools, "arm-linux-androideabi-ld"),
                new File(hostTools, "i686-linux-android-ld"),
                new File(hostTools, "mipsel-linux-android-ld"),
                new File(hostTools, FN_ZIPALIGN));

        return new TargetInfo(androidTarget, buildToolInfo);
    }

    @NonNull
    @Override
    public SdkInfo getSdkInfo(@NonNull ILogger logger) {
        init(logger);
        return mSdkInfo;
    }

    @Override
    @NonNull
    public ImmutableList<File> getRepositories() {
        return mRepositories;
    }

    private PlatformLoader(@NonNull File treeLocation) {
        mTreeLocation = treeLocation;
        mRepositories = ImmutableList.of(new File(mTreeLocation + "/prebuilts/sdk/m2repository"));
    }

    private synchronized void init(@NonNull ILogger logger) {
        if (mSdkInfo == null) {
            String host;
            if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_DARWIN) {
                host = "darwin-x86";
            } else if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_LINUX) {
                host = "linux-x86";
            } else {
                throw new IllegalStateException(
                        "Windows is not supported for platform development");
            }

            mSdkInfo = new SdkInfo(
                    new File(mTreeLocation, "out/host/" + host + "/framework/annotations.jar"),
                    new File(mTreeLocation, "out/host/" + host + "/bin/adb"));
        }
    }

    @NonNull
    private synchronized File getHostToolsFolder() {
        if (mHostToolsFolder == null) {
            File tools = new File(mTreeLocation, "prebuilts/sdk/tools");
            if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_DARWIN) {
                mHostToolsFolder = new File(tools, "darwin/bin");
            } else if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_LINUX) {
                mHostToolsFolder = new File(tools, "linux/bin");
            } else {
                throw new IllegalStateException(
                        "Windows is not supported for platform development");
            }

            if (!mHostToolsFolder.isDirectory()) {
                throw new IllegalStateException("Host tools folder missing: " +
                        mHostToolsFolder.getAbsolutePath());
            }
        }

        return mHostToolsFolder;
    }
}
