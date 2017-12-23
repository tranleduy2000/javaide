/*
 * Copyright (C) 2011 The Android Open Source Project
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

import com.android.sdklib.io.FileOp;

import java.io.File;


/**
 * Describes a system image as used by an {@link IAndroidTarget}.
 * A system image has an installation path, a location type and an ABI type.
 */
public class SystemImage implements ISystemImage {

    public static final String ANDROID_PREFIX = "android-";     //$NON-NLS-1$

    private final LocationType mLocationtype;
    private final String mAbiType;
    private final File mLocation;

    /**
     * Creates a {@link SystemImage} description for an existing system image folder.
     *
     * @param location The location of an installed system image.
     * @param locationType Where the system image folder is located for this ABI.
     * @param abiType The ABI type. For example, one of {@link SdkConstants#ABI_ARMEABI},
     *          {@link SdkConstants#ABI_ARMEABI_V7A} or  {@link SdkConstants#ABI_INTEL_ATOM}.
     */
    public SystemImage(File location, LocationType locationType, String abiType) {
        mLocation = location;
        mLocationtype = locationType;
        mAbiType = abiType;
    }

    /**
     * Creates a {@link SystemImage} description for a non-existing system image folder.
     * The actual location is computed based on the {@code locationtype}.
     *
     * @param sdkManager The current SDK manager.
     * @param locationType Where the system image folder is located for this ABI.
     * @param abiType The ABI type. For example, one of {@link SdkConstants#ABI_ARMEABI},
     *          {@link SdkConstants#ABI_ARMEABI_V7A} or  {@link SdkConstants#ABI_INTEL_ATOM}.
     * @throws IllegalArgumentException if the {@code target} used for
     *         {@link LocationType#IN_SYSTEM_IMAGE} is not a {@link PlatformTarget}.
     */
    public SystemImage(
            SdkManager sdkManager,
            IAndroidTarget target,
            LocationType locationType,
            String abiType) {
        mLocationtype = locationType;
        mAbiType = abiType;

        File location = null;
        switch(locationType) {
        case IN_PLATFORM_LEGACY:
            location = new File(target.getLocation(), SdkConstants.OS_IMAGES_FOLDER);
            break;

        case IN_PLATFORM_SUBFOLDER:
            location = FileOp.append(target.getLocation(), SdkConstants.OS_IMAGES_FOLDER, abiType);
            break;

        case IN_SYSTEM_IMAGE:
            if (!target.isPlatform()) {
                throw new IllegalArgumentException(
                        "Add-ons do not support the system-image location type"); //$NON-NLS-1$
            }

            location = getCanonicalFolder(sdkManager.getLocation(), target.getVersion(), abiType);
            break;
        default:
            // This is not supposed to happen unless LocationType is
            // extended without adjusting this code.
            assert false : "SystemImage used with an incorrect locationType";       //$NON-NLS-1$
        }
        mLocation = location;
    }

    /**
     * Static helper method that returns the canonical path for a system-image that uses
     * the {@link LocationType#IN_SYSTEM_IMAGE} location type.
     * <p/>
     * Such an image is located in {@code SDK/system-images/android-N/abiType}.
     * For this reason this method requires the root SDK as well as the platform and the ABI type.
     *
     * @param sdkOsPath The OS path to the SDK.
     * @param platformVersion The platform version.
     * @param abiType An optional ABI type. If null, the parent directory is returned.
     * @return A file that represents the location of the canonical system-image folder
     *         for this configuration.
     */
    public static File getCanonicalFolder(
            String sdkOsPath,
            AndroidVersion platformVersion,
            String abiType) {
        File root = FileOp.append(
                sdkOsPath,
                SdkConstants.FD_SYSTEM_IMAGES,
                ANDROID_PREFIX + platformVersion.getApiString());
        if (abiType == null) {
            return root;
        } else {
            return FileOp.append(root, abiType);
        }
    }

    /** Returns the actual location of an installed system image. */
    public File getLocation() {
        return mLocation;
    }

    /** Indicates the location strategy for this system image in the SDK. */
    public LocationType getLocationType() {
        return mLocationtype;
    }

    /**
     * Returns the ABI type. For example, one of {@link SdkConstants#ABI_ARMEABI},
     * {@link SdkConstants#ABI_ARMEABI_V7A} or  {@link SdkConstants#ABI_INTEL_ATOM}.
     * Cannot be null nor empty.
     */
    public String getAbiType() {
        return mAbiType;
    }

    public int compareTo(ISystemImage other) {
        // Sort by ABI name only. This is what matters from a user point of view.
        return this.getAbiType().compareToIgnoreCase(other.getAbiType());
    }

    /**
     * Generates a string representation suitable for debug purposes.
     * The string is not intended to be displayed to the user.
     *
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("SystemImage ABI=%s, location %s='%s'",           //$NON-NLS-1$
                mAbiType,
                mLocationtype.toString().replace('_', ' ').toLowerCase(),
                mLocation
                );
    }


}
