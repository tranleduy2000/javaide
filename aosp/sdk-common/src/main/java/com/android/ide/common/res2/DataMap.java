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
import com.google.common.collect.ListMultimap;

/**
 * A DataItem Map able to provide a {@link com.google.common.collect.ListMultimap} of data items
 * where the keys are the value returned by {@link DataItem#getKey()}
 */
interface DataMap<T extends DataItem> {

    /**
     * Returns the number of items.
     * @return the number of items
     */
    int size();

    /**
     * a Multi map of (key, dataItem) where key is the result of
     * {@link DataItem#getKey()}
     * @return a non null map
     */
    @NonNull
    ListMultimap<String, T> getDataMap();
}
