/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource Qualifier for Region.
 */
public final class RegionQualifier extends ResourceQualifier {
    private static final Pattern sRegionPattern = Pattern.compile("^r([a-zA-Z]{2})$"); //$NON-NLS-1$

    public static final String FAKE_REGION_VALUE = "__"; //$NON-NLS-1$
    public static final String NAME = "Region";

    private String mValue;

    /**
     * Creates and returns a qualifier from the given folder segment. If the segment is incorrect,
     * <code>null</code> is returned.
     * @param segment the folder segment from which to create a qualifier.
     * @return a new {@link RegionQualifier} object or <code>null</code>
     */
    public static RegionQualifier getQualifier(String segment) {
        Matcher m = sRegionPattern.matcher(segment);
        if (m.matches()) {
            RegionQualifier qualifier = new RegionQualifier();
            assert m.group(1).length() == 2;
            qualifier.mValue = new String(new char[] {
                    Character.toUpperCase(segment.charAt(1)),
                    Character.toUpperCase(segment.charAt(2))
            });

            return qualifier;
        }
        return null;
    }

    /**
     * Returns the folder name segment for the given value. This is equivalent to calling
     * {@link #toString()} on a {@link RegionQualifier} object.
     * @param value the value of the qualifier, as returned by {@link #getValue()}.
     */
    public static String getFolderSegment(String value) {
        if (value != null) {
            // See http://developer.android.com/reference/java/util/Locale.html#default_locale
            String segment = "r" + value.toUpperCase(Locale.US); //$NON-NLS-1$
            if (sRegionPattern.matcher(segment).matches()) {
                return segment;
            }
        }

        return "";  //$NON-NLS-1$
    }

    public RegionQualifier() {

    }

    public RegionQualifier(String value) {
        mValue = value.toUpperCase(Locale.US);
    }

    public String getValue() {
        if (mValue != null) {
            return mValue;
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return NAME;
    }

    @Override
    public int since() {
        return 1;
    }

    @Override
    public boolean isValid() {
        return mValue != null;
    }

    @Override
    public boolean hasFakeValue() {
        return FAKE_REGION_VALUE.equals(mValue);
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        if (value.length() != 3) {
            return false;
        }
        RegionQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setRegionQualifier(qualifier);
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof RegionQualifier) {
            if (mValue == null) {
                return ((RegionQualifier)qualifier).mValue == null;
            }
            return mValue.equals(((RegionQualifier)qualifier).mValue);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (mValue != null) {
            return mValue.hashCode();
        }

        return 0;
    }

    /**
     * Returns the string used to represent this qualifier in the folder name.
     */
    @Override
    public String getFolderSegment() {
        return getFolderSegment(mValue);
    }

    @Override
    public String getShortDisplayValue() {
        if (mValue != null) {
            return mValue;
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public String getLongDisplayValue() {
        if (mValue != null) {
            return String.format("Region %s", mValue);
        }

        return ""; //$NON-NLS-1$
    }
}
