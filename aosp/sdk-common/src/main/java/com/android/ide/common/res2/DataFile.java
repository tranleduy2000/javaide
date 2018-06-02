/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.ide.common.res2;

import com.android.annotations.NonNull;
import com.google.common.collect.Maps;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a data file.
 *
 * It contains a link to its {@link java.io.File}, and the {@link DataItem}s it generates.
 *
 */
public abstract class DataFile<I extends DataItem> {

    enum FileType {
        SINGLE_FILE,
        GENERATED_FILES,
        XML_VALUES
    }

    private final FileType mType;
    protected File mFile;
    protected final Map<String, I> mItems = Maps.newHashMap();

    DataFile(@NonNull File file, FileType fileType) {
        mType = fileType;
        mFile = file;
    }

    /**
     * This must be called from the constructor of the children classes.
     * @param item the item
     */
    protected final void init(@NonNull I item) {
        addItem(item);
    }

    /**
     * This must be called from the constructor of the children classes.
     * @param items the items
     */
    protected final void init(@NonNull Iterable<I> items) {
        addItems(items);
    }

    @NonNull
    FileType getType() {
        return mType;
    }

    @NonNull
    public File getFile() {
        return mFile;
    }

    I getItem() {
        assert mItems.size() == 1;
        return mItems.values().iterator().next();
    }

    boolean hasNotRemovedItems() {
        for (I item : mItems.values()) {
            if (!item.isRemoved()) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    public Collection<I> getItems() {
        return mItems.values();
    }

    @NonNull
    public Map<String, I> getItemMap() {
        return mItems;
    }

    public void addItem(@NonNull I item) {
          //noinspection unchecked
          item.setSource(this);
          mItems.put(item.getKey(), item);
     }

    public void addItems(@NonNull Iterable<I> items) {
        for (I item : items) {
            //noinspection unchecked
            item.setSource(this);
            mItems.put(item.getKey(), item);
        }
    }

    public void removeItems(@NonNull Iterable<I> items) {
        for (I item : items) {
            mItems.remove(item.getKey());
            //noinspection unchecked
            item.setSource(null);
        }
    }

    public void removeItem(ResourceItem item) {
        mItems.remove(item.getKey());
        item.setSource(null);
    }

    void addExtraAttributes(Document document, Node node, String namespaceUri) {
        // nothing
    }

    public void replace(@NonNull I oldItem, @NonNull I newItem) {
        mItems.remove(oldItem.getKey());
        //noinspection unchecked
        oldItem.setSource(null);
        //noinspection unchecked
        newItem.setSource(this);
        mItems.put(newItem.getKey(), newItem);
    }

    @Override
    public String toString() {
        return "DataFile{" +
                "mFile=" + mFile +
                '}';
    }
}
