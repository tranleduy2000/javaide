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

import com.android.resources.NavigationState;
import com.android.resources.ResourceEnum;

/**
 * Resource Qualifier for navigation state.
 */
public final class NavigationStateQualifier extends EnumBasedResourceQualifier {

    public static final String NAME = "Navigation State";

    private NavigationState mValue = null;

    public NavigationStateQualifier() {
        // pass
    }

    public NavigationStateQualifier(NavigationState value) {
        mValue = value;
    }

    public NavigationState getValue() {
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
        return 1;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        NavigationState state = NavigationState.getEnum(value);
        if (state != null) {
            NavigationStateQualifier qualifier = new NavigationStateQualifier();
            qualifier.mValue = state;
            config.setNavigationStateQualifier(qualifier);
            return true;
        }

        return false;
    }
}
