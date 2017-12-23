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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Describe the content of the dynamic android.widget.Adapter used to fill
 * android.widget.AdapterView
 */
public class AdapterBinding implements Iterable<DataBindingItem> {

    private final int mRepeatCount;
    private final List<ResourceReference> mHeaders = new ArrayList<ResourceReference>();
    private final List<DataBindingItem> mItems = new ArrayList<DataBindingItem>();
    private final List<ResourceReference> mFooters = new ArrayList<ResourceReference>();

    public AdapterBinding(int repeatCount) {
        mRepeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return mRepeatCount;
    }

    public void addHeader(ResourceReference layoutInfo) {
        mHeaders.add(layoutInfo);
    }

    public int getHeaderCount() {
        return mHeaders.size();
    }

    public ResourceReference getHeaderAt(int index) {
        return mHeaders.get(index);
    }

    public void addItem(DataBindingItem item) {
        mItems.add(item);
    }

    public int getItemCount() {
        return mItems.size();
    }

    public DataBindingItem getItemAt(int index) {
        return mItems.get(index);
    }

    public void addFooter(ResourceReference layoutInfo) {
        mFooters.add(layoutInfo);
    }

    public int getFooterCount() {
        return mFooters.size();
    }

    public ResourceReference getFooterAt(int index) {
        return mFooters.get(index);
    }

    @Override
    public Iterator<DataBindingItem> iterator() {
        return mItems.iterator();
    }
}
