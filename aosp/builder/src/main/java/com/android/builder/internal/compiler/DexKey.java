/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.builder.internal.compiler;

import com.android.annotations.NonNull;
import com.android.annotations.concurrency.Immutable;
import com.android.sdklib.repository.FullRevision;

import java.io.File;

/**
 * Key to store Item/StoredItem in maps.
 * The key contains the element that are used for the dex call:
 * - source file
 * - build tools revision
 * - jumbo mode
 */
@Immutable
class DexKey extends PreProcessCache.Key {
    private final boolean mJumboMode;

    static DexKey of(@NonNull File sourceFile, @NonNull FullRevision buildToolsRevision,
            boolean jumboMode) {
        return new DexKey(sourceFile, buildToolsRevision, jumboMode);
    }

    private DexKey(@NonNull File sourceFile, @NonNull FullRevision buildToolsRevision,
            boolean jumboMode) {
        super(sourceFile, buildToolsRevision);
        mJumboMode = jumboMode;
    }

    boolean isJumboMode() {
        return mJumboMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DexKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DexKey dexKey = (DexKey) o;

        if (mJumboMode != dexKey.mJumboMode) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mJumboMode ? 1 : 0);
        return result;
    }
}
