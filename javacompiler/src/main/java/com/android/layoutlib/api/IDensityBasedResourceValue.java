/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.layoutlib.api;

import com.android.ide.common.rendering.api.DensityBasedResourceValue;

/**
 * Represents an Android Resources that has a density info attached to it.
 * @deprecated use {@link DensityBasedResourceValue}.
 */
@Deprecated
public interface IDensityBasedResourceValue extends IResourceValue {

    /**
     * Density.
     *
     * @deprecated use {@link com.android.resources.Density}.
     */
    @Deprecated
    public static enum Density {
        XHIGH(320),
        HIGH(240),
        MEDIUM(160),
        LOW(120),
        NODPI(0);

        private final int mValue;

        Density(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        /**
         * Returns the enum matching the given density value
         * @param value The density value.
         * @return the enum for the density value or null if no match was found.
         */
        public static Density getEnum(int value) {
            for (Density d : values()) {
                if (d.mValue == value) {
                    return d;
                }
            }

            return null;
        }
    }

    /**
     * Returns the density associated to the resource.
     * @deprecated use {@link DensityBasedResourceValue#getResourceDensity()}
     */
    @Deprecated
    Density getDensity();
}
