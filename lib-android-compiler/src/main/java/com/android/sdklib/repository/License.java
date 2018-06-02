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

package com.android.sdklib.repository;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.SdkManager;
import com.android.utils.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * License text, with an optional license XML reference.
 */
public class License {
    private final String mLicense;
    private final String mLicenseRef;
    private final String mLicenseHash;

    private static final String LICENSE_DIR = "licenses";

    public License(@NonNull String license, @Nullable String licenseRef) {
        mLicense = license;
        mLicenseRef = licenseRef;
        mLicenseHash = Hashing.sha1().hashBytes(mLicense.getBytes()).toString();
    }

    /** Returns the license text. Never null. */
    @NonNull
    public String getLicense() {
        return mLicense;
    }

    /** Returns the hash of the license text. Never null. */
    @NonNull
    public String getLicenseHash() {
        return mLicenseHash;
    }

    /**
     * Returns the license XML reference.
     * Could be null, e.g. in tests or synthetic packages
     * recreated from local source.properties.
     */
    @Nullable
    public String getLicenseRef() {
        return mLicenseRef;
    }

    /**
     * Returns a string representation of the license, useful for debugging.
     * This is not designed to be shown to the user.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<License ref:")
          .append(mLicenseRef)
          .append(", text:")
          .append(mLicense)
          .append(">");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mLicense == null) ? 0 : mLicense.hashCode());
        result = prime * result
                + ((mLicenseRef == null) ? 0 : mLicenseRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof License)) {
            return false;
        }
        License other = (License) obj;
        if (mLicense == null) {
            if (other.mLicense != null) {
                return false;
            }
        } else if (!mLicense.equals(other.mLicense)) {
            return false;
        }
        if (mLicenseRef == null) {
            if (other.mLicenseRef != null) {
                return false;
            }
        } else if (!mLicenseRef.equals(other.mLicenseRef)) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether this license has previously been accepted.
     * @param sdkRoot The root directory of the Android SDK
     * @return true if this license has already been accepted
     */
    public boolean checkAccepted(@Nullable File sdkRoot) {
        if (sdkRoot == null) {
            return false;
        }
        File licenseDir = new File(sdkRoot, LICENSE_DIR);
        File licenseFile = new File(licenseDir, mLicenseRef == null ? mLicenseHash : mLicenseRef);
        if (!licenseFile.exists()) {
            return false;
        }
        try {
            String hash = Files.readFirstLine(licenseFile, Charsets.UTF_8);
            return hash.equals(mLicenseHash);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Marks this license as accepted.
     *
     * @param sdkRoot The root directory of the Android SDK
     * @return true if the acceptance was persisted successfully.
     */
    public boolean setAccepted(@Nullable File sdkRoot) {
        if (sdkRoot == null) {
            return false;
        }
        if (checkAccepted(sdkRoot)) {
            return true;
        }
        File licenseDir = new File(sdkRoot, LICENSE_DIR);
        if (licenseDir.exists() && !licenseDir.isDirectory()) {
            return false;
        }
        if (!licenseDir.exists()) {
            licenseDir.mkdir();
        }
        File licenseFile = new File(licenseDir, mLicenseRef == null ? mLicenseHash : mLicenseRef);
        try {
            Files.write(mLicenseHash, licenseFile, Charsets.UTF_8);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }
}

