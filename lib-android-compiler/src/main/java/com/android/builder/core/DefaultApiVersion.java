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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.ApiVersion;

/**
 * Basic implementation of ApiVersion
 */
public class DefaultApiVersion implements ApiVersion {

    private final int mApiLevel;

    @Nullable
    private final String mCodename;

    public DefaultApiVersion(int apiLevel, @Nullable String codename) {
        mApiLevel = apiLevel;
        mCodename = codename;
    }

    public DefaultApiVersion(int apiLevel) {
        this(apiLevel, null);
    }

    public DefaultApiVersion(@NonNull String codename) {
        this(1, codename);
    }

    @NonNull
    public static ApiVersion create(@NonNull Object value) {
        if (value instanceof Integer) {
            return new DefaultApiVersion((Integer) value, null);
        } else if (value instanceof String) {
            return new DefaultApiVersion(1, (String) value);
        }

        return new DefaultApiVersion(1, null);
    }

    @Override
    public int getApiLevel() {
        return mApiLevel;
    }

    @Nullable
    @Override
    public String getCodename() {
        return mCodename;
    }

    @NonNull
    @Override
    public String getApiString() {
        return mCodename != null ? mCodename : Integer.toString(mApiLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultApiVersion that = (DefaultApiVersion) o;

        if (mApiLevel != that.mApiLevel) {
            return false;
        }
        if (mCodename != null ? !mCodename.equals(that.mCodename) : that.mCodename != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = mApiLevel;
        result = 31 * result + (mCodename != null ? mCodename.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApiVersionImpl{" +
                "mApiLevel=" + mApiLevel +
                ", mCodename='" + mCodename + '\'' +
                '}';
    }
}
