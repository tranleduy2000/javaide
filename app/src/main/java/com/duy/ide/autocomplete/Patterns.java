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

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * Created by Duy on 11-Feb-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Patterns {

    public static final Pattern LINE = compile(".*\\n");

    /**
     * match reserved keyword
     */
    public static final Pattern JAVA_KEYWORDS = compile(
            "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|" +
                    "continue|default|do|double|else|enum|extends|final|finally|float|" +
                    "for|goto|if|implements|import|instanceof|int|interface|long|native|new|" +
                    "package|private|protected|public|short|static|super|switch|synchronized|" +
                    "this|throw|throws|transient|try|void|volatile|while|" +
                    "null)\\b", CASE_INSENSITIVE);
    public static final Pattern JAVA_COMMENTS = compile("(//.*)|(/\\*(?:.|[\\n\\r])*?\\*/)");
    /**
     * match string java
     * include
     * <p>
     * 'string'
     * <p>
     * And can not find close quote
     * <p>
     * 'sadhasdhasdhashdhas ds asda sd
     */
    public static final Pattern STRINGS = Pattern.compile(
            "((\")(.*?)(\"))" +//'string'
                    "|((\")(.+))", Pattern.DOTALL); // no end string 'asdasdasd

    public static final Pattern CHARS = compile("(\')(.*?)(\')");

    /*Number*/
    public static final Pattern DECIMAL_NUMBERS = compile(
            "\\b((\\d*[.]?\\d+([Ee][+-]?[\\d]+)?[LlfFdD]?)|" + //simple decimal
                    "(0[xX][0-9a-zA-Z]+)|" + //hex
                    "(0[bB][0-1]+)|" + //binary
                    "(0[0-7]+))\\b"); //octal);

    /*XML patterns*/
    public static final Pattern XML_TAGS = compile(
            "<([A-Za-z][A-Za-z0-9]*)\\b[^>]*>|</([A-Za-z][A-Za-z0-9]*)\\b[^>]*>");
    public static final Pattern XML_ATTRS = compile(
            "(\\S+)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
    public static final Pattern XML_COMMENTS = compile("(?s)<!--.*?-->");


    public static final Pattern PRIMITIVE_TYPES = compile("boolean|byte|char|int|short|long|float|double");
    public static final Pattern KEYWORD_TYPES = compile("class|interface|enum");
    public static final Pattern KEYWORDS_MODS = compile("public|private|protected|static|final|synchronized|volatile|transient|native|strictfp|abstract");
    public static final Pattern KEYWORDS = compile(PRIMITIVE_TYPES.toString()
            + KEYWORDS_MODS.toString() + "|"
            + KEYWORD_TYPES.toString() + "|" +
            "super|this|void|assert|break|case|catch|" +
            "const|continue|default|do|else|extends|finally|for|goto|if|implements|import|instanceof|" +
            "interface|new|package|return|switch|throw|throws|try|while|true|false|null");

    public static final Pattern RE_BRACKETS = compile("(\\s*\\[\\s*\\])");

    public static final Pattern RE_IDENTIFIER = compile("[a-zA-Z_][a-zA-Z0-9_]*");
    // case:   new JCTree.JCClassDecl();
    public static final Pattern RE_QUALID = compile(RE_IDENTIFIER + "(\\s*\\.\\s*" + RE_IDENTIFIER + ")?");
    public static final Pattern RE_REFERENCE_TYPE = compile(RE_QUALID + RE_BRACKETS.toString() + "\\*");
    public static final Pattern RE_TYPE = RE_REFERENCE_TYPE;

    public static final Pattern RE_TYPE_MODS = compile("public|protected|private|abstract|static|final|strictfp");
    public static final Pattern RE_TYPE_DECL_HEAD = compile("(class|inteface|enum)" + "[\\s]+");
    public static final Pattern RE_TYPE_DECL = compile("(" + RE_TYPE_MODS + "\\s+)*" + RE_TYPE_DECL_HEAD + RE_IDENTIFIER);

    public static final Pattern RE_ARRAY_TYPE = compile("^\\s*(" + RE_QUALID + ")(" + RE_BRACKETS + ")+\\s*");
    public static final Pattern RE_SELECT_OR_ACCESS = compile("^\\s*(" + RE_IDENTIFIER + ")\\s*(\\[.*\\])?\\s*$");
    public static final Pattern RE_ARRAY_ACCESS = compile("^\\s*(" + RE_IDENTIFIER + ")" + "\\s*(\\[.*\\])+\\s*$");
    public static final Pattern RE_CASTING = compile("\\s*\\((" + RE_QUALID + "\\s*)\\)\\s*" + RE_IDENTIFIER);
    public static final Pattern RE_KEYWORDS = KEYWORDS;

    public static final Pattern PACKAGE_NAME = compile("([A-Za-z0-9_]([.][a-zA-Z0-9_])?)*");
}
