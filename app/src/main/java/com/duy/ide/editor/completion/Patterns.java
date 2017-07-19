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
    /**
     * match builtin pascal function
     */
    public static final Pattern BUILTIN_FUNCTIONS = Pattern.compile(
            "\\b(sin|cos|sqrt|length" +
                    "|exp|tan|keyPressed|readKey|delay|random|randomize|inc|dec" +
                    "|ceil|trunc|frac|floor|abs|round|sqr|pred|succ|ln|arctan" +
                    "|odd|int|halt|odd)\\b", Pattern.CASE_INSENSITIVE);
    /**
     * match some spacial symbol
     */
    public static final Pattern SYMBOLS = Pattern.compile("[+\\-'*=<>/:)(\\]\\[;@\\^,.]");

    public static final Pattern REPLACE_HIGHLIGHT = Pattern.compile("\"(.*?)\"");
    public static final Pattern REPLACE_CURSOR = Pattern.compile("%\\w");
    public static final Pattern VAR = Pattern.compile("\\b(var)\\b", Pattern.CASE_INSENSITIVE);
    public static final Pattern TYPE = Pattern.compile("\\b(type)\\b", Pattern.CASE_INSENSITIVE);
    public static final Pattern PROGRAM = Pattern.compile("\\b(program)[\\s](.*?);\\b", Pattern.CASE_INSENSITIVE);
    public static final Pattern USES = Pattern.compile("\\b(uses)[\\s](.*?);\\b", Pattern.CASE_INSENSITIVE);
    public static final Pattern CONST = Pattern.compile("\\b(const)\\b", Pattern.CASE_INSENSITIVE);
    public static final Pattern OPEN_PATTERN
            = Pattern.compile("(begin|then|else|do|repeat|of|" +
                    "type|var|const|interface|implementation)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern END_PATTERN
            = Pattern.compile("\\b(end)\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * match number
     */
    public static final Pattern NUMBERS = Pattern.compile(
            "\\b((\\d*[.]?\\d+([Ee][+-]?[\\d]+)?)|" + //simple decimal
                    "(\\$[0-9a-fA-F]+)|" + //hex
                    "(%[01]+)|" + //binary
                    "(&[0-7]+)|" +//octal
                    "([Ee][+-]?[\\d]+))\\b");

    public static final Pattern HEX_COLOR = Pattern.compile("(#[0-9a-fA-F]{6})");

    public static final Pattern RGB_FUNCTION = Pattern.compile(
            "([Rr][Gg][Bb])" + //1
                    "(\\()" +//2
                    "(\\s?\\d+\\s?)" +//3
                    "(,)" +//4
                    "(\\s?\\d+\\s?)" +//5
                    "(,)" +//6
                    "(\\s?\\d+\\s?)" +//7
                    "(\\))");

    public static final Pattern ARGB_FUNCTION = Pattern.compile(
            "([Aa][Rr][Gg][Bb])" +
                    "(\\()" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(\\))");

    public static final Pattern TEXT_COLOR_FUNCTION = Pattern.compile("(textColor)" +
            "(\\()" +
            "([0-9]+)" +
            "(\\))");

    public static final Pattern TEXT_BACKGROUND_FUNCTION = Pattern.compile("(textColor)" +
            "(\\()" +
            "([0-9]+)" +
            "(\\))");

}
