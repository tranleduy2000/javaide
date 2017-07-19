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

package com.duy.ide.keyboard;

import android.view.KeyEvent;

import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_CTRL_RIGHT;
import static android.view.KeyEvent.KEYCODE_F;
import static android.view.KeyEvent.KEYCODE_G;
import static android.view.KeyEvent.KEYCODE_H;
import static android.view.KeyEvent.KEYCODE_L;
import static android.view.KeyEvent.KEYCODE_O;
import static android.view.KeyEvent.KEYCODE_R;
import static android.view.KeyEvent.KEYCODE_S;
import static android.view.KeyEvent.KEYCODE_V;
import static android.view.KeyEvent.KEYCODE_X;
import static android.view.KeyEvent.KEYCODE_Y;
import static android.view.KeyEvent.KEYCODE_Z;

/**
 * An ASCII key listener. Supports control characters and escape. Keeps track of
 * the current state of the alt, shift, and control keys.
 */
public class KeyListener {

    public static final int ACTION_COPY = 1;
    public static final int ACTION_CUT = 2;
    public static final int ACTION_PASTE = 3;
    public static final int ACTION_SELECT_ALL = 4;
    public static final int ACTION_RUN = 5;
    public static final int ACTION_COMPILE = 6;
    public static final int ACTION_SAVE = 7;
    public static final int ACTION_SAVE_AS = 8;
    public static final int ACTION_GOTO_LINE = 9;
    public static final int ACTION_FORMAT_CODE = 10;
    public static final int ACTION_UNDO = 11;
    public static final int ACTION_REDO = 12;
    public static final int ACTION_FIND_AND_REPLACE = 13;
    public static final int ACTION_OPEN = 14;
    public static final int ACTION_FIND = 15;

    public ModifierKey mControlKey = new ModifierKey();

    public void handleControlKey(boolean down) {
        if (down) {
            mControlKey.onPress();
        } else {
            mControlKey.onRelease();
        }
    }

    /**
     * CTRL + C copy z
     * CTRL + V paste z
     * CTRL + B: compile z
     * CTRL + R run z
     * CTRL + X cut z
     * CTRL + Z undo z
     * CTRL + Y redo z
     * CTRL + Q quit
     * CTRL + S save z
     * CTRL + O open z
     * CTRL + F find z
     * CTRL + H find and replace z
     * CTRL + L format code z
     * CTRL + G: goto line z
     */
    public int keyDown(int keyCode, KeyEvent event) {
        if (!mControlKey.isActive()) return -1;
        switch (keyCode) {
            case -99:
            case KEYCODE_CTRL_LEFT:
            case KEYCODE_CTRL_RIGHT:
                mControlKey.onPress();
                return -10;
            case KeyEvent.KEYCODE_A:
                return ACTION_SELECT_ALL;
            case KEYCODE_C:
                return ACTION_COPY;
            case KEYCODE_X:
                return ACTION_CUT;
            case KEYCODE_V:
                return ACTION_PASTE;
            case KEYCODE_B:
                return ACTION_COMPILE;
            case KEYCODE_R:
                return ACTION_RUN;
            case KEYCODE_Z:
                return ACTION_UNDO;
            case KEYCODE_Y:
                return ACTION_REDO;
            case KEYCODE_S:
                return ACTION_SAVE;
            case KEYCODE_O:
                return ACTION_OPEN;
            case KEYCODE_H:
                return ACTION_FIND_AND_REPLACE;
            case KEYCODE_G:
                return ACTION_GOTO_LINE;
            case KEYCODE_L:
                return ACTION_FORMAT_CODE;
            case KEYCODE_F:
                return ACTION_FIND;
        }
        return -1;
//        editText.getEditableText().insert(editText.getSelectionStart(), Character.toString((char) result));
    }

    /**
     * Handle a keyUp event.
     *
     * @param keyCode the keyCode of the keyUp event
     */
    public int keyUp(int keyCode) {
        switch (keyCode) {
            case -99:
            case KEYCODE_CTRL_LEFT:
            case KEYCODE_CTRL_RIGHT:
                mControlKey.onRelease();
                return keyCode;
            default:
                // Ignore other keyUps
                break;
        }
        return -1;
    }

}
