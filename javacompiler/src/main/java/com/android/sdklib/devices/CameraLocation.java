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

package com.android.sdklib.devices;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

public enum CameraLocation {
    FRONT("front"), //$NON-NLS-1$
    BACK("back");   //$NON-NLS-1$

    @NonNull
    private final String mValue;

    CameraLocation(@NonNull String value) {
        mValue = value;
    }

    @Nullable
    public static CameraLocation getEnum(@NonNull String value) {
        for (CameraLocation l : values()) {
            if (l.mValue.equals(value)) {
                return l;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return mValue;
    }
}
