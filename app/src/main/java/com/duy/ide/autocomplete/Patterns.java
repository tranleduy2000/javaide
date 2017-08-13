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

package com.duy.ide.autocomplete;

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
    public static final Pattern JAVA_KEYWORDS = Pattern.compile(
            "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|" +
                    "continue|default|do|double|else|enum|extends|final|finally|float|" +
                    "for|goto|if|implements|import|instanceof|int|interface|long|native|new|" +
                    "package|private|protected|public|short|static|super|switch|synchronized|" +
                    "this|throw|throws|transient|try|void|volatile|while|" +
                    "null)\\b",
            Pattern.CASE_INSENSITIVE);
    public static final Pattern JAVA_COMMENTS = Pattern.compile("(//.*)|(/\\*(?:.|[\\n\\r])*?\\*/)");


    /**
     * match number
     */
    public static final Pattern DECIMAL_NUMBERS = Pattern.compile(
            "\\b((\\d*[.]?\\d+([Ee][+-]?[\\d]+)?[LlfFdD]?)|" + //simple decimal
                    "(0[xX][0-9a-zA-Z]+)|" + //hex
                    "(0[bB][0-1]+)|" + //binary
                    "(0[0-7]+))\\b"); //octal);

    /*XML patterns*/
    public static final Pattern XML_TAGS = Pattern.compile(
            "<([A-Za-z][A-Za-z0-9]*)\\b[^>]*>|</([A-Za-z][A-Za-z0-9]*)\\b[^>]*>");
    public static final Pattern XML_ATTRS = Pattern.compile(
            "(\\S+)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
    public static final Pattern XML_COMMENTS = Pattern.compile("(?s)<!--.*?-->");
}
