/*
 * Copyright (C) 2008 The Android Open Source Project
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
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManager.LayoutlibVersion;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.utils.SparseArray;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a platform target in the SDK.
 */
public final class PlatformTarget implements IAndroidTarget {

    private static final String PLATFORM_VENDOR = "Android Open Source Project";

    private static final String PLATFORM_NAME = "Android %s";
    private static final String PLATFORM_NAME_PREVIEW = "Android %s (Preview)";

    /** the OS path to the root folder of the platform component. */
    private final String mRootFolderOsPath;
    private final String mName;
    private final AndroidVersion mVersion;
    private final String mVersionName;
    private final int mRevision;
    private final Map<String, String> mProperties;
    private final SparseArray<String> mPaths = new SparseArray<String>();
    private File[] mSkins;
    private final ISystemImage[] mSystemImages;
    private final List<OptionalLibrary> mOptionalLibraries;
    private final LayoutlibVersion mLayoutlibVersion;
    private final BuildToolInfo mBuildToolInfo;

    /**
     * Creates a Platform target.
     *
     * @param sdkOsPath the root folder of the SDK
     * @param platformOSPath the root folder of the platform component
     * @param apiVersion the API Level + codename.
     * @param versionName the version name of the platform.
     * @param revision the revision of the platform component.
     * @param layoutlibVersion The {@link LayoutlibVersion}. May be null.
     * @param systemImages list of supported system images
     * @param properties the platform properties
     */
    public PlatformTarget(
            String sdkOsPath,
            String platformOSPath,
            AndroidVersion apiVersion,
            String versionName,
            int revision,
            LayoutlibVersion layoutlibVersion,
            ISystemImage[] systemImages,
            Map<String, String> properties,
            List<OptionalLibrary> optionalLibraries,
            @NonNull BuildToolInfo buildToolInfo) {
        if (!platformOSPath.endsWith(File.separator)) {
            platformOSPath = platformOSPath + File.separator;
        }
        mRootFolderOsPath = platformOSPath;
        mProperties = Collections.unmodifiableMap(properties);
        mVersion = apiVersion;
        mVersionName = versionName;
        mRevision = revision;
        mLayoutlibVersion = layoutlibVersion;
        mBuildToolInfo = buildToolInfo;
        mSystemImages = systemImages == null ? new ISystemImage[0] : systemImages;
        Arrays.sort(mSystemImages);
        mOptionalLibraries = ImmutableList.copyOf(optionalLibraries);

        if (mVersion.isPreview()) {
            mName =  String.format(PLATFORM_NAME_PREVIEW, mVersionName);
        } else {
            mName = String.format(PLATFORM_NAME, mVersionName);
        }

        // pre-build the path to the platform components
        mPaths.put(ANDROID_JAR, mRootFolderOsPath + SdkConstants.FN_FRAMEWORK_LIBRARY);
        mPaths.put(UI_AUTOMATOR_JAR, mRootFolderOsPath + SdkConstants.FN_UI_AUTOMATOR_LIBRARY);
        mPaths.put(SOURCES, mRootFolderOsPath + SdkConstants.FD_ANDROID_SOURCES);
        mPaths.put(ANDROID_AIDL, mRootFolderOsPath + SdkConstants.FN_FRAMEWORK_AIDL);
        mPaths.put(SAMPLES, mRootFolderOsPath + SdkConstants.OS_PLATFORM_SAMPLES_FOLDER);
        mPaths.put(SKINS, mRootFolderOsPath + SdkConstants.OS_SKINS_FOLDER);
        mPaths.put(TEMPLATES, mRootFolderOsPath + SdkConstants.OS_PLATFORM_TEMPLATES_FOLDER);
        mPaths.put(DATA, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER);
        mPaths.put(ATTRIBUTES, mRootFolderOsPath + SdkConstants.OS_PLATFORM_ATTRS_XML);
        mPaths.put(MANIFEST_ATTRIBUTES,
                mRootFolderOsPath + SdkConstants.OS_PLATFORM_ATTRS_MANIFEST_XML);
        mPaths.put(RESOURCES, mRootFolderOsPath + SdkConstants.OS_PLATFORM_RESOURCES_FOLDER);
        mPaths.put(FONTS, mRootFolderOsPath + SdkConstants.OS_PLATFORM_FONTS_FOLDER);
        mPaths.put(LAYOUT_LIB, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_LAYOUTLIB_JAR);
        mPaths.put(WIDGETS, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_WIDGETS);
        mPaths.put(ACTIONS_ACTIVITY, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_ACTIVITY);
        mPaths.put(ACTIONS_BROADCAST, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_BROADCAST);
        mPaths.put(ACTIONS_SERVICE, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_SERVICE);
        mPaths.put(CATEGORIES, mRootFolderOsPath + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_CATEGORIES);
        mPaths.put(ANT, mRootFolderOsPath + SdkConstants.OS_PLATFORM_ANT_FOLDER);
    }

