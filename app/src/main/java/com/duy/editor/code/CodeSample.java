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

package com.duy.editor.code;

/**
 * Created by Duy on 12-Feb-17.
 */
@SuppressWarnings("DefaultFileTemplate")
public class CodeSample {

    public static final String DEMO_THEME =
            "program test;\n" +
                    "uses crt;\n" +
                    "type\n" +
                    "    int = integer;\n" +
                    "var\n" +
                    "    i, j: Integer;\n" +
                    "begin\n" +
                    "    i := 1;\n" +
                    "    j := 3;\n" +
                    "    writeln(i / j : 3 : 2);\n" +
                    "    {Comment...}\n" +
                    "end.";
}
