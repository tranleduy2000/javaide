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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.devices.Abi;
import com.android.sdklib.repository.descriptors.IdDisplay;

import java.io.File;


/**
 * Describes a system image as used by an {@link IAndroidTarget}.
 * A system image has an installation path, a location type and an ABI type.
 */
public interface ISystemImage extends Comparable<ISystemImage> {

    /** Indicates the type of location for the system image folder in the SDK. */
    enum LocationType {
        /**
         * The system image is located in the legacy platform's {@link SdkConstants#FD_IMAGES}
         * folder.
         * <p/>
         * Used by both platform and add-ons.
         */
        IN_LEGACY_FOLDER,

        /**
         * The system image is located in a sub-directory of the platform's
         * {@link SdkConstants#FD_IMAGES} folder, allowing for multiple system
         * images within the platform.
         * <p/>
         * Used by both platform and add-ons.
         */
        IN_IMAGES_SUBFOLDER,

        /**
         * The system image is located in the new SDK's {@link SdkConstants#FD_SYSTEM_IMAGES}
         * folder. Supported as of Tools R14 and Repository XSD version 5.
         * <p/>
         * Used <em>only</em> by both platform up to Tools R22.6.
         * Supported for add-ons as of Tools R22.8.
         */
        IN_SYSTEM_IMAGE,
    }

    /** Returns the actual location of an installed system image. */
    @NonNull
    File getLocation();

    /** Indicates the location strategy for this system image in the SDK. */
    @NonNull
    LocationType getLocationType();

    /** Returns the tag of the system image. */
    @NonNull
    IdDisplay getTag();

    /** Returns the vendor for an add-on's system image, or null for a platform system-image. */
    @Nullable
    IdDisplay getAddonVendor();

    /**
     * Returns the ABI type.
     * See {@link Abi} for a full list.
     * Cannot be null nor empty.
     */
    @NonNull
    String getAbiType();

    /**
     * Returns the skins embedded in the system image. <br/>
     * Only supported by system images using {@link LocationType#IN_SYSTEM_IMAGE}. <br/>
     * The skins listed here are merged in the {@link IAndroidTarget#getSkins()} list.
     * @return A non-null skin list, possibly empty.
     */
    @NonNull
    File[] getSkins();
}
