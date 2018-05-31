/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.ide.common.rendering.api;

import com.android.resources.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an android plurals resource
 */
public class PluralsResourceValue extends ResourceValue {

    private final List<String> mQuantities = new ArrayList<String>();

    private final List<String> mValues = new ArrayList<String>();

    /**
     * Constructs a new {@linkplain PluralsResourceValue}
     *
     * @param name        the name of the array
     * @param isFramework whether this is a framework resource
     */
    public PluralsResourceValue(String name, boolean isFramework) {
        super(ResourceType.PLURALS, name, isFramework);
    }

    /**
     * Adds an element into the array
     */
    public void addPlural(String quantity, String value) {
        mQuantities.add(quantity);
        mValues.add(value);
    }

    /**
     * Returns the number of plural string
     *
     * @return the element count
     */
    public int getPluralsCount() {
        return mQuantities.size();
    }

    /**
     * Returns the quantity at the given index, such as "one", "two", "few", etc.
     *
     * @param index the index, which must be in the range [0..getPluralsCount()].
     * @return the corresponding quantity string
     */
    public String getQuantity(int index) {
        return mQuantities.get(index);
    }

    /**
     * Returns the string element at the given index position.
     *
     * @param index index, which must be in the range [0..getPluralsCount()].
     * @return the corresponding element
     */
    public String getValue(int index) {
        return mValues.get(index);
    }

    /**
     * Returns the string element for the given quantity
     *
     * @param quantity the quantity string, such as "one", "two", "few", etc.
     * @return the corresponding string value, or null if not defined
     */
    public String getValue(String quantity) {
        assert mQuantities.size() == mValues.size();
        for (int i = 0, n = mQuantities.size(); i < n; i++) {
            if (quantity.equals(mQuantities.get(i))) {
                return mValues.get(i);
            }
        }

        return null;
    }

    @Override
    public String getValue() {
        // Clients should normally not call this method on PluralsResourceValues; they should
        // pick the specific quantity element they want. However, for compatibility with older
        // layout libs, return the first plurals element's value instead.

        //noinspection VariableNotUsedInsideIf
        if (mValue == null) {
            if (!mValues.isEmpty()) {
                return getValue(0);
            }

        }

        return mValue;
    }
}
