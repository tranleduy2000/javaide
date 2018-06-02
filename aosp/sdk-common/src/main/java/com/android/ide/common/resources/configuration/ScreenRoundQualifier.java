/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.annotations.Nullable;
import com.android.resources.ResourceEnum;
import com.android.resources.ScreenRound;

public class ScreenRoundQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "Screen Roundness";

    @Nullable
    private ScreenRound mValue = null;

    public ScreenRoundQualifier() {
    }

    public ScreenRoundQualifier(@Nullable ScreenRound value) {
        mValue = value;
    }

    public ScreenRound getValue() {
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
        return "Roundness";
    }

    @Override
    public int since() {
        return 23;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        ScreenRound roundness = ScreenRound.getEnum(value);
        if (roundness != null) {
            ScreenRoundQualifier qualifier = new ScreenRoundQualifier(roundness);
            config.setScreenRoundQualifier(qualifier);
            return true;
        }

        return false;
    }
}
