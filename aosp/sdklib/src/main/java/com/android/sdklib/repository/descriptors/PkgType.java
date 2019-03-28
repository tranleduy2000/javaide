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

package com.android.sdklib.repository.descriptors;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.internal.repository.LocalSdkParser;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.local.LocalSdk;

import java.util.EnumSet;

/**
 * Package types handled by the {@link LocalSdk}.
 * <p/>
 * Integer bit values are provided via {@link #getIntValue()} for backward
 * compatibility with the older {@link LocalSdkParser} class.
 * The integer bit values also indicate the natural ordering of the packages.
 */
public enum PkgType implements IPkgCapabilities {

    // Boolean attributes below, in that order:
    //      maj-r, full-r, api, path, tag, vend, min-t-r, min-pt-r
    //
    // Corresponding flags for list description pattern string:
    //      $MAJ  $FULL  $API  $PATH  $TAG  $VEND  $NAME (for extras & add-ons)
    //
    //

    /** Filter the SDK/tools folder.
     *  Has {@link FullRevision}. */
    PKG_TOOLS(0x0001, SdkConstants.FD_TOOLS,
            "Android SDK Tools $FULL",
            false, true /*full-r*/, false, false, false, false, false, true /*min-pt-r*/),

    /** Filter the SDK/platform-tools folder.
     *  Has {@link FullRevision}. */
    PKG_PLATFORM_TOOLS(0x0002, SdkConstants.FD_PLATFORM_TOOLS,
            "Android SDK Platform-Tools $FULL",
            false, true /*full-r*/, false, false, false, false, false, false),

    /** Filter the SDK/build-tools folder.
     *  Has {@link FullRevision}. */
    PKG_BUILD_TOOLS(0x0004, SdkConstants.FD_BUILD_TOOLS,
            "Android SDK Build-Tools $FULL",
            false, true /*full-r*/, false, false, false, false, false, false),

    /** Filter the SDK/docs folder.
     *  Has {@link MajorRevision}. */
    PKG_DOC(0x0010, SdkConstants.FD_DOCS,
            "Documentation for Android SDK",
            true /*maj-r*/, false, true /*api*/, false, false, false, false, false),

