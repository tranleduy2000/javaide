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

package com.duy.editor.editor.completion;


import com.duy.editor.utils.ArrayUtil;

/**
 * Created by Duy on 30-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class KeyWord {

    public static final String[] RESERVED_KEY_WORDS;
    public static final String[] SPINETS;
    public static final String[] SYMBOL_KEY;
    //operator
    public static final String[] OPERATOR_BOOLEAN;
    public static final String[] OPERATOR;
    //build in type
    public static final String[] COMMON_TYPE;
    public static final String[] INTEGER_TYPE;
    public static final String[] REAL_TYPE;
    public static final String[] STRING_TYPE;
    public static final String[] BOOLEAN_TYPE;
    public static final String[] ALL_KEY_WORD;
    public static final String[] DATA_TYPE;

    static {
        RESERVED_KEY_WORDS = new String[]{
                "program", "begin", "end", "procedure", "function",
                "const", "var", "array", "record", "uses", "set", //declare
                "shl", "shr", "if ", "then", "else", "case", "of", "with", //condition
                "for", "to", "do", "downto", "while", "repeat", "until", //loop
                "and", "or", "xor", "not", "div", "mod", //operator
                "unit", "interface", "initialization", "finalization", "implementation", //unit
                "continue", "exit", "break" //exit
        };

        SPINETS = new String[]{
                "if %v then  ;",
                "if %v then \n" +
                        "begin\n" +
                        "\t\n" +
                        "end;\n",
                "if %v then  else  ;\n",
                "if %v then\n " +
                        "begin\n " +
                        "\t\n" +
                        "end\n" +
                        "else\n" +
                        "begin\n" +
                        "\t\n" +
                        "end;\n",
                "for %v :=  to  do  \n",
                "for %v :=  to  do\n" +
                        "begin\n" +
                        "\t\n" +
                        "end;\n",
                "for %v :=  downto  do  \n",
                "for %v :=  downto  do\n" +
                        "begin\n" +
                        "\t\n" +
                        "end;\n",
                "while %v do  ;\n",
                "while %v do\n" +
                        "begin\n" +
                        "\t\n" +
                        "end;\n",
                "case %v of \n" +
                        "\t\n" +
                        "end;\n",
                "case %v of \n" +
                        "\t\n" +
                        "end;\n" +
                        "else  ;\n",
                "repeat\n" +
                        "\t\n" +
                        "until (%v = );\n"

        };

        OPERATOR_BOOLEAN = new String[]{"and", "or", "xor", "not", "<", ">", "=", "<>", "<=", ">="};
        OPERATOR = new String[]{"+", "-", "*", "/", "div", "mod"};

        COMMON_TYPE = new String[]{"String", "Char", "Integer", "Real"};
        REAL_TYPE = new String[]{"Real", "Single", "Double", "Extended", "Comp", "Currency"};
        INTEGER_TYPE = new String[]{"Byte", "ShortInt", "Smallint", "Word", "Integer", "Cardinal", "Longint", "Longword", "Int64", "QWord"};
        STRING_TYPE = new String[]{"String", "AnsiString", "Char"};
        BOOLEAN_TYPE = new String[]{"Boolean", "ByteBool", "WordBool", "LongBool"};

        DATA_TYPE = ArrayUtil.join(String.class, COMMON_TYPE, INTEGER_TYPE, REAL_TYPE, STRING_TYPE, BOOLEAN_TYPE);

        ALL_KEY_WORD = ArrayUtil.join(String.class,  RESERVED_KEY_WORDS, REAL_TYPE, INTEGER_TYPE, STRING_TYPE, BOOLEAN_TYPE);

        SYMBOL_KEY = new String[]{":=", ";", ".", "[", "]", ":", "'", "(", ")", "<", ">", "=", "<>", "<=", ">=", "{", "}", "+", "-", "*", "/", "_"};

    }


    public KeyWord() {
        initKeyWord();
    }

    private void initKeyWord() {
    }

}
