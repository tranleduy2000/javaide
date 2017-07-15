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
 * Screen size enum.
 * <p/>This is used in the manifest in the uses-configuration node and in the resource folder names.
 */
public enum ScreenSize implements ResourceEnum {
    SMALL("small", "Small", "Small Screen"), //$NON-NLS-1$
    NORMAL("normal", "Normal", "Normal Screen"), //$NON-NLS-1$
    LARGE("large", "Large", "Large Screen"); //$NON-NLS-1$

    private final String mValue;
    private final String mShortDisplayValue;
    private final String mLongDisplayValue;

    private ScreenSize(String value, String shortDisplayValue, String longDisplayValue) {
        mValue = value;
        mShortDisplayValue = shortDisplayValue;
        mLongDisplayValue = longDisplayValue;
    }

    /**
     * Returns the enum for matching the provided qualifier value.
     * @param value The qualifier value.
     * @return the enum for the qualifier value or null if no matching was found.
     */
    public static ScreenSize getEnum(String value) {
        for (ScreenSize orient : values()) {
            if (orient.mValue.equals(value)) {
                return orient;
            }
        }

        return null;
    }

    public String getResourceValue() {
        return mValue;
    }

    public String getShortDisplayValue() {
        return mShortDisplayValue;
    }

    public String getLongDisplayValue() {
        return mLongDisplayValue;
    }

    public static int getIndex(ScreenSize orientation) {
        int i = 0;
        for (ScreenSize orient : values()) {
            if (orient == orientation) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public static ScreenSize getByIndex(int index) {
        int i = 0;
        for (ScreenSize orient : values()) {
            if (i == index) {
                return orient;
            }
            i++;
        }

        return null;
    }

    public boolean isFakeValue() {
        return false;
    }

    public boolean isValidValueForDevice() {
        return true;
    }

}
