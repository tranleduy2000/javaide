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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A data binding item. It contain a {@link ResourceReference} to the view used to represent it.
 * It also contains how many items of this type the AdapterView should display.
 *
 * It can also contain an optional list of children in case the AdapterView is an
 * ExpandableListView. In this case, the count value is used as a repeat count for the children,
 * similar to {@link AdapterBinding#getRepeatCount()}.
 *
 */
public class DataBindingItem implements Iterable<DataBindingItem> {
    private final ResourceReference mReference;
    private final int mCount;
    private List<DataBindingItem> mChildren;

    public DataBindingItem(ResourceReference reference, int count) {
        mReference = reference;
        mCount = count;
    }

    public DataBindingItem(String name, boolean platformLayout, int count) {
        this(new ResourceReference(name, platformLayout), count);
    }

    public DataBindingItem(String name, boolean platformLayout) {
        this(name, platformLayout, 1);
    }

    public DataBindingItem(String name, int count) {
        this(name, false /*platformLayout*/, count);
    }

    public DataBindingItem(String name) {
        this(name, false /*platformLayout*/, 1);
    }

    /**
     * Returns the {@link ResourceReference} for the view. The {@link ResourceType} for the
     * referenced resource is implied to be {@link ResourceType#LAYOUT}.
     */
    public ResourceReference getViewReference() {
        return mReference;
    }

    /**
     * The repeat count for this object or the repeat count for the children if there are any.
     */
    public int getCount() {
        return mCount;
    }

    public void addChild(DataBindingItem child) {
        if (mChildren == null) {
            mChildren = new ArrayList<DataBindingItem>();
        }

        mChildren.add(child);
    }

    public List<DataBindingItem> getChildren() {
        if (mChildren != null) {
            return mChildren;
        }

        return Collections.emptyList();
    }

    @Override
    public Iterator<DataBindingItem> iterator() {
        List<DataBindingItem> list = getChildren();
        return list.iterator();
    }
}
