/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.sdklib.internal.androidTarget;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.ISystemImage.LocationType;
import com.android.sdklib.SystemImage;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A mock PlatformTarget.
 * This reimplements the minimum needed from the interface for our limited testing needs.
 */
public class MockPlatformTarget implements IAndroidTarget {

    private final int mApiLevel;
    private final int mRevision;
    private ISystemImage[] mSystemImages;

    public MockPlatformTarget(int apiLevel, int revision) {
        mApiLevel = apiLevel;
        mRevision = revision;
    }

    @Override
    public String getClasspathName() {
        return getName();
    }

    @Override
    public String getShortClasspathName() {
        return getName();
    }

    @Override
    public File getDefaultSkin() {
        return null;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public ISystemImage[] getSystemImages() {
        if (mSystemImages == null) {
            SystemImage si = new SystemImage(
                    FileOp.append(getLocation(), SdkConstants.OS_IMAGES_FOLDER),
                    LocationType.IN_LEGACY_FOLDER,
                    SystemImage.DEFAULT_TAG,
                    SdkConstants.ABI_ARMEABI,
                    FileOp.EMPTY_FILE_ARRAY);
            mSystemImages = new SystemImage[] { si };
        }
        return mSystemImages;
    }

    @Override
    @Nullable
    public ISystemImage getSystemImage(@NonNull IdDisplay tag, @NonNull String abiType) {
        if (SystemImage.DEFAULT_TAG.equals(tag) && SdkConstants.ABI_ARMEABI.equals(abiType)) {
            return getSystemImages()[0];
        }
        return null;
    }

    @Override
    public String getLocation() {
        return "/sdk/platforms/android-" + getVersion().getApiString();
    }

    @NonNull
    @Override
    public List<OptionalLibrary> getOptionalLibraries() {
        return ImmutableList.of();
    }

    @NonNull
    @Override
    public List<OptionalLibrary> getAdditionalLibraries() {
        return ImmutableList.of();
    }

    @Override
    public IAndroidTarget getParent() {
        return null;
    }

    @Override
    public String getPath(int pathId) {
        throw new UnsupportedOperationException("Implement this as needed for tests");
    }

    @Override
    public File getFile(int pathId) {
        return new File(getPath(pathId));
    }

    @Override
    public BuildToolInfo getBuildToolInfo() {
        return null;
    }

    @Override @NonNull
    public List<String> getBootClasspath() {
        throw new UnsupportedOperationException("Implement this as needed for tests");
    }

    @Override
    public String[] getPlatformLibraries() {
        return null;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public Integer getProperty(String name, Integer defaultValue) {
        return defaultValue;
    }

    @Override
    public Boolean getProperty(String name, Boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public int getRevision() {
        return mRevision;
    }

    @NonNull
    @Override
    public File[] getSkins() {
        return FileOp.EMPTY_FILE_ARRAY;
    }

    @Override
    public int getUsbVendorId() {
        return 0;
    }

    /**
     * Returns a vendor that depends on the parent *platform* API.
     * This works well in Unit Tests where we'll typically have different
     * platforms as unique identifiers.
     */
    @Override
    public String getVendor() {
        return "vendor " + Integer.toString(mApiLevel);
    }

    /**
     * Create a synthetic name using the target API level.
     */
    @Override
    public String getName() {
        return "platform r" + Integer.toString(mApiLevel);
    }

    @NonNull
    @Override
    public AndroidVersion getVersion() {
        return new AndroidVersion(mApiLevel, null /*codename*/);
    }

    @Override
    public String getVersionName() {
        return String.format("android-%1$d", mApiLevel);
    }

    @Override
    public String hashString() {
        return getVersionName();
    }

    /** Returns true for a platform. */
    @Override
    public boolean isPlatform() {
        return true;
    }

    @Override
    public boolean canRunOn(IAndroidTarget target) {
        throw new UnsupportedOperationException("Implement this as needed for tests");
    }

    @Override
    public int compareTo(IAndroidTarget o) {
        throw new UnsupportedOperationException("Implement this as needed for tests");
    }

    @Override
    public boolean hasRenderingLibrary() {
        return false;
    }
}
