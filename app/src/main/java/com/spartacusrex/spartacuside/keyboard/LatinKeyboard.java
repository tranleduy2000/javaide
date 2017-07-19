/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spartacusrex.spartacuside.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.duy.ide.R;

public class LatinKeyboard extends Keyboard {

    private static int[] NORMAL_ENTER = {-743};
    private static int[] TERMINAL_ENTER = {13};
    boolean mKeysPositionSet = false;
    private Key mEnterKey;
    private Key mCtrlKey;
    private Key mALTKey;
    private Key mShiftKeyLeft;
    private Key mShiftKeyRight;
    private Key mFNKey;

    public LatinKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public LatinKeyboard(Context context, int layoutTemplateResId,
                         CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    public boolean hasKeysSet() {
        return mKeysPositionSet;
    }

    public void KeysSet() {
        mKeysPositionSet = true;
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        Key key = new LatinKey(res, parent, x, y, parser);

        if (key.codes[0] == 13) {
            mEnterKey = key;
        } else if (key.codes[0] == TerminalKeyboard.CTRL_KEY) {
            mCtrlKey = key;
        } else if (key.codes[0] == TerminalKeyboard.ALT_KEY) {
            mALTKey = key;
        } else if (key.codes[0] == -1) {
            mShiftKeyLeft = key;
        } else if (key.codes[0] == -999) {
            mShiftKeyRight = key;
        } else if (key.codes[0] == -2) {
            mFNKey = key;
        }

        return key;
    }

    private Key getCTRLKey() {
        return mCtrlKey;
    }

    private Key getALTKey() {
        return mALTKey;
    }

    public Key getShiftKeyLeft() {
        return mShiftKeyLeft;
    }

    public Key getShiftKeyRight() {
        return mShiftKeyRight;
    }

    private Key getFNKey() {
        return mFNKey;
    }

    public boolean setALTKeyState(boolean zOn) {
        Key key = getALTKey();
        if (key != null) {
            key.on = zOn;
            return true;
        }
        return false;
    }

    public boolean setCTRLKeyState(boolean zOn) {
        Key key = getCTRLKey();
        if (key != null) {
            key.on = zOn;
            return true;
        }
        return false;
    }

    public boolean setFNKeyState(boolean zOn) {
        Key key = getFNKey();
        if (key != null) {
            key.on = zOn;
            return true;
        }
        return false;
    }

//    public boolean setSHIFTKeyState(boolean zOn){
//        Key key = getShiftKey();
//        if(key != null){
//           key.on = zOn;
//           return true;
//        }
//        return false;
//    }

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }

        int valnorm = KeyEvent.KEYCODE_ENTER;

        switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.codes = NORMAL_ENTER;
                mEnterKey.label = res.getText(R.string.label_go_key);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.codes = NORMAL_ENTER;
                mEnterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_search);
                mEnterKey.codes = NORMAL_ENTER;
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.codes = NORMAL_ENTER;
                mEnterKey.label = res.getText(R.string.label_send_key);
                break;
            default:
                mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                mEnterKey.codes = TERMINAL_ENTER;
                break;
        }
    }

    static class LatinKey extends Keyboard.Key {

        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }

        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
//        @Override
//        public boolean isInside(int x, int y) {
//            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
//        }
    }

}
