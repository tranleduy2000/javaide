/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.sdklib.internal.export;

class LogHelper {

    /**
     * Separator for putting multiple properties in a single {@link String}.
     */
    final static char PROP_SEPARATOR = ';';
    /**
     * Equal sign between the name and value of a property
     */
    final static char PROPERTY_EQUAL = '=';

    static void write(StringBuilder sb, String name, Object value) {
        sb.append(name).append(PROPERTY_EQUAL).append(value).append(PROP_SEPARATOR);
    }

    static void write(StringBuilder sb, String name, int value) {
        sb.append(name).append(PROPERTY_EQUAL).append(value).append(PROP_SEPARATOR);
    }

}
