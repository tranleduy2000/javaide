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
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents an add-on target in the SDK.
 * An add-on extends a standard {@link PlatformTarget}.
 */
public final class AddOnTarget implements IAndroidTarget {

    private final String mLocation;
    private final PlatformTarget mBasePlatform;
    private final String mName;
    private final ISystemImage[] mSystemImages;
    private final String mVendor;
    private final int mRevision;
    private final String mDescription;
    private final boolean mHasRenderingLibrary;
    private final boolean mHasRenderingResources;

    private File[] mSkins;
    private File mDefaultSkin;
    private ImmutableList<OptionalLibrary> mLibraries;
    private int mVendorId = NO_USB_ID;

    /**
     * Creates a new add-on
     * @param location the OS path location of the add-on
     * @param name the name of the add-on
     * @param vendor the vendor name of the add-on
     * @param revision the revision of the add-on
     * @param description the add-on description
     * @param systemImages list of supported system images. Can be null or empty.
     * @param libMap A map containing the optional libraries. The map key is the fully-qualified
     * library name. The value is a 2 string array with the .jar filename, and the description.
     * @param hasRenderingLibrary whether the addon has a custom layoutlib.jar
     * @param hasRenderingResources whether the add has custom framework resources.
     * @param basePlatform the platform the add-on is extending.
     */
    public AddOnTarget(
            String location,
            String name,
            String vendor,
            int revision,
            String description,
            ISystemImage[] systemImages,
            Map<String, String[]> libMap,
            boolean hasRenderingLibrary,
            boolean hasRenderingResources,
            PlatformTarget basePlatform) {
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }

        mLocation = location;
        mName = name;
        mVendor = vendor;
        mRevision = revision;
        mDescription = description;
        mHasRenderingLibrary = hasRenderingLibrary;
        mHasRenderingResources = hasRenderingResources;
        mBasePlatform = basePlatform;

        // If the add-on does not have any system-image of its own, the list here
        // is empty and it's up to the callers to query the parent platform.
        mSystemImages = systemImages == null ? new ISystemImage[0] : systemImages;
        Arrays.sort(mSystemImages);

