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

import com.android.annotations.Nullable;
import com.android.resources.ResourceEnum;

/**
 * Base class for {@link ResourceQualifier} whose value is an {@link ResourceEnum}.
 *
 */
abstract class EnumBasedResourceQualifier extends ResourceQualifier {

    @Nullable
    abstract ResourceEnum getEnumValue();

    @Override
    public boolean isValid() {
        return getEnumValue() != null;
    }

    @Override
    public boolean hasFakeValue() {
        ResourceEnum value = getEnumValue();
        return value != null && value.isFakeValue();

    }

    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof EnumBasedResourceQualifier) {
            return getEnumValue() == ((EnumBasedResourceQualifier)qualifier).getEnumValue();
        }

        return false;
    }

    @Override
    public int hashCode() {
        ResourceEnum value = getEnumValue();
        if (value != null) {
            return value.hashCode();
        }

        return 0;
    }

    /**
     * Returns the string used to represent this qualifier in the folder name.
     */
    @Override
    public final String getFolderSegment() {
        ResourceEnum value = getEnumValue();
        if (value != null) {
            return value.getResourceValue();
        }

        return ""; //$NON-NLS-1$
    }


    @Override
    public String getShortDisplayValue() {
        ResourceEnum value = getEnumValue();
        if (value != null) {
            return value.getShortDisplayValue();
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public String getLongDisplayValue() {
        ResourceEnum value = getEnumValue();
        if (value != null) {
            return value.getLongDisplayValue();
        }

        return ""; //$NON-NLS-1$
    }

}
