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

package com.android.sdklib.util;

import com.android.annotations.NonNull;

/**
 * Helper methods to do some format conversions.
 */
public abstract class FormatUtils {

    /**
     * Converts a byte size to a human readable string,
     * for example "3 MiB", "1020 Bytes" or "1.2 GiB".
     *
     * @param size The byte size to convert.
     * @return A new non-null string, with the size expressed in either Bytes
     *   or KiB or MiB or GiB.
     */
    @NonNull
    public static String byteSizeToString(long size) {
        String sizeStr;

        if (size < 1024) {
            sizeStr = String.format("%d Bytes", size);
        } else if (size < 1024 * 1024) {
            sizeStr = String.format("%d KiB", Math.round(size / 1024.0));
        } else if (size < 1024 * 1024 * 1024) {
            sizeStr = String.format("%.1f MiB",
                    Math.round(10.0 * size / (1024 * 1024.0))/ 10.0);
        } else {
            sizeStr = String.format("%.1f GiB",
                    Math.round(10.0 * size / (1024 * 1024 * 1024.0))/ 10.0);
        }

        return sizeStr;
    }

}
