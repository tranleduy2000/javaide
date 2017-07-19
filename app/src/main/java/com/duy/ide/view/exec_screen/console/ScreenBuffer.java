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

package com.duy.ide.view.exec_screen.console;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Duy on 26-Mar-17.
 */

public class ScreenBuffer {

    public int firstIndex;
    public char[] textOnScreenBuffer;
    public TextConsole[] textConsole;
    public int[] colorScreenBuffer;
    /**
     * store text input, with unicode character
     */
    public ConsoleInputStream textBuffer = new ConsoleInputStream();

    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public TextConsole getTextAt(int index) {
        return textConsole[index];
    }

    public TextConsole[] getTextConsole() {
        return textConsole;
    }

    public void setTextConsole(TextConsole[] textConsole) {
        this.textConsole = textConsole;
    }

    public char[] getTextOnScreenBuffer() {
        return textOnScreenBuffer;
    }

    public void setTextOnScreenBuffer(char[] textOnScreenBuffer) {
        this.textOnScreenBuffer = textOnScreenBuffer;
    }

    public int[] getColorScreenBuffer() {
        return colorScreenBuffer;
    }

    public void setColorScreenBuffer(int[] colorScreenBuffer) {
        this.colorScreenBuffer = colorScreenBuffer;
    }

    /**
     * save current text to file
     */
    public void store() {
// TODO: 26-Mar-17
    }

    /**
     * read text from then file
     */
    public void restore() {
// TODO: 26-Mar-17
    }

    public int getFirstIndex() {
        return firstIndex;
    }


    public void clearAll() {
        textOnScreenBuffer = null;
        textConsole = null;
        colorScreenBuffer = null;
    }
}
