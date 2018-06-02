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

package com.android.sdklib;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.google.common.base.Splitter;

import java.util.List;


/**
 * Helper methods to manipulate hash strings used by {@link IAndroidTarget#hashString()}.
 */
public abstract class AndroidTargetHash {

    /**
     * Prefix used to build hash strings for platform targets
     * @see SdkManager#getTargetFromHashString(String)
     */
    public static final String PLATFORM_HASH_PREFIX = "android-";

    /**
     * String to compute hash for add-on targets. <br/>
     * Format is {@code vendor:name:apiVersion}. <br/>
     *
     * <em>Important<em/>: the vendor and name compontents are the display strings, not the
     * newer id strings.
     */
    public static final String ADD_ON_FORMAT = "%s:%s:%s"; //$NON-NLS-1$

    /**
     * String used to get a hash to the platform target.
     * This format is compatible with the PlatformPackage.installId().
     */
    static final String PLATFORM_HASH = PLATFORM_HASH_PREFIX + "%s";

    /**
     * Returns the hash string for a given platform version.
     *
     * @param version A non-null platform version.
     * @return A non-null hash string uniquely representing this platform target.
     */
    @NonNull
    public static String getPlatformHashString(@NonNull AndroidVersion version) {
        return String.format(AndroidTargetHash.PLATFORM_HASH, version.getApiString());
    }

    /**
     * Returns the {@link AndroidVersion} for the given hash string,
     * if it represents a platform. If the hash string represents a preview platform,
     * the returned {@link AndroidVersion} will have an unknown API level (set to 1
     * or a known matching API level.)
     *
     * @param hashString the hash string (e.g. "android-19" or "android-CUPCAKE")
     *          or a pure API level for convenience (e.g. "19" instead of the proper "android-19")
     * @return a platform, or null
     */
    @Nullable
    public static AndroidVersion getPlatformVersion(@NonNull String hashString) {
        if (hashString.startsWith(PLATFORM_HASH_PREFIX)) {
            String suffix = hashString.substring(PLATFORM_HASH_PREFIX.length());
            if (!suffix.isEmpty()) {
                if (Character.isDigit(suffix.charAt(0))) {
                    try {
                        int api = Integer.parseInt(suffix);
                        return new AndroidVersion(api, null);
                    } catch (NumberFormatException ignore) {}
                } else {
                    int api = SdkVersionInfo.getApiByBuildCode(suffix, false);
                    if (api < 1) {
                        api = 1;
                    }
                    return new AndroidVersion(api, suffix);
                }
            }
        } else if (!hashString.isEmpty() && Character.isDigit(hashString.charAt(0))) {
            // For convenience, interpret a single integer as the proper "android-NN" form.
            try {
                int api = Integer.parseInt(hashString);
                return new AndroidVersion(api, null);
            } catch (NumberFormatException ignore) {}
        }

        return null;
    }

    @Nullable
    public static AndroidVersion getAddOnVersion(@NonNull String hashString) {
        List<String> parts = Splitter.on(':').splitToList(hashString);
        if (parts.size() != 3) {
            return null;
        }

        String apiLevelPart = parts.get(2);
        try {
            int apiLevel = Integer.parseInt(apiLevelPart);
            return new AndroidVersion(apiLevel, null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the API level from a hash string, either a platform version or add-on version.
     *
     * @see #getAddOnVersion(String)
     * @see #getPlatformVersion(String)
     */
    @Nullable
    public static AndroidVersion getVersionFromHash(@NonNull String hashString) {
        if (isPlatform(hashString)) {
            return getPlatformVersion(hashString);
        } else {
            return getAddOnVersion(hashString);
        }
    }

    /**
     * Returns the hash string for a given add-on.
     *
     * @param addonVendorDisplay A non-null vendor. When using an {@link IdDisplay} source,
     *                      this parameter should be the {@link IdDisplay#getDisplay()}.
     * @param addonNameDisplay A non-null add-on name. When using an {@link IdDisplay} source,
     *                      this parameter should be the {@link IdDisplay#getDisplay()}.
     * @param version A non-null platform version (the addon's base platform version)
     * @return A non-null hash string uniquely representing this add-on target.
     */
    public static String getAddonHashString(
            @NonNull String addonVendorDisplay,
            @NonNull String addonNameDisplay,
            @NonNull AndroidVersion version) {
        return String.format(ADD_ON_FORMAT,
                addonVendorDisplay,
                addonNameDisplay,
                version.getApiString());
    }

    /**
     * Returns the hash string for a given target (add-on or platform.)
     *
     * @param target A non-null target.
     * @return A non-null hash string uniquely representing this target.
     */
    public static String getTargetHashString(@NonNull IAndroidTarget target) {
        if (target.isPlatform()) {
            return getPlatformHashString(target.getVersion());
        } else {
            return getAddonHashString(
                    target.getVendor(),
                    target.getName(),
                    target.getVersion());
        }
    }

    /**
     * Given a hash string, indicates whether this is a platform hash string.
     * If not, it's an addon hash string.
     *
     * @param hashString The hash string to test.
     * @return True if this hash string starts by the platform prefix.
     */
    public static boolean isPlatform(@NonNull String hashString) {
        return hashString.startsWith(PLATFORM_HASH_PREFIX);
    }

}
