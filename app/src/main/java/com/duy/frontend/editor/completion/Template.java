/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.editor.completion;

import android.support.annotation.NonNull;

/**
 * Created by Duy on 22-May-17.
 */
public class Template {
    public static final String PROGRAM_TEMPLATE =
            "\n" +
                    "public class %1$s {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        \n" +
                    "    }\n" +
                    "}\n";

    private static final String INTERFACE_TEMPLATE =
            "\n" +
                    "public interface %1$s {\n" +
                    "}\n";
    private static final String ENUM_TEMPLATE =
            "\n" +
                    "public enum %1$s {\n" +
                    "}\n";

    @NonNull
    public static String createClass(String name) {
        return String.format(PROGRAM_TEMPLATE, name);
    }

    @NonNull
    public static String createEnum(String name) {
        return String.format(ENUM_TEMPLATE, name);
    }

    @NonNull
    public static String createInterface(@NonNull String name) {
        return String.format(INTERFACE_TEMPLATE, name);
    }
}
