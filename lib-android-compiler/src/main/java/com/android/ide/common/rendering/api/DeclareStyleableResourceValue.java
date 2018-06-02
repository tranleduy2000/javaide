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

import com.android.annotations.NonNull;
import com.android.resources.ResourceType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Resource value representing a declare-styleable resource.
 *
 * {@link #getValue()} will return null, instead use {@link #getAllAttributes()} to
 * get the list of attributes defined in the declare-styleable.
 */
public class DeclareStyleableResourceValue extends ResourceValue {

    /** Used only for {@link #getAttributeValues(String)}, which is used only by old LayoutLibs. */
    @Deprecated
    private Map<String, AttrResourceValue> mAttrMap;

    @NonNull
    private List<AttrResourceValue> mAttrs = new ArrayList<AttrResourceValue>();

    public DeclareStyleableResourceValue(@NonNull ResourceType type, @NonNull String name,
            boolean isFramework) {
        super(type, name, isFramework);
        assert type == ResourceType.DECLARE_STYLEABLE;
    }

    /**
     * Return the enum/flag integer value for a given attribute.
     *
     * @param name the name of the attribute
     * @return the map of (name, integer) values.
     * @deprecated the method doesn't respect namespaces and is only present for older versions
     *             of LayoutLibs.
     */
    @Deprecated
    public Map<String, Integer> getAttributeValues(String name) {
        if (mAttrMap == null && !mAttrs.isEmpty()) {
            // Preserve insertion order. This order affects the int[] indices for styleables.
            mAttrMap = new LinkedHashMap<String, AttrResourceValue>(mAttrs.size());
            for (AttrResourceValue attr : mAttrs) {
                mAttrMap.put(attr.getName(), attr);
            }
        }
        if (mAttrMap != null) {
            AttrResourceValue attr = mAttrMap.get(name);
            if (attr != null) {
                return attr.getAttributeValues();
            }
        }
        return null;
    }

    @NonNull
    public List<AttrResourceValue> getAllAttributes() {
        return mAttrs;
    }

    public void addValue(@NonNull AttrResourceValue attr) {
        assert attr.isFramework() || !isFramework()
                : "Can't add non-framework attributes to framework resource.";
        mAttrs.add(attr);
    }
}
