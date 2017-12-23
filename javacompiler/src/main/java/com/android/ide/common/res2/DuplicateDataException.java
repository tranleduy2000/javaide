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

/**
 * Exception when a {@link DataItem} is declared more than once in a {@link DataSet}
 */
public class DuplicateDataException extends MergingException {

    private DataItem mOne;
    private DataItem mTwo;

    DuplicateDataException(@NonNull DataItem one, @NonNull DataItem two) {
        super(String.format("Duplicate resources: %1s:%2s, %3s:%4s",
                one.getSource().getFile().getAbsolutePath(), one.getKey(),
                two.getSource().getFile().getAbsolutePath(), two.getKey()));
        mOne = one;
        mTwo = two;
        setFile(one.getSource().getFile());
    }

    public DataItem getOne() {
        return mOne;
    }

    public DataItem getTwo() {
        return mTwo;
    }
}
