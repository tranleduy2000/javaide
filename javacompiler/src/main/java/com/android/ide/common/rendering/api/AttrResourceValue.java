/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.HashMap;
import java.util.Map;

/**
 * A Resource value representing an attr resource.
 *
 * {@link #getValue()} will return null, instead use {@link #getAttributeValues()} to
 * get the enum/flag value associated with an attribute defined in the declare-styleable.
 *
 */
public class AttrResourceValue extends ResourceValue {

    private Map<String, Integer> mValueMap;


    public AttrResourceValue(ResourceType type, String name, boolean isFramework) {
        super(type, name, isFramework);
    }

    /**
     * Return the enum/flag integer values.
     *
     * @return the map of (name, integer) values. Can be null.
     */
    public Map<String, Integer> getAttributeValues() {
        return mValueMap;
    }

    public void addValue(String name, Integer value) {
        if (mValueMap == null) {
            mValueMap = new HashMap<String, Integer>();
        }

        mValueMap.put(name, value);
    }
}
