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

package com.android.sdklib.internal.repository.packages;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * License text, with an optional license XML reference.
 */
public class License {
    private final String mLicense;
    private final String mLicenseRef;

    public License(@NonNull String license) {
        mLicense = license;
        mLicenseRef = null;
    }

    public License(@NonNull String license, @Nullable String licenseRef) {
        mLicense = license;
        mLicenseRef = licenseRef;
    }

    /** Returns the license text. Never null. */
    @NonNull
    public String getLicense() {
        return mLicense;
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
}

