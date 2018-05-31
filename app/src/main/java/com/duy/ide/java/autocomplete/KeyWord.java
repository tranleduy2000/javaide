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

package com.duy.ide.java.autocomplete;


/**
 * Created by Duy on 30-Mar-17.
 */

public class KeyWord {

    public static final String[] RESERVED_KEY_WORDS;
    public static final String[] SYMBOL_KEY;


    static {
        RESERVED_KEY_WORDS = new String[]{
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
                "int", "interface", "long", "native", "new", "package", "private", "protected",
                "public", "short", "static", "super", "switch", "synchronized",
                "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };
        SYMBOL_KEY = new String[]{"{", "}", "(", ")", ";", ",", ".", "=", "\"", "|", "&", "!", "[",
                "]", "<", ">", "+", "-", "/", "*", "?", ":", "_"};

    }


    public KeyWord() {
        initKeyWord();
    }

    private void initKeyWord() {
    }

}
