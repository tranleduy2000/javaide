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

package com.android.ide.common.res2;

import com.android.annotations.NonNull;

import java.io.File;

/**
 * Represents a file in an asset folder.
 */
class AssetFile extends DataFile<AssetItem> {

    /**
     * Creates a resource file with a single resource item.
     *
     * The source file is set on the item with {@link AssetItem#setSource(DataFile)}
     *
     * @param file the File
     * @param item the resource item
     */
    AssetFile(@NonNull File file, @NonNull AssetItem item) {
        super(file, FileType.SINGLE_FILE);
        init(item);
    }
}
