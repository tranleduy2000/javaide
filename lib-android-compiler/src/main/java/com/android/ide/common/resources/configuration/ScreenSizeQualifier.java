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

import com.android.resources.ResourceEnum;
import com.android.resources.ScreenSize;

/**
 * Resource Qualifier for Screen Size. Size can be "small", "normal", "large" and "x-large"
 */
public class ScreenSizeQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "Screen Size";

    private ScreenSize mValue = null;


    public ScreenSizeQualifier() {
    }

    public ScreenSizeQualifier(ScreenSize value) {
        mValue = value;
    }

    public ScreenSize getValue() {
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
        return "Size";
    }

    @Override
    public int since() {
        return 4;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        ScreenSize size = ScreenSize.getEnum(value);
        if (size != null) {
            ScreenSizeQualifier qualifier = new ScreenSizeQualifier(size);
            config.setScreenSizeQualifier(qualifier);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMatchFor(ResourceQualifier qualifier) {
        // This is a match only if the screen size is smaller than the qualifier's screen size.
        if (qualifier instanceof ScreenSizeQualifier) {
            int qualifierIndex = ScreenSize.getIndex(((ScreenSizeQualifier) qualifier).mValue);
            int index = ScreenSize.getIndex(mValue);
            if (index <= qualifierIndex) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        if (compareTo == null) {
            return true;
        }

        ScreenSizeQualifier compareQ = (ScreenSizeQualifier) compareTo;
        int thisIndex = ScreenSize.getIndex(mValue);
        int compareIndex = ScreenSize.getIndex(compareQ.mValue);

        // Return true if this size is larger than reference size. Since isMatchFor() is called
        // before, it is guaranteed that the size will not be larger than the reference.
        return thisIndex > compareIndex;
    }
}
