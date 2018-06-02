/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.resources.LayoutDirection;
import com.android.resources.ResourceEnum;

/**
 * Resource Qualifier for layout direction. values can be "ltr", or "rtl"
 */
public class LayoutDirectionQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "Layout Direction";

    private LayoutDirection mValue = null;


    public LayoutDirectionQualifier() {
    }

    public LayoutDirectionQualifier(LayoutDirection value) {
        mValue = value;
    }

    public LayoutDirection getValue() {
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
        return 17;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        LayoutDirection ld = LayoutDirection.getEnum(value);
        if (ld != null) {
            LayoutDirectionQualifier qualifier = new LayoutDirectionQualifier(ld);
            config.setLayoutDirectionQualifier(qualifier);
            return true;
        }

        return false;
    }
}
