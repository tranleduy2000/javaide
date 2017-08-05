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

import java.io.File;


/**
 * Describes a system image as used by an {@link IAndroidTarget}.
 * A system image has an installation path, a location type and an ABI type.
 */
public interface ISystemImage extends Comparable<ISystemImage> {

    /** Indicates the type of location for the system image folder in the SDK. */
    public enum LocationType {
        /**
         * The system image is located in the legacy platform's {@link SdkConstants#FD_IMAGES}
         * folder.
         * <p/>
         * Used by both platform and add-ons.
         */
        IN_PLATFORM_LEGACY,

        /**
         * The system image is located in a sub-directory of the platform's
         * {@link SdkConstants#FD_IMAGES} folder, allowing for multiple system
         * images within the platform.
         * <p/>
         * Used by both platform and add-ons.
         */
        IN_PLATFORM_SUBFOLDER,

        /**
         * The system image is located in the new SDK's {@link SdkConstants#FD_SYSTEM_IMAGES}
         * folder. Supported as of Tools R14 and Repository XSD version 5.
         * <p/>
         * Used <em>only</em> by both platform. This is not supported for add-ons yet.
         */
        IN_SYSTEM_IMAGE,
    }

    /** Returns the actual location of an installed system image. */
    public abstract File getLocation();

    /** Indicates the location strategy for this system image in the SDK. */
    public abstract LocationType getLocationType();

    /**
     * Returns the ABI type. For example, one of {@link SdkConstants#ABI_ARMEABI},
     * {@link SdkConstants#ABI_ARMEABI_V7A} or  {@link SdkConstants#ABI_INTEL_ATOM}.
     * Cannot be null nor empty.
     */
    public abstract String getAbiType();
}
