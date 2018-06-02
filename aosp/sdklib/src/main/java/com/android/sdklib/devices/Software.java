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

package com.android.sdklib.devices;

import com.android.annotations.NonNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Software {
    private int mMinSdkLevel = 0;
    private int mMaxSdkLevel = Integer.MAX_VALUE;
    private boolean mLiveWallpaperSupport;
    private Set<BluetoothProfile> mBluetoothProfiles = EnumSet.noneOf(BluetoothProfile.class);
    private String mGlVersion;
    private Set<String> mGlExtensions = new LinkedHashSet<String>();
    private boolean mStatusBar;

    public int getMinSdkLevel() {
        return mMinSdkLevel;
    }

    public void setMinSdkLevel(int sdkLevel) {
        mMinSdkLevel = sdkLevel;
    }

    public int getMaxSdkLevel() {
        return mMaxSdkLevel;
    }

    public void setMaxSdkLevel(int sdkLevel) {
        mMaxSdkLevel = sdkLevel;
    }

    public boolean hasLiveWallpaperSupport() {
        return mLiveWallpaperSupport;
    }

    public void setLiveWallpaperSupport(boolean liveWallpaperSupport) {
        mLiveWallpaperSupport = liveWallpaperSupport;
    }

    @NonNull
    public Set<BluetoothProfile> getBluetoothProfiles() {
        return mBluetoothProfiles;
    }

    public void addBluetoothProfile(BluetoothProfile bp) {
        mBluetoothProfiles.add(bp);
    }

    public void addAllBluetoothProfiles(Collection<BluetoothProfile> bps) {
        mBluetoothProfiles.addAll(bps);
    }

    public String getGlVersion() {
        return mGlVersion;
    }

    public void setGlVersion(String version) {
        mGlVersion = version;
    }

    @NonNull
    public Set<String> getGlExtensions() {
        return mGlExtensions;
    }

    public void addGlExtension(String extension) {
        mGlExtensions.add(extension);
    }

    public void addAllGlExtensions(Collection<String> extensions) {
        mGlExtensions.addAll(extensions);
    }

    public void setStatusBar(boolean hasBar) {
        mStatusBar = hasBar;
    }

    public boolean hasStatusBar() {
        return mStatusBar;
    }

    public Software deepCopy() {
        Software s = new Software();
        s.setMinSdkLevel(getMinSdkLevel());
        s.setMaxSdkLevel(getMaxSdkLevel());
        s.setLiveWallpaperSupport(hasLiveWallpaperSupport());
        s.addAllBluetoothProfiles(getBluetoothProfiles());
        s.setGlVersion(getGlVersion());
        s.addAllGlExtensions(getGlExtensions());
        s.setStatusBar(hasStatusBar());
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Software)) {
            return false;
        }

        Software sw = (Software) o;
        return mMinSdkLevel == sw.getMinSdkLevel()
                && mMaxSdkLevel == sw.getMaxSdkLevel()
                && mLiveWallpaperSupport == sw.hasLiveWallpaperSupport()
                && mBluetoothProfiles.equals(sw.getBluetoothProfiles())
                && mGlVersion.equals(sw.getGlVersion())
                && mGlExtensions.equals(sw.getGlExtensions())
                && mStatusBar == sw.hasStatusBar();
    }

    @Override
    /** A stable hash across JVM instances */
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + mMinSdkLevel;
        hash = 31 * hash + mMaxSdkLevel;
        hash = 31 * hash + (mLiveWallpaperSupport ? 1 : 0);
        for (BluetoothProfile bp : mBluetoothProfiles) {
            hash = 31 * hash + bp.ordinal();
        }
        if (mGlVersion != null) {
            hash = 31 * hash + mGlVersion.hashCode();
        }
        for (String glExtension : mGlExtensions) {
            if (glExtension != null) {
                hash = 31 * hash + glExtension.hashCode();
            }
        }
        hash = 31 * hash + (mStatusBar ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Software <mMinSdkLevel=");
        sb.append(mMinSdkLevel);
        sb.append(", mMaxSdkLevel=");
        sb.append(mMaxSdkLevel);
        sb.append(", mLiveWallpaperSupport=");
        sb.append(mLiveWallpaperSupport);
        sb.append(", mBluetoothProfiles=");
        sb.append(mBluetoothProfiles);
        sb.append(", mGlVersion=");
        sb.append(mGlVersion);
        sb.append(", mGlExtensions=");
        sb.append(mGlExtensions);
        sb.append(", mStatusBar=");
        sb.append(mStatusBar);
        sb.append(">");
        return sb.toString();
    }

}
