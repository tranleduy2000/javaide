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

    public static final String UNIT_TEMPlATE =
            "unit %s;\n" +
                    "interface\n" +
                    "    \n" +
                    "implementation\n" +
                    "    \n" +
                    "initialization\n" +
                    "begin\n" +
                    "    \n" +
                    "end;\n" +
                    "finalization\n" +
                    "begin\n" +
                    "    \n" +
                    "end;\n" +
                    "end.";

    public static final String FUNCTION_TEMPLATE =
            "\nfunction %1$s( ) : ;\n" +
                    "begin\n" +
                    "    \n" +
                    "end;\n";

    public static String createProgramTemplate(String name) {
        return String.format(PROGRAM_TEMPLATE, name);
    }

    public static String createUnitTemplate(String name) {
        return String.format(UNIT_TEMPlATE, name);
    }
}
