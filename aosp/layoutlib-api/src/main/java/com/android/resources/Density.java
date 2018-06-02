/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.resources;


/**
 * Density enum.
 * <p/>This is used in the manifest in the uses-configuration node and in the resource folder names
 * as well as other places needing to know the density values.
 */
public enum Density implements ResourceEnum {
    XXXHIGH("xxxhdpi", "XXX-High Density", 640, 18), //$NON-NLS-1$
    DPI_560("560dpi",  "560 DPI Density",  560,  1), //$NON-NLS-1$
    XXHIGH( "xxhdpi",  "XX-High Density",  480, 16), //$NON-NLS-1$
    DPI_400("400dpi",  "400 DPI Density",  400,  1), //$NON-NLS-1$
    DPI_360("360dpi",  "360 DPI Density",  360, 23), //$NON-NLS-1$
    XHIGH(  "xhdpi",   "X-High Density",   320,  8), //$NON-NLS-1$
    DPI_280("280dpi",  "280 DPI Density",  280, 22), //$NON-NLS-1$
    HIGH(   "hdpi",    "High Density",     240,  4), //$NON-NLS-1$
    TV(     "tvdpi",   "TV Density",       213, 13), //$NON-NLS-1$
    MEDIUM( "mdpi",    "Medium Density",   160,  4), //$NON-NLS-1$
    LOW(    "ldpi",    "Low Density",      120,  4), //$NON-NLS-1$
    ANYDPI( "anydpi",  "Any Density",        0, 21), //$NON-NLS-1$
    NODPI(  "nodpi",   "No Density",         0,  4); //$NON-NLS-1$

    public static final int DEFAULT_DENSITY = 160;

    private final String mValue;
    private final String mDisplayValue;
    private final int mDensity;
    private final int mSince;

    Density(String value, String displayValue, int density, int since) {
        mValue = value;
        mDisplayValue = displayValue;
        mDensity = density;
        mSince = since;
    }

    /**
     * Returns the enum matching the provided qualifier value.
     * @param value The qualifier value.
     * @return the enum for the qualifier value or null if no match was found.
     */
    public static Density getEnum(String value) {
        for (Density orient : values()) {
            if (orient.mValue.equals(value)) {
                return orient;
            }
        }

        return null;
    }

    /**
     * Returns the enum matching the given density value
     * @param value The density value.
     * @return the enum for the density value or null if no match was found.
     */
    public static Density getEnum(int value) {
        for (Density d : values()) {
            if (d.mDensity == value) {
                return d;
            }
        }

        return null;
    }

    @Override
    public String getResourceValue() {
        return mValue;
    }

    public int getDpiValue() {
        return mDensity;
    }

    public int since() {
        return mSince;
    }

    public String getLegacyValue() {
        if (this != NODPI && this != ANYDPI) {
            return String.format("%1$ddpi", getDpiValue());
        }

        return "";
    }

    @Override
    public String getShortDisplayValue() {
        return mDisplayValue;
    }

    @Override
    public String getLongDisplayValue() {
        return mDisplayValue;
    }

    public static int getIndex(Density value) {
        int i = 0;
        for (Density input : values()) {
            if (value == input) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public static Density getByIndex(int index) {
        Density[] values = values();
        if (index >=0 && index < values.length) {
            return values[index];
        }
        return null;
    }

    /**
     * Returns true if this density is relevant for app developers (e.g.
     * a density you should consider providing resources for)
     */
    public boolean isRecommended() {
        switch (this) {
            case TV:
            case DPI_280:
            case DPI_360:
            case DPI_400:
            case DPI_560:
                return false;
            default:
                return true;
        }
    }

    @Override
    public boolean isFakeValue() {
        return false;
    }

    @Override
    public boolean isValidValueForDevice() {
        return this != NODPI && this != ANYDPI; // nodpi/anydpi is not a valid config for devices.
    }
}
