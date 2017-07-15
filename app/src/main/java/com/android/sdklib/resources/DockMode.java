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
 * Dock enum.
 * <p/>This is used in the resource folder names.
 */
public enum DockMode implements ResourceEnum {
    NONE("", "No Dock"),
    CAR("car", "Car Dock"),
    DESK("desk", "Desk Dock");

    private final String mValue;
    private final String mDisplayValue;

    private DockMode(String value, String display) {
        mValue = value;
        mDisplayValue = display;
    }

    /**
     * Returns the enum for matching the provided qualifier value.
     * @param value The qualifier value.
     * @return the enum for the qualifier value or null if no matching was found.
     */
    public static DockMode getEnum(String value) {
        for (DockMode mode : values()) {
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
        return mDisplayValue;
    }

    public String getLongDisplayValue() {
        return mDisplayValue;
    }

    public static int getIndex(DockMode value) {
        int i = 0;
        for (DockMode mode : values()) {
            if (mode == value) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public static DockMode getByIndex(int index) {
        int i = 0;
        for (DockMode value : values()) {
            if (i == index) {
                return value;
            }
            i++;
        }
        return null;
    }

    public boolean isFakeValue() {
        return this == NONE; // NONE is not a real enum. it's used for internal state only.
    }

    public boolean isValidValueForDevice() {
        return this != NONE;
    }
}
