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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Resource value representing a declare-styleable resource.
 *
 * {@link #getValue()} will return null, instead use {@link #getAttributeValues(String)} to
 * get the enum/flag value associated with an attribute defined in the declare-styleable.
 *
 * @deprecated This class is broken as it does not handle the namespace for each attribute.
 * Thankfully, newer versions of layoutlib don't actually use it, so we just keep it as is for
 * backward compatibility on older layoutlibs.
 *
 */
@Deprecated
public class DeclareStyleableResourceValue extends ResourceValue {

    private Map<String, AttrResourceValue> mAttrMap;

    public DeclareStyleableResourceValue(ResourceType type, String name, boolean isFramework) {
        super(type, name, isFramework);
    }

    /**
     * Return the enum/flag integer value for a given attribute.
     * @param name the name of the attribute
     * @return the map of (name, integer) values.
     */
    public Map<String, Integer> getAttributeValues(String name) {
        if (mAttrMap != null) {
            AttrResourceValue attr = mAttrMap.get(name);
            if (attr != null) {
                return attr.getAttributeValues();
            }
        }

        return null;
    }

    public Map<String, AttrResourceValue> getAllAttributes() {
        return mAttrMap;
    }

    public void addValue(AttrResourceValue attr) {
        if (mAttrMap == null) {
            // Preserve insertion order. This order affects the int[] indices for styleables.
            mAttrMap = new LinkedHashMap<String, AttrResourceValue>();
        }

        mAttrMap.put(attr.getName(), attr);
    }
}