    /** Filter the SDK/platforms.
     *  Has {@link AndroidVersion}. Has {@link MajorRevision}.
     *  Path returns the platform's target hash. */
    PKG_PLATFORM(0x0100, SdkConstants.FD_PLATFORMS,
            "Android SDK Platform $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, true /*path*/, false, false, true /*min-t-r*/, false),

    /** Filter the SDK/system-images/android.
     * Has {@link AndroidVersion}. Has {@link MajorRevision}. Has tag.
     * Path returns the system image ABI. */
    PKG_SYS_IMAGE(0x0200, SdkConstants.FD_SYSTEM_IMAGES,
            "$PATH System Image, Android $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, true /*path*/, true /*tag*/, false /*vend*/, false, false),

    /** Filter the SDK/addons.
     *  Has {@link AndroidVersion}. Has {@link MajorRevision}.
     *  Path returns the add-on's target hash. */
    PKG_ADDON(0x0400, SdkConstants.FD_ADDONS,
            "{|$NAME|$VEND $PATH|}, Android $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, true /*path*/, false, true /*vend*/, false, false),

    /** Filter the SDK/system-images/addons.
     * Has {@link AndroidVersion}. Has {@link MajorRevision}. Has tag.
     * Path returns the system image ABI. */
    PKG_ADDON_SYS_IMAGE(0x0800, SdkConstants.FD_SYSTEM_IMAGES,
            "{|$NAME|$VEND $PATH|} System Image, Android $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, true /*path*/, true /*tag*/, true /*vend*/, false, false),

    /** Filter the SDK/samples folder.
     *  Note: this will not detect samples located in the SDK/extras packages.
     *  Has {@link AndroidVersion}. Has {@link MajorRevision}. */
    PKG_SAMPLE(0x1000, SdkConstants.FD_SAMPLES,
            "Samples for Android $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, false, false, false, true /*min-t-r*/, false),

    /** Filter the SDK/sources folder.
     *  Has {@link AndroidVersion}. Has {@link MajorRevision}. */
    PKG_SOURCE(0x2000, SdkConstants.FD_ANDROID_SOURCES,
            "Sources for Android $API{?$MAJ>1:, rev $MAJ}",
            true /*maj-r*/, false, true /*api*/, false, false, false, false, false),

    /** Filter the SDK/extras folder.
     *  Has {@code Path}. Has {@link MajorRevision}.
     *  Path returns the combined vendor id + extra path.
     *  Cast the descriptor to {@link IPkgDescExtra} to get extra's specific attributes. */
    PKG_EXTRA(0x4000, SdkConstants.FD_EXTRAS,
            "{|$NAME|$VEND $PATH|}{?$FULL>1:, rev $FULL}",
            false, true /*full-r*/, false, true /*path*/, false, true /*vend*/, false, false),

    /** The SDK/ndk folder. */
    PKG_NDK(0x8000, SdkConstants.FD_NDK, "",
                    false, true, false, false, false, false, false, false);

    /** A collection of all the known PkgTypes. */
    public static final EnumSet<PkgType> PKG_ALL = EnumSet.allOf(PkgType.class);

    /** Integer value matching all available pkg types, for the old LocalSdkParer. */
    public static final int PKG_ALL_INT = 0xFFFF;

    private int mIntValue;
    private String mFolderName;

    private final boolean mHasMajorRevision;
    private final boolean mHasFullRevision;
    private final boolean mHasAndroidVersion;
    private final boolean mHasPath;
    private final boolean mHasTag;
    private final boolean mHasVendor;
    private final boolean mHasMinToolsRev;
    private final boolean mHasMinPlatformToolsRev;
    private final String mListDisplayPattern;

    PkgType(int intValue,
            @NonNull String folderName,
            @NonNull String listDisplayPattern,
            boolean hasMajorRevision,
            boolean hasFullRevision,
            boolean hasAndroidVersion,
            boolean hasPath,
            boolean hasTag,
            boolean hasVendor,
            boolean hasMinToolsRev,
            boolean hasMinPlatformToolsRev) {
        mIntValue = intValue;
        mFolderName = folderName;
        mListDisplayPattern = listDisplayPattern;
        mHasMajorRevision = hasMajorRevision;
        mHasFullRevision = hasFullRevision;
        mHasAndroidVersion = hasAndroidVersion;
        mHasPath = hasPath;
        mHasTag = hasTag;
        mHasVendor = hasVendor;
        mHasMinToolsRev = hasMinToolsRev;
        mHasMinPlatformToolsRev = hasMinPlatformToolsRev;
    }

    /** Returns the integer value matching the type, compatible with the old LocalSdkParer. */
    public int getIntValue() {
        return mIntValue;
    }

    /** Returns the name of SDK top-folder where this type of package is stored. */
    @NonNull
    public String getFolderName() {
        return mFolderName;
    }

    @Override
    public boolean hasMajorRevision() {
        return mHasMajorRevision;
    }

    @Override
    public boolean hasFullRevision() {
        return mHasFullRevision;
    }

    @Override
    public boolean hasAndroidVersion() {
        return mHasAndroidVersion;
    }

    @Override
    public boolean hasPath() {
        return mHasPath;
    }

    @Override
    public boolean hasTag() {
        return mHasTag;
    }

    @Override
    public boolean hasVendor() {
        return mHasVendor;
    }

    @Override
    public boolean hasMinToolsRev() {
        return mHasMinToolsRev;
    }

    @Override
    public boolean hasMinPlatformToolsRev() {
        return mHasMinPlatformToolsRev;
    }

    /*
     * Returns a pattern string used by {@link PkgDesc#getListDescription()} to
     * compute a default list-display representation string for this package.
     */
    public String getListDisplayPattern() {
        return mListDisplayPattern;
    }
}

