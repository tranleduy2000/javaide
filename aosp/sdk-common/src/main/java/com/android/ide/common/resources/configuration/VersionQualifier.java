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

package com.android.ide.common.resources.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource Qualifier for Platform Version.
 */
public final class VersionQualifier extends ResourceQualifier {
    /** Default version. This means the property is not set. */
    private static final int DEFAULT_VERSION = -1;

    private static final Pattern sVersionPattern = Pattern.compile("^v(\\d+)$");//$NON-NLS-1$

    private int mVersion = DEFAULT_VERSION;

    public static final String NAME = "Platform Version";

    /**
     * Creates and returns a qualifier from the given folder segment. If the segment is incorrect,
     * <code>null</code> is returned.
     * @param segment the folder segment from which to create a qualifier.
     * @return a new {@link VersionQualifier} object or <code>null</code>
     */
    public static VersionQualifier getQualifier(String segment) {
        Matcher m = sVersionPattern.matcher(segment);
        if (m.matches()) {
            String v = m.group(1);

            int code = -1;
            try {
                code = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                // looks like the string we extracted wasn't a valid number.
                return null;
            }

            VersionQualifier qualifier = new VersionQualifier();
            qualifier.mVersion = code;
            return qualifier;
        }

        return null;
    }

    /**
     * Returns the folder name segment for the given value. This is equivalent to calling
     * {@link #toString()} on a {@link VersionQualifier} object.
     * @param version the value of the qualifier, as returned by {@link #getVersion()}.
     */
    public static String getFolderSegment(int version) {
        if (version != DEFAULT_VERSION) {
            return String.format("v%1$d", version); //$NON-NLS-1$
        }

        return ""; //$NON-NLS-1$
    }

    public VersionQualifier(int apiLevel) {
        mVersion = apiLevel;
    }

    public VersionQualifier() {
        //pass
    }

    public int getVersion() {
        return mVersion;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return "Version";
    }

    @Override
    public int since() {
        return 1;
    }

    @Override
    public boolean isValid() {
        return mVersion != DEFAULT_VERSION;
    }

    @Override
    public boolean hasFakeValue() {
        return false;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        VersionQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setVersionQualifier(qualifier);
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof VersionQualifier) {
            return mVersion == ((VersionQualifier)qualifier).mVersion;
        }

        return false;
    }

    @Override
    public boolean isMatchFor(ResourceQualifier qualifier) {
        if (qualifier instanceof VersionQualifier) {
            // it is considered a match if our api level is equal or lower to the given qualifier,
            // or the given qualifier doesn't specify an API Level.
            return mVersion <= ((VersionQualifier) qualifier).mVersion
                    || ((VersionQualifier)qualifier).mVersion == -1;
        }

        return false;
    }

    @Override
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        if (compareTo == null) {
            return true;
        }

        VersionQualifier compareQ = (VersionQualifier)compareTo;
        VersionQualifier referenceQ = (VersionQualifier)reference;

        if (compareQ.mVersion == referenceQ.mVersion) {
            // what we have is already the best possible match (exact match)
            return false;
        } else if (mVersion == referenceQ.mVersion) {
            // got new exact value, this is the best!
            return true;
        } else {
            // in all case we're going to prefer the higher version (since they have been filtered
            // to not be too high
            return mVersion > compareQ.mVersion;
        }
    }

    @Override
    public int hashCode() {
        return mVersion;
    }

    /**
     * Returns the string used to represent this qualifier in the folder name.
     */
    @Override
    public String getFolderSegment() {
        return getFolderSegment(mVersion);
    }

    @Override
    public String getShortDisplayValue() {
        if (mVersion != DEFAULT_VERSION) {
            return String.format("API %1$d", mVersion);
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public String getLongDisplayValue() {
        if (mVersion != DEFAULT_VERSION) {
            return String.format("API Level %1$d", mVersion);
        }

        return ""; //$NON-NLS-1$
    }
}
