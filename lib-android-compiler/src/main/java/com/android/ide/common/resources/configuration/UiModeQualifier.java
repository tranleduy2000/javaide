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

package com.android.ide.common.resources.configuration;

import com.android.resources.ResourceEnum;
import com.android.resources.UiMode;

/**
 * Resource Qualifier for UI Mode.
 */
public final class UiModeQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "UI Mode";

    private UiMode mValue;

    public UiModeQualifier() {
        // pass
    }

    public UiModeQualifier(UiMode value) {
        mValue = value;
    }

    public UiMode getValue() {
        return mValue;
    }

    @Override
    ResourceEnum getEnumValue() {
        return mValue;
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
        if (mValue != null) {
            return mValue.since();
        }
        return 8;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        UiMode mode = UiMode.getEnum(value);
        if (mode != null) {
            UiModeQualifier qualifier = new UiModeQualifier(mode);
            config.setUiModeQualifier(qualifier);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMatchFor(ResourceQualifier qualifier) {
        // only normal is a match for all UI mode, because it's not an actual mode.
        if (mValue == UiMode.NORMAL) {
            return true;
        }

        // others must be an exact match
        return ((UiModeQualifier)qualifier).mValue == mValue;
    }

    @Override
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        if (compareTo == null) {
            return true;
        }

        UiModeQualifier compareQualifier = (UiModeQualifier)compareTo;
        UiModeQualifier referenceQualifier = (UiModeQualifier)reference;

        if (compareQualifier.getValue() == referenceQualifier.getValue()) {
            // what we have is already the best possible match (exact match)
            return false;
        } else  if (mValue == referenceQualifier.mValue) {
            // got new exact value, this is the best!
            return true;
        } else if (mValue == UiMode.NORMAL) {
            // else "normal" can be a match in case there's no exact match
            return true;
        }

        return false;
    }
}
