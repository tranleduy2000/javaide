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

import com.android.resources.NightMode;
import com.android.resources.ResourceEnum;

/**
 * Resource Qualifier for Navigation Method.
 */
public final class NightModeQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "Night Mode";

    private NightMode mValue;

    public NightModeQualifier() {
        // pass
    }

    public NightModeQualifier(NightMode value) {
        mValue = value;
    }

    public NightMode getValue() {
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
        return "Night Mode";
    }

    @Override
    public int since() {
        return 8;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        NightMode mode = NightMode.getEnum(value);
        if (mode != null) {
            NightModeQualifier qualifier = new NightModeQualifier(mode);
            config.setNightModeQualifier(qualifier);
            return true;
        }

        return false;
    }
}
