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
 * An asset.
 *
 * This includes the name and source file as a {@link AssetFile}.
 *
 */
class AssetItem extends DataItem<AssetFile> {

    /**
     * Constructs the object with a name
     *
     * Note that the object is not fully usable as-is. It must be added to an AssetFile first.
     *
     * @param name the name of the asset
     */
    AssetItem(@NonNull String name) {
        super(name);
    }

    static AssetItem create(@NonNull File sourceFolder, @NonNull File file) {
        // compute the relative path
        StringBuilder sb = new StringBuilder();
        computePath(sb, file.getParentFile(), sourceFolder);
        sb.append(file.getName());

        return new AssetItem(sb.toString());
    }

    private static void computePath(StringBuilder sb, File current, File stop) {
        if (current.equals(stop)) {
            return;
        }

        computePath(sb, current.getParentFile(), stop);
        sb.append(current.getName()).append('/');
    }
}
