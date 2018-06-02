/*
 * Copyright (C) 2015 The Android Open Source Project
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

public abstract class DataBindingResourceItem extends DataItem<ResourceFile> {

    private DataBindingResourceType mType;

    /**
     * Constructs the object with a name, type and optional value. <p/> Note that the object is not
     * fully usable as-is. It must be added to a DataFile first.
     *
     * @param name the name of the item
     */
    public DataBindingResourceItem(@NonNull String name, @NonNull DataBindingResourceType type) {
        super(name);
        mType = type;
    }

    @NonNull
    public DataBindingResourceType getType() {
        return mType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DataBindingResourceItem that = (DataBindingResourceItem) o;
        if (mType != that.mType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
    }
}
