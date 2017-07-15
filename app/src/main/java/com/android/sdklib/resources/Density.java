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

package com.android.sdklib.resources;


/**
 * Density enum.
 * <p/>This is used in the manifest in the uses-configuration node and in the resource folder names
 * as well as other places needing to know the density values.
 */
public enum Density implements ResourceEnum {
    HIGH("hdpi", "High Density", 240), //$NON-NLS-1$
    MEDIUM("mdpi", "Medium Density", 160), //$NON-NLS-1$
    LOW("ldpi", "Low Density", 120), //$NON-NLS-1$
    NODPI("nodpi", "No Density", 0); //$NON-NLS-1$

    public final static int DEFAULT_DENSITY = 160;

    private final String mValue;
    private final String mDisplayValue;
    private final int mDensity;

    private Density(String value, String displayValue, int density) {
        mValue = value;
        mDisplayValue = displayValue;
        mDensity = density;
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

    public String getResourceValue() {
        return mValue;
    }

    public int getDpiValue() {
        return mDensity;
    }

    public String getLegacyValue() {
        if (this != NODPI) {
            return String.format("%1$ddpi", getDpiValue());
        }

        return "";
    }

    public String getShortDisplayValue() {
        return mDisplayValue;
    }

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
        int i = 0;
        for (Density value : values()) {
            if (i == index) {
                return value;
            }
            i++;
        }
        return null;
    }

    public boolean isFakeValue() {
        return false;
    }

    public boolean isValidValueForDevice() {
        return this != NODPI; // nodpi is not a valid config for devices.
    }
}
