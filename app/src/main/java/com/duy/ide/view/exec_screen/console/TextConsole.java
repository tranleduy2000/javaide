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

import android.graphics.Canvas;
import android.graphics.Color;

import com.duy.ide.view.exec_screen.ScreenObject;

/**
 * Created by Duy on 04-Apr-17.
 */

public class TextConsole implements ScreenObject {
    public String text = "";
    private int textBackground = Color.BLACK;//back
    public int alpha = 255;
    private int textColor = Color.WHITE; //white

    public TextConsole(String text, int textBackground) {
        this.text = text;
        this.textBackground = textBackground;
    }

    public TextConsole(String text, int textBackground, int textColor) {
        this.text = text;
        this.textBackground = textBackground;
        this.textColor = textColor;
    }

    /**
     * Constructor
     */
    public TextConsole() {
        //default value for the text
        this.text = "\0";
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public String getSingleString() {
        return text.substring(0, 1);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextBackground() {
        return textBackground;
    }

    public void setTextBackground(int textBackground) {
        this.textBackground = textBackground;
    }

    @Override
    public void draw(Canvas canvas) {

    }

    public void setText(String text) {
        this.text = text;
    }
}
