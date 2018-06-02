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

package com.android.sdklib.internal.androidTarget;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A target that we don't have, but is referenced (e.g. by a system image).
 */
public class MissingTarget implements IAndroidTarget {

    private final String mVendor;

    private final AndroidVersion mVersion;

    private final List<ISystemImage> mSystemImages = Lists.newArrayList();

    private final String mName;

    public MissingTarget(String vendor, String name, AndroidVersion version) {
        mVendor = vendor;
        mVersion = version;
        mName = name;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public String getVendor() {
        return mVendor;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String getClasspathName() {
        return null;
    }

    @Override
    public String getShortClasspathName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @NonNull
    @Override
    public AndroidVersion getVersion() {
        return mVersion;
    }

    @Override
    public String getVersionName() {
        return SdkVersionInfo.getAndroidName(getVersion().getApiLevel());
    }

    @Override
    public int getRevision() {
        return 0;
    }

    @Override
    public boolean isPlatform() {
        return mVendor == null;
    }

    @Override
    public IAndroidTarget getParent() {
        return null;
    }

    @Override
    public String getPath(int pathId) {
        return null;
    }

    @Override
    public File getFile(int pathId) {
        return null;
    }

    @Override
    public BuildToolInfo getBuildToolInfo() {
        return null;
    }

    @NonNull
    @Override
    public List<String> getBootClasspath() {
        return ImmutableList.of();
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
    public boolean hasRenderingLibrary() {
        return false;
    }

    @NonNull
    @Override
    public File[] getSkins() {
        return new File[0];
    }

    @Nullable
    @Override
    public File getDefaultSkin() {
        return null;
    }

    @Override
    public String[] getPlatformLibraries() {
        return new String[0];
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public Integer getProperty(String name, Integer defaultValue) {
        return null;
    }

    @Override
    public Boolean getProperty(String name, Boolean defaultValue) {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public int getUsbVendorId() {
        return 0;
    }

    @Override
    public ISystemImage[] getSystemImages() {
        return mSystemImages.toArray(new ISystemImage[mSystemImages.size()]);
    }

    public void addSystemImage(ISystemImage image) {
        mSystemImages.add(image);
    }

    @Nullable
    @Override
    public ISystemImage getSystemImage(@NonNull IdDisplay tag, @NonNull String abiType) {
        for (ISystemImage image : mSystemImages) {
            if (tag.equals(image.getTag()) && abiType.equals(image.getAbiType())) {
                return image;
            }
        }
        return null;
    }

    @Override
    public boolean canRunOn(IAndroidTarget target) {
        return false;
    }

    @Override
    public String hashString() {
        return AndroidTargetHash.getTargetHashString(this);
    }

    @Override
    public int compareTo(IAndroidTarget o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MissingTarget)) {
            return false;
        }
        MissingTarget other = (MissingTarget) obj;
        return Objects.equal(mVendor, other.mVendor) && Objects.equal(mVersion, other.mVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mVendor, mVersion);
    }
}
