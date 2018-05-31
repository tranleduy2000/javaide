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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;

/**
 * Represents a file in a resource folders.
 *
 * It contains a link to the {@link File}, the qualifier string (which is the name of the folder
 * after the first '-' character), a list of {@link ResourceItem} and a type.
 *
 * The type of the file is based on whether the file is located in a values folder (FileType.MULTI)
 * or in another folder (FileType.SINGLE).
 */
public class ResourceFile extends DataFile<ResourceItem> {

    static final String ATTR_QUALIFIER = "qualifiers";

    private String mQualifiers;

    /**
     * Creates a resource file with a single resource item.
     *
     * The source file is set on the item with {@link ResourceItem#setSource(DataFile)}
     *
     * The type of the ResourceFile will by {@link FileType#SINGLE}.
     *
     * @param file the File
     * @param item the resource item
     * @param qualifiers the qualifiers.
     */
    public ResourceFile(@NonNull File file, @NonNull ResourceItem item,
            @NonNull String qualifiers) {
        super(file, FileType.SINGLE);
        mQualifiers = qualifiers;
        init(item);
    }

    /**
     * Creates a resource file with a list of resource items.
     *
     * The source file is set on the items with {@link ResourceItem#setSource(DataFile)}
     *
     * The type of the ResourceFile will by {@link FileType#MULTI}.
     *
     * @param file the File
     * @param items the resource items
     * @param qualifiers the qualifiers.
     */
    public ResourceFile(@NonNull File file, @NonNull List<ResourceItem> items,
            @NonNull String qualifiers) {
        super(file, FileType.MULTI);
        mQualifiers = qualifiers;
        init(items);
    }

    @NonNull
    public String getQualifiers() {
        return mQualifiers;
    }

    // Used in Studio
    public void setQualifiers(@NonNull String qualifiers) {
        mQualifiers = qualifiers;
    }

    @Override
    void addExtraAttributes(Document document, Node node, String namespaceUri) {
        NodeUtils.addAttribute(document, node, namespaceUri, ATTR_QUALIFIER,
                getQualifiers());
    }

    @Override
    public String toString() {
        return "ResourceFile{" +
                "mFile='" + getFile() + '\'' +
                ", mQualifiers='" + mQualifiers + '\'' +
                '}';
    }
}
