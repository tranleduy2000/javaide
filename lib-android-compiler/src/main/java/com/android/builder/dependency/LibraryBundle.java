/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.builder.dependency;

import static com.android.SdkConstants.FD_AIDL;
import static com.android.SdkConstants.FD_ASSETS;
import static com.android.SdkConstants.FD_JARS;
import static com.android.SdkConstants.FD_RES;
import static com.android.SdkConstants.FN_ANDROID_MANIFEST_XML;
import static com.android.SdkConstants.FN_ANNOTATIONS_ZIP;
import static com.android.SdkConstants.FN_CLASSES_JAR;
import static com.android.SdkConstants.FN_PUBLIC_TXT;
import static com.android.SdkConstants.FN_RESOURCE_TEXT;
import static com.android.SdkConstants.LIBS_FOLDER;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.concurrency.Immutable;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the LibraryDependency interface that handles a default bundle project
 * structure.
 */
@Immutable
public abstract class LibraryBundle implements LibraryDependency {

    public static final String FN_PROGUARD_TXT = "proguard.txt";

    private final File mBundle;
    private final File mBundleFolder;
    private final String mName;
    private final String mProjectPath;

    /**
     * Creates the bundle dependency with an optional name
     *
     * @param bundle the library's aar bundle file
     * @param bundleFolder the folder containing the unarchived library content
     * @param name an optional name
     * @param projectPath an optional project path.
     */
    protected LibraryBundle(
            @NonNull File bundle,
            @NonNull File bundleFolder,
            @Nullable String name,
            @Nullable String projectPath) {
        mBundle = bundle;
        mBundleFolder = bundleFolder;
        mName = name;
        mProjectPath = projectPath;
    }

    @Override
    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    @Override
    public String getProject() {
        return mProjectPath;
    }

    @Nullable
    @Override
    public String getProjectVariant() {
        return null;
    }

    @Override
    @NonNull
    public File getManifest() {
        return new File(mBundleFolder, FN_ANDROID_MANIFEST_XML);
    }

    @Override
    @NonNull
    public File getSymbolFile() {
        return new File(mBundleFolder, FN_RESOURCE_TEXT);
    }

    @Override
    @NonNull
    public File getBundle() {
        return mBundle;
    }

    @Override
    @NonNull
    public File getFolder() {
        return mBundleFolder;
    }

    @Override
    @NonNull
    public File getJarFile() {
        return new File(getJarsRootFolder(), FN_CLASSES_JAR);
    }

    @Override
    @NonNull
    public List<JarDependency> getLocalDependencies() {
        List<File> jars = getLocalJars();
        List<JarDependency> localDependencies = Lists.newArrayListWithCapacity(jars.size());
        for (File jar : jars) {
            localDependencies.add(new JarDependency(jar, true, true, null, null));
        }

        return localDependencies;
    }

    @NonNull
    @Override
    public List<File> getLocalJars() {
        List<File> localJars = Lists.newArrayList();
        File[] jarList = new File(getJarsRootFolder(), LIBS_FOLDER).listFiles();
        if (jarList != null) {
            for (File jars : jarList) {
                if (jars.isFile() && jars.getName().endsWith(".jar")) {
                    localJars.add(jars);
                }
            }
        }

        return localJars;
    }

    @Override
    @NonNull
    public File getResFolder() {
        return new File(mBundleFolder, FD_RES);
    }

    @Override
    @NonNull
    public File getAssetsFolder() {
        return new File(mBundleFolder, FD_ASSETS);
    }

    @Override
    @NonNull
    public File getJniFolder() {
        return new File(mBundleFolder, "jni");
    }

    @Override
    @NonNull
    public File getAidlFolder() {
        return new File(mBundleFolder, FD_AIDL);
    }

    @Override
    @NonNull
    public File getRenderscriptFolder() {
        return new File(mBundleFolder, SdkConstants.FD_RENDERSCRIPT);
    }

    @Override
    @NonNull
    public File getProguardRules() {
        return new File(mBundleFolder, FN_PROGUARD_TXT);
    }

    @Override
    @NonNull
    public File getLintJar() {
        return new File(getJarsRootFolder(), "lint.jar");
    }

    @Override
    @NonNull
    public File getExternalAnnotations() {
        return new File(mBundleFolder, FN_ANNOTATIONS_ZIP);
    }

    @Override
    @NonNull
    public File getPublicResources() {
        return new File(mBundleFolder, FN_PUBLIC_TXT);
    }

    @NonNull
    public File getBundleFolder() {
        return mBundleFolder;
    }

    @NonNull
    protected File getJarsRootFolder() {
        return new File(mBundleFolder, FD_JARS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryBundle that = (LibraryBundle) o;
        return Objects.equal(mName, that.mName) &&
                Objects.equal(mProjectPath, that.mProjectPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mName, mProjectPath);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mBundle", mBundle)
                .add("mBundleFolder", mBundleFolder)
                .add("mName", mName)
                .add("mProjectPath", mProjectPath)
                .toString();
    }
}
