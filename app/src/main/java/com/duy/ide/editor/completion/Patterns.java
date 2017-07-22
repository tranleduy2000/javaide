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

package com.duy.ide.editor.completion;

import java.util.regex.Pattern;

/**
 * Created by Duy on 11-Feb-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Patterns {

    public static final Pattern LINE = Pattern.compile(".*\\n");


    /**
     * match reserved keyword
     */
    public static final Pattern KEYWORDS = Pattern.compile(
            "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|" +
                    "continue|default|do|double|else|enum|extends|final|finally|float|" +
                    "for|goto|if|implements|import|instanceof|int|interface|long|native|new|" +
                    "package|private|protected|public|short|static|super|switch|synchronized|" +
                    "this|throw|throws|transient|try|void|volatile|while)\\b",
            Pattern.CASE_INSENSITIVE);


    public static final Pattern FILE_JAVA = Pattern.compile("\\w+\\.java");

    /**
     * match some spacial symbol
     */
    public static final Pattern SYMBOLS = Pattern.compile("[+\\-'*=<>/:)(\\]\\[;@\\^,.]");
    /**
     * match number
     */
    public static final Pattern NUMBERS = Pattern.compile(
            "\\b((\\d*[.]?\\d+([Ee][+-]?[\\d]+)?)|" + //simple decimal
                    "(\\$[0-9a-fA-F]+)|" + //hex
                    "(%[01]+)|" + //binary
                    "(&[0-7]+)|" +//octal
                    "([Ee][+-]?[\\d]+))\\b");

}
