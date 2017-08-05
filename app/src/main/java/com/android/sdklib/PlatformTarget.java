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

package com.android.sdklib;

import com.android.sdklib.SdkManager.LayoutlibVersion;
import com.android.sdklib.util.SparseArray;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a platform target in the SDK.
 */
final class PlatformTarget implements IAndroidTarget {
    /** String used to get a hash to the platform target */
    private final static String PLATFORM_HASH = "android-%s";

    private final static String PLATFORM_VENDOR = "Android Open Source Project";

    private final static String PLATFORM_NAME = "Android %s";
    private final static String PLATFORM_NAME_PREVIEW = "Android %s (Preview)";

    /** the OS path to the root folder of the platform component. */
    private final String mRootFolderOsPath;
    private final String mName;
    private final AndroidVersion mVersion;
    private final String mVersionName;
    private final int mRevision;
    private final Map<String, String> mProperties;
    private final SparseArray<String> mPaths = new SparseArray<String>();
    private String[] mSkins;
    private final ISystemImage[] mSystemImages;
    private final LayoutlibVersion mLayoutlibVersion;

    /**
     * Creates a Platform target.
     *
     * @param sdkOsPath the root folder of the SDK
     * @param platformOSPath the root folder of the platform component
     * @param apiLevel the API Level
     * @param codeName the codename. can be null.
     * @param versionName the version name of the platform.
     * @param revision the revision of the platform component.
     * @param layoutlibVersion The {@link LayoutlibVersion}. May be null.
     * @param systemImages list of supported system images
     * @param properties the platform properties
     */
    @SuppressWarnings("deprecation")
    PlatformTarget(
            String sdkOsPath,
            String platformOSPath,
            int apiLevel,
            String codeName,
            String versionName,
            int revision,
            LayoutlibVersion layoutlibVersion,
            ISystemImage[] systemImages,
            Map<String, String> properties) {
        if (platformOSPath.endsWith(File.separator) == false) {
            platformOSPath = platformOSPath + File.separator;
        }
        mRootFolderOsPath = platformOSPath;
        mProperties = Collections.unmodifiableMap(properties);
        mVersion = new AndroidVersion(apiLevel, codeName);
        mVersionName = versionName;
        mRevision = revision;
        mLayoutlibVersion = layoutlibVersion;
        mSystemImages = systemImages == null ? new ISystemImage[0] : systemImages;
        Arrays.sort(mSystemImages);

        if (mVersion.isPreview()) {
            mName =  String.format(PLATFORM_NAME_PREVIEW, mVersionName);
        } else {
            mName = String.format(PLATFORM_NAME, mVersionName);
        }

        // pre-build the path to the platform components
        mPaths.put(ANDROID_JAR, mRootFolderOsPath + SdkConstants.FN_FRAMEWORK_LIBRARY);
        mPaths.put(SOURCES, mRootFolderOsPath + SdkConstants.FD_ANDROID_SOURCES);
        mPaths.put(ANDROID_AIDL, mRootFolderOsPath + SdkConstants.FN_FRAMEWORK_AIDL);
        mPaths.put(ANDROID_RS, mRootFolderOsPath + SdkConstants.OS_FRAMEWORK_RS);
        mPaths.put(ANDROID_RS_CLANG, mRootFolderOsPath + SdkConstants.OS_FRAMEWORK_RS_CLANG);
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

        // location for aapt, aidl, dx is now in the platform-tools folder.
        mPaths.put(AAPT, sdkOsPath + SdkConstants.OS_SDK_PLATFORM_TOOLS_FOLDER +
                SdkConstants.FN_AAPT);
        mPaths.put(AIDL, sdkOsPath + SdkConstants.OS_SDK_PLATFORM_TOOLS_FOLDER +
                SdkConstants.FN_AIDL);
        mPaths.put(DX, sdkOsPath + SdkConstants.OS_SDK_PLATFORM_TOOLS_FOLDER +
                SdkConstants.FN_DX);
        mPaths.put(DX_JAR, sdkOsPath + SdkConstants.OS_SDK_PLATFORM_TOOLS_LIB_FOLDER +
                SdkConstants.FN_DX_JAR);
    }

    /**
     * Returns the {@link LayoutlibVersion}. May be null.
     */
    public LayoutlibVersion getLayoutlibVersion() {
        return mLayoutlibVersion;
    }

    public ISystemImage getSystemImage(String abiType) {
        for (ISystemImage sysImg : mSystemImages) {
            if (sysImg.getAbiType().equals(abiType)) {
                return sysImg;
            }
        }
        return null;
    }

    public ISystemImage[] getSystemImages() {
        return mSystemImages;
    }

    public String getLocation() {
        return mRootFolderOsPath;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For Platform, the vendor name is always "Android".
     *
     * @see IAndroidTarget#getVendor()
     */
    public String getVendor() {
        return PLATFORM_VENDOR;
    }

    public String getName() {
        return mName;
    }

    public String getFullName() {
        return mName;
    }

    public String getClasspathName() {
        return mName;
    }

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
    public String getDescription() {
        return String.format("Standard Android platform %s", mVersionName);
    }

    public AndroidVersion getVersion() {
        return mVersion;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public int getRevision() {
        return mRevision;
    }

    public boolean isPlatform() {
        return true;
    }

    public IAndroidTarget getParent() {
        return null;
    }

    public String getPath(int pathId) {
        return mPaths.get(pathId);
    }

    /**
     * Returns whether the target is able to render layouts. This is always true for platforms.
     */
    public boolean hasRenderingLibrary() {
        return true;
    }


    public String[] getSkins() {
        return mSkins;
    }

    public String getDefaultSkin() {
        // only one skin? easy.
        if (mSkins.length == 1) {
            return mSkins[0];
        }

        // look for the skin name in the platform props
        String skinName = mProperties.get(SdkConstants.PROP_SDK_DEFAULT_SKIN);
        if (skinName != null) {
            return skinName;
        }

        // otherwise try to find a good default.
        if (mVersion.getApiLevel() >= 4) {
            // at this time, this is the default skin for all older platforms that had 2+ skins.
            return "WVGA800";
        }

        return "HVGA"; // this is for 1.5 and earlier.
    }

    /**
     * Always returns null, as a standard platform ha no optional libraries.
     *
     * {@inheritDoc}
     * @see IAndroidTarget#getOptionalLibraries()
     */
    public IOptionalLibrary[] getOptionalLibraries() {
        return null;
    }

    /**
     * Currently always return a fixed list with "android.test.runner" in it.
     * <p/>
     * TODO change the fixed library list to be build-dependent later.
     * {@inheritDoc}
     */
    public String[] getPlatformLibraries() {
        return new String[] { SdkConstants.ANDROID_TEST_RUNNER_LIB };
    }

    /**
     * The platform has no USB Vendor Id: always return {@link IAndroidTarget#NO_USB_ID}.
     * {@inheritDoc}
     */
    public int getUsbVendorId() {
        return NO_USB_ID;
    }

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

    public String hashString() {
        return String.format(PLATFORM_HASH, mVersion.getApiString());
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

    public String getProperty(String name) {
        return mProperties.get(name);
    }

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

    public Boolean getProperty(String name, Boolean defaultValue) {
        String value = getProperty(name);
        if (value != null) {
            return Boolean.valueOf(value);
        }

        return defaultValue;
    }

    public Map<String, String> getProperties() {
        return mProperties; // mProperties is unmodifiable.
    }

    // ---- platform only methods.

    void setSkins(String[] skins) {
        mSkins = skins;
    }

    void setSamplesPath(String osLocation) {
        mPaths.put(SAMPLES, osLocation);
    }
}
