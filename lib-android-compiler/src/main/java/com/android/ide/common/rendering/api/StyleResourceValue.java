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

package com.android.ide.common.rendering.api;

import com.android.ide.common.rendering.api.ItemResourceValue.Attribute;
import com.android.layoutlib.api.IResourceValue;
import com.android.layoutlib.api.IStyleResourceValue;
import com.android.resources.ResourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an android style resource with a name and a list of children {@link ResourceValue}.
 */
@SuppressWarnings("deprecation")
public final class StyleResourceValue extends ResourceValue implements IStyleResourceValue {

    private String mParentStyle = null;
    private final Map<Attribute, ItemResourceValue> mItems
            = new HashMap<Attribute, ItemResourceValue>();

    public StyleResourceValue(ResourceType type, String name, boolean isFramework) {
        super(type, name, isFramework);
    }

    public StyleResourceValue(ResourceType type, String name, String parentStyle,
            boolean isFramework) {
        super(type, name, isFramework);
        mParentStyle = parentStyle;
    }

    /**
     * Returns the parent style name or <code>null</code> if unknown.
     */
    @Override
    public String getParentStyle() {
        return mParentStyle;
    }

    /**
     * Finds a value in the list by name
     * @param name the name of the resource
     *
     * @deprecated use {@link #getItem(String, boolean)}
     */
    @Deprecated
    public ResourceValue findValue(String name) {
        return getItem(name, isFramework());
    }

    /**
     * Finds a value in the list by name
     * @param name the name of the resource
     *
     * @deprecated use {@link #getItem(String, boolean)}
     */
    @Deprecated
    public ResourceValue findValue(String name, boolean isFrameworkAttr) {
        return getItem(name, isFrameworkAttr);
    }

    /**
     * Finds a value in the list of items by name.
     * @param name the name of the resource
     * @param isFrameworkAttr is it in the framework namespace
     */
    public ItemResourceValue getItem(String name, boolean isFrameworkAttr) {
        return mItems.get(new Attribute(name, isFrameworkAttr));
    }

    /**
     * @deprecated use {@link #addItem(ItemResourceValue)}
     */
    @Deprecated
    public void addValue(ResourceValue value, boolean isFrameworkAttr) {
        addItem(ItemResourceValue.fromResourceValue(value, isFrameworkAttr));
    }

    public void addItem(ItemResourceValue value) {
        mItems.put(value.getAttribute(), value);
    }

    @Override
    public void replaceWith(ResourceValue value) {
        assert value instanceof StyleResourceValue :
                value.getClass() + " is not StyleResourceValue";
        super.replaceWith(value);

        //noinspection ConstantConditions
        if (value instanceof StyleResourceValue) {
            mItems.clear();
            mItems.putAll(((StyleResourceValue) value).mItems);
        }
    }

    /**
     * Legacy method.
     * @deprecated use {@link #getValue()}
     */
    @Override
    @Deprecated
    public IResourceValue findItem(String name) {
        return mItems.get(new Attribute(name, true));
    }

    /** Returns the names available in this style, intended for diagnostic purposes */
    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (Attribute item : mItems.keySet()) {
            String name = item.mName;
            if (item.mIsFrameworkAttr) {
                name = "android:" + name;
            }
            names.add(name);
        }
        return names;
    }

    /**
     * Returns a list of all values defined in this Style. This doesn't return the values
     * inherited from the parent.
     */
    public Collection<ItemResourceValue> getValues() {
        return mItems.values();
    }
}
