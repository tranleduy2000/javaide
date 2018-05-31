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

public enum ButtonType {
    HARD("hard", "Hardware"), //$NON-NLS-1$
    SOFT("soft", "Software"); //$NON-NLS-1$

    @NonNull
    private final String mId;
    @NonNull
    private final String mDescription;

    /**
     * Construct a {@link ButtonType}.
     *
     * @param id identifier for this button type. Persisted on disk when a user creates a device.
     * @param desc User friendly description
     */
    ButtonType(@NonNull String id, @NonNull String desc) {
        mId = id;
        mDescription = desc;
    }

    @Nullable
    public static ButtonType getEnum(@NonNull String value) {
        for (ButtonType n : values()) {
            if (n.mId.equals(value)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return mId;
    }

    @NonNull
    public String getDescription() {
        return mDescription;
    }
}