        // handle the optional libraries.
        if (libMap != null) {
            ImmutableList.Builder<OptionalLibrary> builder = ImmutableList.builder();
            for (Entry<String, String[]> entry : libMap.entrySet()) {
                String jarFile = entry.getValue()[0];
                String desc = entry.getValue()[1];
                builder.add(new OptionalLibraryImpl(
                        entry.getKey(),
                        new File(mLocation, SdkConstants.OS_ADDON_LIBS_FOLDER + jarFile),
                        desc,
                        true /*requireManifestEntry*/));
            }
            mLibraries = builder.build();
        } else {
            mLibraries = ImmutableList.of();
        }
    }

    @Override
    public String getLocation() {
        return mLocation;
    }

    @Override
    public String getName() {
        return mName;
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
    public String getVendor() {
        return mVendor;
    }

    @Override
    public String getFullName() {
        return String.format("%1$s (%2$s)", mName, mVendor);
    }

    @Override
    public String getClasspathName() {
        return String.format("%1$s [%2$s]", mName, mBasePlatform.getClasspathName());
    }

    @Override
    public String getShortClasspathName() {
        return String.format("%1$s [%2$s]", mName, mBasePlatform.getVersionName());
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @NonNull
    @Override
    public AndroidVersion getVersion() {
        // this is always defined by the base platform
        return mBasePlatform.getVersion();
    }

    @Override
    public String getVersionName() {
        return mBasePlatform.getVersionName();
    }

    @Override
    public int getRevision() {
        return mRevision;
    }

    @Override
    public boolean isPlatform() {
        return false;
    }

    @Override
    public IAndroidTarget getParent() {
        return mBasePlatform;
    }

    @Override
    public String getPath(int pathId) {
        switch (pathId) {
            case SKINS:
                return mLocation + SdkConstants.OS_SKINS_FOLDER;
            case DOCS:
                return mLocation + SdkConstants.FD_DOCS + File.separator
                        + SdkConstants.FD_DOCS_REFERENCE;

            case LAYOUT_LIB:
                if (mHasRenderingLibrary) {
                    return mLocation + SdkConstants.FD_DATA + File.separator
                            + SdkConstants.FN_LAYOUTLIB_JAR;
                }
                return mBasePlatform.getPath(pathId);

            case RESOURCES:
                if (mHasRenderingResources) {
                    return mLocation + SdkConstants.FD_DATA + File.separator
                            + SdkConstants.FD_RES;
                }
                return mBasePlatform.getPath(pathId);

            case FONTS:
                if (mHasRenderingResources) {
                    return mLocation + SdkConstants.FD_DATA + File.separator
                            + SdkConstants.FD_FONTS;
                }
                return mBasePlatform.getPath(pathId);

            case SAMPLES:
                // only return the add-on samples folder if there is actually a sample (or more)
                File sampleLoc = new File(mLocation, SdkConstants.FD_SAMPLES);
                if (sampleLoc.isDirectory()) {
                    File[] files = sampleLoc.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.isDirectory();
                        }

                    });
                    if (files != null && files.length > 0) {
                        return sampleLoc.getAbsolutePath();
                    }
                }
                //$FALL-THROUGH$
            default :
                return mBasePlatform.getPath(pathId);
        }
    }

    @Override
    public File getFile(int pathId) {
        return new File(getPath(pathId));
    }

    @Override
    public BuildToolInfo getBuildToolInfo() {
        return mBasePlatform.getBuildToolInfo();
    }

    @Override @NonNull
    public List<String> getBootClasspath() {
        return mBasePlatform.getBootClasspath();
    }

    @NonNull
    @Override
    public List<OptionalLibrary> getOptionalLibraries() {
        return mBasePlatform.getOptionalLibraries();
    }

    @NonNull
    @Override
    public List<OptionalLibrary> getAdditionalLibraries() {
        return mLibraries;
    }

    @Override
    public boolean hasRenderingLibrary() {
        return mHasRenderingLibrary || mHasRenderingResources;
    }

    @NonNull
    @Override
    public File[] getSkins() {
        return mSkins;
    }

    @Nullable
    @Override
    public File getDefaultSkin() {
        return mDefaultSkin;
    }

    /**
     * Returns the list of libraries of the underlying platform.
     *
     * {@inheritDoc}
     */
    @Override
    public String[] getPlatformLibraries() {
        return mBasePlatform.getPlatformLibraries();
    }

    @Override
    public String getProperty(String name) {
        return mBasePlatform.getProperty(name);
    }

    @Override
    public Integer getProperty(String name, Integer defaultValue) {
        return mBasePlatform.getProperty(name, defaultValue);
    }

    @Override
    public Boolean getProperty(String name, Boolean defaultValue) {
        return mBasePlatform.getProperty(name, defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        return mBasePlatform.getProperties();
    }

    @Override
    public int getUsbVendorId() {
        return mVendorId;
    }

    @Override
    public boolean canRunOn(IAndroidTarget target) {
        // basic test
        if (target == this) {
            return true;
        }

        /*
         * The method javadoc indicates:
         * Returns whether the given target is compatible with the receiver.
         * <p/>A target is considered compatible if applications developed for the receiver can
         * run on the given target.
         */

        // The receiver is an add-on. There are 2 big use cases: The add-on has libraries
        // or the add-on doesn't (in which case we consider it a platform).
        if (mLibraries.isEmpty()) {
            return mBasePlatform.canRunOn(target);
        } else {
            // the only targets that can run the receiver are the same add-on in the same or later
            // versions.
            // first check: vendor/name
            if (!mVendor.equals(target.getVendor()) || !mName.equals(target.getName())) {
                return false;
            }

            // now check the version. At this point since we checked the add-on part,
            // we can revert to the basic check on version/codename which are done by the
            // base platform already.
            return mBasePlatform.canRunOn(target);
        }

    }

    @Override
    public String hashString() {
        return String.format(AndroidTargetHash.ADD_ON_FORMAT, mVendor, mName,
                mBasePlatform.getVersion().getApiString());
    }

    @Override
    public int hashCode() {
        return hashString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddOnTarget) {
            AddOnTarget addon = (AddOnTarget)obj;

            return mVendor.equals(addon.mVendor) && mName.equals(addon.mName) &&
                mBasePlatform.getVersion().equals(addon.mBasePlatform.getVersion());
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
    public int compareTo(@NonNull IAndroidTarget target) {
        // quick check.
        if (this == target) {
            return 0;
        }

        int versionDiff = getVersion().compareTo(target.getVersion());

        // only if the version are the same do we care about platform/add-ons.
        if (versionDiff == 0) {
            // platforms go before add-ons.
            if (target.isPlatform()) {
                return +1;
            } else {
                AddOnTarget targetAddOn = (AddOnTarget)target;

                // both are add-ons of the same version. Compare per vendor then by name
                int vendorDiff = mVendor.compareTo(targetAddOn.mVendor);
                if (vendorDiff == 0) {
                    return mName.compareTo(targetAddOn.mName);
                } else {
                    return vendorDiff;
                }
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
        return String.format("AddonTarget %1$s rev %2$d (based on %3$s)",     //$NON-NLS-1$
                getVersion(),
                getRevision(),
                getParent().toString());
    }

    // ---- local methods.

    public void setSkins(@NonNull File[] skins, @NonNull File defaultSkin) {
        mDefaultSkin = defaultSkin;

        // we mix the add-on and base platform skins
        HashSet<File> skinSet = new HashSet<File>();
        skinSet.addAll(Arrays.asList(skins));
        skinSet.addAll(Arrays.asList(mBasePlatform.getSkins()));

        mSkins = skinSet.toArray(new File[skinSet.size()]);
        Arrays.sort(mSkins);
    }

    /**
     * Sets the USB vendor id in the add-on.
     */
    public void setUsbVendorId(int vendorId) {
        if (vendorId == 0) {
            throw new IllegalArgumentException( "VendorId must be > 0");
        }

        mVendorId = vendorId;
    }
}
