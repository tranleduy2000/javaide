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

package com.android.ide.common.repository;

import static com.android.SdkConstants.FD_EXTRAS;
import static com.android.SdkConstants.FD_M2_REPOSITORY;
import static com.android.ide.common.repository.GradleCoordinate.COMPARE_PLUS_HIGHER;
import static java.io.File.separator;
import static java.io.File.separatorChar;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.NoPreviewRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.sdklib.repository.descriptors.PkgDesc;
import com.android.sdklib.repository.descriptors.PkgType;
import com.android.sdklib.repository.local.LocalPkgInfo;
import com.android.sdklib.repository.local.LocalSdk;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * A {@linkplain com.android.ide.common.repository.SdkMavenRepository} represents a Maven
 * repository that is shipped with the SDK and located in the {@code extras} folder of the
 * SDK location.
 */
public enum SdkMavenRepository {
    /** The Android repository; contains support lib, app compat, media router, etc */
    ANDROID("android", "Android Support Repository"),

    /** The Google repository; contains Play Services etc */
    GOOGLE("google", "Google Support Repository");

    private final String mDir;
    @NonNull private final String myDisplayName;

    SdkMavenRepository(@NonNull String dir, @NonNull String displayName) {
        mDir = dir;
        myDisplayName = displayName;
    }

    /**
     * Returns the location of the repository within a given SDK home
     * @param sdkHome the SDK home, or null
     * @param requireExists if true, the location will only be returned if it also exists
     * @return the location of the this repository within a given SDK
     */
    @Nullable
    public File getRepositoryLocation(@Nullable File sdkHome, boolean requireExists) {
        if (sdkHome != null) {
            File dir = new File(sdkHome, FD_EXTRAS + separator + mDir
                    + separator + FD_M2_REPOSITORY);
            if (!requireExists || dir.isDirectory()) {
                return dir;
            }
        }

        return null;
    }

    /**
     * Returns true if the given SDK repository is installed
     *
     * @param sdkHome the SDK installation location
     * @return true if the repository is installed
     */
    public boolean isInstalled(@Nullable File sdkHome) {
        return getRepositoryLocation(sdkHome, true) != null;
    }

    /**
     * Returns true if the given SDK repository is installed
     *
     * @param sdk the SDK to check
     * @return true if the repository is installed
     */
    public boolean isInstalled(@Nullable LocalSdk sdk) {
        if (sdk != null) {
            LocalPkgInfo[] infos = sdk.getPkgsInfos(PkgType.PKG_EXTRA);
            for (LocalPkgInfo info : infos) {
                IPkgDesc d = info.getDesc();
                //noinspection ConstantConditions,ConstantConditions
                if (d.hasVendor() && mDir.equals(d.getVendor().getId()) &&
                        d.hasPath() && FD_M2_REPOSITORY.equals(d.getPath())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Find the best matching {@link GradleCoordinate}
     *
     * @param sdkHome the SDK installation
     * @param groupId the artifact group id
     * @param artifactId the artifact id
     * @param filter an optional filter which the matched coordinate's version name must start with
     * @param allowPreview whether preview versions are allowed to match
     * @return the best (highest version) matching coordinate, or null if none were found
     */
    @Nullable
    public GradleCoordinate getHighestInstalledVersion(
            @Nullable File sdkHome,
            @NonNull String groupId,
            @NonNull String artifactId,
            @Nullable String filter,
            boolean allowPreview) {
        File repository = getRepositoryLocation(sdkHome, true);
        if (repository != null) {
            return getHighestInstalledVersion(groupId, artifactId, repository, filter,
                    allowPreview);
        }

        return null;
    }

    /**
     * Find the best matching {@link GradleCoordinate}
     *
     * @param groupId the artifact group id
     * @param artifactId the artifact id
     * @param repository the path to the m2repository directory
     * @param filter an optional filter which the matched coordinate's version name must start with
     * @param allowPreview whether preview versions are allowed to match
     * @return the best (highest version) matching coordinate, or null if none were found
     */
    @Nullable
    public static GradleCoordinate getHighestInstalledVersion(
            @NonNull String groupId,
            @NonNull String artifactId,
            @NonNull File repository,
            @Nullable String filter,
            boolean allowPreview) {
        assert FD_M2_REPOSITORY.equals(repository.getName()) : repository;

        File versionDir = new File(repository,
                groupId.replace('.', separatorChar) + separator + artifactId);
        File[] versions = versionDir.listFiles();
        if (versions != null) {
            List<GradleCoordinate> versionCoordinates = Lists.newArrayList();
            for (File dir : versions) {
                if (!dir.isDirectory()) {
                    continue;
                }
                if (filter != null && !dir.getName().startsWith(filter)) {
                    continue;
                }
                GradleCoordinate gc = GradleCoordinate.parseCoordinateString(
                        groupId + ":" + artifactId + ":" + dir.getName());

                if (gc != null && (allowPreview || !gc.getFullRevision().contains("-rc"))) {
                    if (!allowPreview && "5.2.08".equals(gc.getFullRevision()) &&
                            "play-services".equals(gc.getArtifactId())) {
                        // This specific version is actually a preview version which should
                        // not be used (https://code.google.com/p/android/issues/detail?id=75292)
                        continue;
                    }
                    FullRevision.parseRevision(gc.getFullRevision());
                    versionCoordinates.add(gc);
                }
            }
            if (!versionCoordinates.isEmpty()) {
                return Collections.max(versionCoordinates, COMPARE_PLUS_HIGHER);
            }
        }

        return null;
    }

    @Nullable
    public static SdkMavenRepository getByGroupId(@NonNull String groupId) {
        if ("com.android.support".equals(groupId) || "com.android.support.test".equals(groupId)) {
            return ANDROID;
        }
        if (groupId.startsWith("com.google.android.")) {
            // com.google.android.gms, com.google.android.support.wearable,
            // com.google.android.wearable, ... possibly more in the future
            return GOOGLE;
        }

        return null;
    }

    /** The directory name of the repository inside the extras folder */
    @NonNull
    public String getDirName() {
        return mDir;
    }

    /**
     * @return SDK package description for this repository
     */
    public IPkgDesc getPackageDescription() {
        return PkgDesc.Builder.newExtra(new IdDisplay(mDir, ""), FD_M2_REPOSITORY, myDisplayName,
                                        null, new NoPreviewRevision(1)).create();
    }
}
