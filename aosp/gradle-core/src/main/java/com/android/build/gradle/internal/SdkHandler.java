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

package com.android.build.gradle.internal;

import static com.android.SdkConstants.FN_LOCAL_PROPERTIES;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.LibraryRequest;
import com.android.builder.sdk.DefaultSdkLoader;
import com.android.builder.sdk.PlatformLoader;
import com.android.builder.sdk.SdkInfo;
import com.android.builder.sdk.SdkLoader;
import com.android.builder.sdk.TargetInfo;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Properties;

/**
 * Handles the all things SDK for the Gradle plugin. There is one instance per project, around
 * a singleton {@link com.android.builder.sdk.SdkLoader}.
 */
public class SdkHandler {

    // Used for injecting SDK location in tests.
    public static File sTestSdkFolder;

    @NonNull
    private final ILogger logger;

    private SdkLoader sdkLoader;
    private File sdkFolder;
    private File ndkFolder;
    private boolean isRegularSdk = true;

    public static void setTestSdkFolder(File testSdkFolder) {
        sTestSdkFolder = testSdkFolder;
    }

    public SdkHandler(@NonNull Project project,
                      @NonNull ILogger logger) {
        this.logger = logger;
        findLocation(project);
    }

    public SdkInfo getSdkInfo() {
        SdkLoader sdkLoader = getSdkLoader();
        return sdkLoader.getSdkInfo(logger);
    }

    public void initTarget(
            @NonNull String targetHash,
            @NonNull FullRevision buildToolRevision,
            @NonNull Collection<LibraryRequest> usedLibraries,
            @NonNull AndroidBuilder androidBuilder) {
        Preconditions.checkNotNull(targetHash, "android.compileSdkVersion is missing!");
        Preconditions.checkNotNull(buildToolRevision, "android.buildToolsVersion is missing!");

        SdkLoader sdkLoader = getSdkLoader();

        SdkInfo sdkInfo = sdkLoader.getSdkInfo(logger);
        TargetInfo targetInfo = sdkLoader.getTargetInfo(targetHash, buildToolRevision, logger);

        androidBuilder.setTargetInfo(sdkInfo, targetInfo, usedLibraries);
    }

    @Nullable
    public File getSdkFolder() {
        return sdkFolder;
    }

    @Nullable
    public File getAndCheckSdkFolder() {
        if (sdkFolder == null) {
            throw new RuntimeException(
                    "SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.");
        }

        return sdkFolder;
    }

    public synchronized SdkLoader getSdkLoader() {
        if (sdkLoader == null) {
            if (isRegularSdk) {
                getAndCheckSdkFolder();

                // check if the SDK folder actually exist.
                // For internal test we provide a fake SDK location through
                // setTestSdkFolder in order to have an SDK, even though we don't use it
                // so in this case we ignore the check.
                if (sTestSdkFolder == null && !sdkFolder.isDirectory()) {
                    throw new RuntimeException(String.format(
                            "The SDK directory '%1$s' does not exist.", sdkFolder));
                }

                sdkLoader = DefaultSdkLoader.getLoader(sdkFolder);
            } else {
                sdkLoader = PlatformLoader.getLoader(sdkFolder);
            }
        }

        return sdkLoader;
    }

    public synchronized void unload() {
        if (sdkLoader != null) {
            if (isRegularSdk) {
                DefaultSdkLoader.unload();
            } else {
                PlatformLoader.unload();
            }

            sdkLoader = null;
        }
    }

    @Nullable
    public File getNdkFolder() {
        return ndkFolder;
    }

    private void findSdkLocation(@NonNull Properties properties, @NonNull File rootDir) {
        String sdkDirProp = properties.getProperty("sdk.dir");
        if (sdkDirProp != null) {
            sdkFolder = new File(sdkDirProp);
            if (!sdkFolder.isAbsolute()) {
                sdkFolder = new File(rootDir, sdkDirProp);
            }
            return;
        }

        sdkDirProp = properties.getProperty("android.dir");
        if (sdkDirProp != null) {
            sdkFolder = new File(rootDir, sdkDirProp);
            isRegularSdk = false;
            return;
        }

        String envVar = System.getenv("ANDROID_HOME");
        if (envVar != null) {
            sdkFolder = new File(envVar);
            return;
        }

        String property = System.getProperty("android.home");
        if (property != null) {
            sdkFolder = new File(property);
        }
    }

    private void findNdkLocation(@NonNull Properties properties) {
        String ndkDirProp = properties.getProperty("ndk.dir");
        if (ndkDirProp != null) {
            ndkFolder = new File(ndkDirProp);
            return;
        }

        String envVar = System.getenv("ANDROID_NDK_HOME");
        if (envVar != null) {
            ndkFolder = new File(envVar);
        }
    }

    private void findLocation(@NonNull Project project) {
        if (sTestSdkFolder != null) {
            sdkFolder = sTestSdkFolder;
            return;
        }

        File rootDir = project.getRootDir();
        File localProperties = new File(rootDir, FN_LOCAL_PROPERTIES);
        Properties properties = new Properties();

        if (localProperties.isFile()) {
            InputStreamReader reader = null;
            try {
                //noinspection IOResourceOpenedButNotSafelyClosed
                FileInputStream fis = new FileInputStream(localProperties);
                reader = new InputStreamReader(fis, Charsets.UTF_8);
                properties.load(reader);
            } catch (FileNotFoundException ignored) {
                // ignore since we check up front and we don't want to fail on it anyway
                // in case there's an env var.
            } catch (IOException e) {
                throw new RuntimeException(
                        String.format("Unable to read %1$s.", localProperties.getAbsolutePath()),
                        e);
            } finally {
                try {
                    Closeables.close(reader, true /* swallowIOException */);
                } catch (IOException e) {
                    // ignore.
                }
            }
        }

        findSdkLocation(properties, rootDir);
        findNdkLocation(properties);
    }
}
