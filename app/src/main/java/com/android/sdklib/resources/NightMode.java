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
 * Night enum.
 * <p/>This is used in the resource folder names.
 */
public enum NightMode implements ResourceEnum {
    NOTNIGHT("notnight", "Not Night", "Day time"),
    NIGHT("night", "Night", "Night time");

    private final String mValue;
    private final String mShortDisplayValue;
    private final String mLongDisplayValue;

    private NightMode(String value, String shortDisplayValue, String longDisplayValue) {
        mValue = value;
        mShortDisplayValue = shortDisplayValue;
        mLongDisplayValue = longDisplayValue;
    }

    /**
     * Returns the enum for matching the provided qualifier value.
     * @param value The qualifier value.
     * @return the enum for the qualifier value or null if no matching was found.
     */
    public static NightMode getEnum(String value) {
        for (NightMode mode : values()) {
            if (mode.mValue.equals(value)) {
                return mode;
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

    public static int getIndex(NightMode value) {
        int i = 0;
        for (NightMode mode : values()) {
            if (mode == value) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public static NightMode getByIndex(int index) {
        int i = 0;
        for (NightMode value : values()) {
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
        return true;
    }

}