    /**
     * Returns the {@link LayoutlibVersion}. May be null.
     */
    public LayoutlibVersion getLayoutlibVersion() {
        return mLayoutlibVersion;
    }

    @Override
    @Nullable
    public ISystemImage getSystemImage(@NonNull IdDisplay tag, @NonNull String abiType) {
        for (ISystemImage sysImg : mSystemImages) {
            if (sysImg.getTag().equals(tag) && sysImg.getAbiType().equals(abiType)) {
                return sysImg;
            }
        }
        return null;
    }

    @Override
    public ISystemImage[] getSystemImages() {
        return mSystemImages;
    }

    @Override
    public String getLocation() {
        return mRootFolderOsPath;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For Platform, the vendor name is always "Android".
     *
     * @see com.android.sdklib.IAndroidTarget#getVendor()
     */
    @Override
    public String getVendor() {
        return PLATFORM_VENDOR;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getFullName() {
        return mName;
    }

    @Override
    public String getClasspathName() {
        return mName;
    }

    @Override
    public String getShortClasspathName() {
        return mName;
    }

    /*
     * (non-Javadoc)
     *
     * Description for the Android platform is dynamically generated.
     *
     * @see com.android.sdklib.IAndroidTarget#getDescription()
     */
    @Override
    public String getDescription() {
        return String.format("Standard Android platform %s", mVersionName);
    }

    @NonNull
    @Override
    public AndroidVersion getVersion() {
        return mVersion;
    }

    @Override
    public String getVersionName() {
        return mVersionName;
    }

    @Override
    public int getRevision() {
        return mRevision;
    }

    @Override
    public boolean isPlatform() {
        return true;
    }

    @Override
    public IAndroidTarget getParent() {
        return null;
    }

    @Override
    public String getPath(int pathId) {
        return mPaths.get(pathId);
    }

    @Override
    public File getFile(int pathId) {
        return new File(getPath(pathId));
    }

    @Override
    public BuildToolInfo getBuildToolInfo() {
        return mBuildToolInfo;
    }

    @Override @NonNull
    public List<String> getBootClasspath() {
        return ImmutableList.of(getPath(IAndroidTarget.ANDROID_JAR));
    }

    @NonNull
    @Override
    public List<OptionalLibrary> getOptionalLibraries() {
        return mOptionalLibraries;
    }

    /**
     * Always returns null, as a standard platform has no additional libraries.
     *
     * {@inheritDoc}
     * @see com.android.sdklib.IAndroidTarget#getAdditionalLibraries()
     */
    @NonNull
    @Override
    public List<OptionalLibrary> getAdditionalLibraries() {
        return ImmutableList.of();
    }

    /**
     * Returns whether the target is able to render layouts. This is always true for platforms.
     */
    @Override
    public boolean hasRenderingLibrary() {
        return true;
    }

    @NonNull
    @Override
    public File[] getSkins() {
        return mSkins;
    }

    @Nullable
    @Override
    public File getDefaultSkin() {
        // only one skin? easy.
        if (mSkins.length == 1) {
            return mSkins[0];
        }

        // look for the skin name in the platform props
        String skinName = mProperties.get(SdkConstants.PROP_SDK_DEFAULT_SKIN);
        if (skinName == null) {
            // otherwise try to find a good default.
            if (mVersion.getApiLevel() >= 4) {
                // at this time, this is the default skin for all older platforms that had 2+ skins.
                skinName = "WVGA800";                                       //$NON-NLS-1$
            } else {
                skinName = "HVGA"; // this is for 1.5 and earlier.          //$NON-NLS-1$
            }
        }

        return new File(getFile(IAndroidTarget.SKINS), skinName);
    }

    /**
     * Currently always return a fixed list with "android.test.runner" in it.
     * <p/>
     * TODO change the fixed library list to be build-dependent later.
     * {@inheritDoc}
     */
    @Override
    public String[] getPlatformLibraries() {
        return new String[] { SdkConstants.ANDROID_TEST_RUNNER_LIB };
    }

    /**
     * The platform has no USB Vendor Id: always return {@link IAndroidTarget#NO_USB_ID}.
     * {@inheritDoc}
     */
    @Override
    public int getUsbVendorId() {
        return NO_USB_ID;
    }

    @Override
    public boolean canRunOn(IAndroidTarget target) {
        // basic test
        if (target == this) {
            return true;
        }

        // if the platform has a codename (ie it's a preview of an upcoming platform), then
        // both platforms must be exactly identical.
        if (mVersion.getCodename() != null) {
            return mVersion.equals(target.getVersion());
        }

        // target is compatible wit the receiver as long as its api version number is greater or
        // equal.
        return target.getVersion().getApiLevel() >= mVersion.getApiLevel();
    }

    @Override
    public String hashString() {
        return AndroidTargetHash.getPlatformHashString(mVersion);
    }

    @Override
    public int hashCode() {
        return hashString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlatformTarget) {
            PlatformTarget platform = (PlatformTarget)obj;

            return mVersion.equals(platform.getVersion());
        }

        return false;
    }

    /*
     * Order by API level (preview/n count as between n and n+1).
     * At the same API level, order as: Platform first, then add-on ordered by vendor and then name
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IAndroidTarget target) {
        // quick check.
        if (this == target) {
            return 0;
        }

        int versionDiff = mVersion.compareTo(target.getVersion());

        // only if the version are the same do we care about add-ons.
        if (versionDiff == 0) {
            // platforms go before add-ons.
            if (target.isPlatform() == false) {
                return -1;
            }
        }

        return versionDiff;
    }

    /**
     * Returns a string representation suitable for debugging.
     * The representation is not intended for display to the user.
     *
     * The representation is also purposely compact. It does not describe _all_ the properties
     * of the target, only a few key ones.
     *
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return String.format("PlatformTarget %1$s rev %2$d",     //$NON-NLS-1$
                getVersion(),
                getRevision());
    }

    @Override
    public String getProperty(String name) {
        return mProperties.get(name);
    }

    @Override
    public Integer getProperty(String name, Integer defaultValue) {
        try {
            String value = getProperty(name);
            if (value != null) {
                return Integer.decode(value);
            }
        } catch (NumberFormatException e) {
            // ignore, return default value;
        }

        return defaultValue;
    }

    @Override
    public Boolean getProperty(String name, Boolean defaultValue) {
        String value = getProperty(name);
        if (value != null) {
            return Boolean.valueOf(value);
        }

        return defaultValue;
    }

    @Override
    public Map<String, String> getProperties() {
        return mProperties; // mProperties is unmodifiable.
    }

    // ---- platform only methods.

    public void setSkins(@NonNull File[] skins) {
        mSkins = skins;
        Arrays.sort(mSkins);
    }

    public void setSamplesPath(String osLocation) {
        mPaths.put(SAMPLES, osLocation);
    }

    public void setSourcesPath(String osLocation) {
        mPaths.put(SOURCES, osLocation);
    }
}
