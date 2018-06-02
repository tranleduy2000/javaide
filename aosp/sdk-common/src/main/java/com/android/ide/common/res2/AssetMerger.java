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

import org.w3c.dom.Node;

import java.util.List;

/**
 * Implementation of {@link DataMerger} for {@link AssetSet}, {@link AssetItem}, and
 * {@link AssetFile}.
 */
public class AssetMerger extends DataMerger<AssetItem, AssetFile, AssetSet> {

    @Override
    protected AssetSet createFromXml(Node node) throws MergingException {
        AssetSet set = new AssetSet("");
        return (AssetSet) set.createFromXml(node);
    }

    @Override
    protected boolean requiresMerge(@NonNull String dataItemKey) {
        return false;
    }

    @Override
    protected void mergeItems(
            @NonNull String dataItemKey,
            @NonNull List<AssetItem> items,
            @NonNull MergeConsumer<AssetItem> consumer) {
        // nothing to do
    }
}
