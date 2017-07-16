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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import java.util.Hashtable;
import java.util.List;

//public class LatinKeyboardView extends KeyboardView {
public class LatinKeyboardView extends KeyboardView {

    public static final int MODE_SMALL = 0;
    public static final int MODE_LARGE = 1;
    static final int KEYCODE_OPTIONS = -100;
    int mMode;
    boolean mFunction;
    Paint mRedPaint;
    Paint mGreenPaint;
    Paint mBluePaint;
    Paint mWhitePaint;
    Paint mYellowPaint;
    private Hashtable<String, String> mKeyTwinPair;
    private Hashtable<String, String> mModeNormKeyTwinPair;
    private Hashtable<String, String> mModeLargeKeyTwinPair;
    private Hashtable<String, String> mSymbolPair;
    private Hashtable<String, String> mModeNormSymbolPair;
    private Hashtable<String, String> mModeLargeSymbolPair;

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    public void setMode(int zMode) {
        mMode = zMode;
    }

    public void setFunction(boolean zFunc) {
        mFunction = zFunc;
    }

    private void init() {
        mKeyTwinPair = new Hashtable<String, String>(100);
        mModeNormKeyTwinPair = new Hashtable<String, String>(100);
        mModeLargeKeyTwinPair = new Hashtable<String, String>(100);
        mSymbolPair = new Hashtable<String, String>(100);
        mModeNormSymbolPair = new Hashtable<String, String>(100);
        mModeLargeSymbolPair = new Hashtable<String, String>(100);

        //Add mappings..
        mKeyTwinPair.put("1", "!");
        mKeyTwinPair.put("2", "\"");
        mKeyTwinPair.put("3", "#");
        mKeyTwinPair.put("4", "$");
        mKeyTwinPair.put("5", "%");
        mKeyTwinPair.put("6", "^");
        mKeyTwinPair.put("7", "&");
        mKeyTwinPair.put("8", "*");
        mKeyTwinPair.put("9", "(");
        mKeyTwinPair.put("0", ")");
        mKeyTwinPair.put("~", "`");
        mKeyTwinPair.put("\\", "|");

        mModeNormKeyTwinPair.put(":", "/");
        mModeNormKeyTwinPair.put(".", ",");


        mSymbolPair.put("!", "F1");
        mSymbolPair.put("\"", "F2");
        mSymbolPair.put("#", "F3");
        mSymbolPair.put("$", "F4");
        mSymbolPair.put("%", "F5");
        mSymbolPair.put("^", "F6");
        mSymbolPair.put("&", "F7");
        mSymbolPair.put("*", "F8");
        mSymbolPair.put("(", "F9");
        mSymbolPair.put(")", "F10");

        mSymbolPair.put("1", "F1");
        mSymbolPair.put("2", "F2");
        mSymbolPair.put("3", "F3");
        mSymbolPair.put("4", "F4");
        mSymbolPair.put("5", "F5");
        mSymbolPair.put("6", "F6");
        mSymbolPair.put("7", "F7");
        mSymbolPair.put("8", "F8");
        mSymbolPair.put("9", "F9");
        mSymbolPair.put("0", "F10");

        mModeNormSymbolPair.put("q", "-");
        mModeNormSymbolPair.put("w", "_");
        mModeNormSymbolPair.put("e", "<");
        mModeNormSymbolPair.put("r", ">");
        mModeNormSymbolPair.put("t", "[");
        mModeNormSymbolPair.put("y", "]");
        mModeNormSymbolPair.put("u", "{");
        mModeNormSymbolPair.put("i", "}");

        //Depends on MODE
        mModeNormSymbolPair.put("o", "F11");
        mModeNormSymbolPair.put("p", "F12");

        //LARGE MODE
        mModeLargeSymbolPair.put("-", "F11");
        mModeLargeSymbolPair.put("=", "F12");
        mModeLargeSymbolPair.put("_", "F11");
        mModeLargeSymbolPair.put("+", "F12");

        mModeLargeKeyTwinPair.put("-", "_");
        mModeLargeKeyTwinPair.put("=", "+");
        mModeLargeKeyTwinPair.put("[", "{");
        mModeLargeKeyTwinPair.put("]", "}");
        mModeLargeKeyTwinPair.put(";", ":");
        mModeLargeKeyTwinPair.put("'", "@");
        mModeLargeKeyTwinPair.put("/", "?");
        mModeLargeKeyTwinPair.put(",", "<");
        mModeLargeKeyTwinPair.put(".", ">");

        mModeNormSymbolPair.put("a", ";");
        mModeNormSymbolPair.put("s", "?");
        mModeNormSymbolPair.put("d", "@");
        mModeNormSymbolPair.put("f", "=");
        mModeNormSymbolPair.put("g", "+");
        mModeNormSymbolPair.put("h", "'");
        mModeNormSymbolPair.put("j", "--");
        mModeNormSymbolPair.put("k", "&&");
        mModeNormSymbolPair.put("l", "||");

        mModeNormSymbolPair.put("z", "\\\\");
        mModeNormSymbolPair.put("x", "//");
        mModeNormSymbolPair.put("c", "==");
        mModeNormSymbolPair.put("v", "<=");
        mModeNormSymbolPair.put("b", ">=");
        mModeNormSymbolPair.put("n", "!=");
        mModeNormSymbolPair.put("m", "++");

        //The Colors
        mRedPaint = new Paint();
        mRedPaint.setColor(Color.RED);
        mRedPaint.setAntiAlias(true);
        mGreenPaint = new Paint();
        mGreenPaint.setColor(Color.GREEN);
        mGreenPaint.setAntiAlias(true);
        mBluePaint = new Paint();
        mBluePaint.setColor(Color.BLUE);
        mBluePaint.setAntiAlias(true);
        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setAntiAlias(true);
        mYellowPaint = new Paint();
        mYellowPaint.setColor(Color.YELLOW);
        mYellowPaint.setAntiAlias(true);
    }

    /*@Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }*/

    private FPoint getTextPosition(Key zKey, boolean zLeft) {
        FPoint pp = new FPoint();

        //Left or Right side
        if (!zLeft) {
            pp.x = zKey.width - (zKey.width / 2.75f) + zKey.x + getPaddingLeft();
            pp.y = zKey.height / 3.0f + zKey.y + getPaddingTop();
        } else {
            pp.x = (zKey.width / 8.0f) + zKey.x + getPaddingLeft();
            pp.y = zKey.height / 3.0f + zKey.y + getPaddingTop();
        }

        return pp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //Set the key positions correctly - ANDROID broken ?
        setKeyPositions();

        super.onDraw(canvas);

        //get The Keyboard..
        Keyboard keyb = getKeyboard();

        if (keyb == null) return;

        //Get the keys..
        List<Key> keys = keyb.getKeys();
        Key[] allkeys = keys.toArray(new Key[keys.size()]);

        int kh = allkeys[0].height;

        //Set the Text Height
        mWhitePaint.setTextSize(kh / 5.0f);
        mYellowPaint.setTextSize(kh / 5.0f);
        mGreenPaint.setTextSize(kh / 6.0f);
        mBluePaint.setTextSize(kh / 6.0f);
        mRedPaint.setTextSize(kh / 5.5f);

        //Now cycle and final int keyCount = keys.length;
        int keyCount = allkeys.length;
        for (int i = 0; i < keyCount; i++) {
            Key key = allkeys[i];

            FPoint fpleft = getTextPosition(key, true);
            FPoint fpright = getTextPosition(key, false);

            String label = key.label == null ? null : key.label.toString();
            if (label != null && label.length() >= 1) {
                //Get the Twin key pair
                String val = mKeyTwinPair.get(label);
                if (val == null) {
                    if (mMode == MODE_SMALL) {
                        val = mModeNormKeyTwinPair.get(label);
                    } else {
                        val = mModeLargeKeyTwinPair.get(label);
                    }
                }
                if (val != null) {
                    canvas.drawText(val, fpright.x, fpright.y, mWhitePaint);
                }

                //Check Symbol Pair
                val = mSymbolPair.get(label);
                if (val == null) {
                    if (mMode == MODE_SMALL) {
                        val = mModeNormSymbolPair.get(label);
                    } else {
                        val = mModeLargeSymbolPair.get(label);
                    }
                }

                if (val != null) {
                    if (val.startsWith("F")) {
                        if (mFunction) {
                            canvas.drawText(val, fpleft.x, fpleft.y, mGreenPaint);
                        } else {
                            canvas.drawText(val, fpleft.x, fpleft.y, mRedPaint);
                        }
                    } else {
                        if (mMode == MODE_SMALL) {
                            canvas.drawText(val, fpleft.x, fpleft.y, mYellowPaint);
                        }
                    }
                }

            } else if ((key.codes[0] <= 22 && key.codes[0] >= 19) || (key.codes[0] <= -150 && key.codes[0] >= -153)) {

                Paint kcol = mBluePaint;
                if (mFunction) {
                    kcol = mGreenPaint;
                }

                //Arrow Keys
                float xstep = key.width / 6;
                float ystep = kh / 3.5f;

                if (key.codes[0] == 19 || key.codes[0] == -150) {
                    canvas.drawText("PgUp", xstep + key.x + getPaddingLeft(), ystep + key.y + getPaddingTop(), kcol);
                } else if (key.codes[0] == 21 || key.codes[0] == -152) {
                    canvas.drawText("Home", xstep + key.x + getPaddingLeft(), ystep + key.y + getPaddingTop(), kcol);
                } else if (key.codes[0] == 20 || key.codes[0] == -151) {
                    canvas.drawText("PgDn", xstep + key.x + getPaddingLeft(), ystep + key.y + getPaddingTop(), kcol);
                } else if (key.codes[0] == 22 || key.codes[0] == -153) {
                    canvas.drawText("End", xstep + key.x + getPaddingLeft(), ystep + key.y + getPaddingTop(), kcol);
                }
            }
        }
    }

    /*    public void setKeyPositions(){
            //get The Keyboard..
            Keyboard keyb = getKeyboard();


            if (keyb == null){
    //            Log.v("Spartacus","NO KEYBOARD TO MAP!!");
                return;
            }

            try {
                LatinKeyboard lkeyb = (LatinKeyboard) keyb;
                if (lkeyb.hasKeysSet()) {
    //            Log.v("Spartacus","Render Saved..!");
                    return;
                }
                lkeyb.KeysSet();
            } catch (Exception e) {
                return;
            }

            //Get the keys..
            List<Key> keys = keyb.getKeys();
            Key[] allkeys  = keys.toArray(new Key[keys.size()]);

            float width  = getWidth();
    //        Log.v("Spartacus","KEYBOARD WIDTH : "+width);

            float fifteenth  = width / (100.0f / 15.0f);
            float fourteenth = width / (100.0f / 14.0f);
            float thirt      = width / (100.0f / 13.0f);
            float twelth     = width / (100.0f / 12.0f);
            float tenth      = width / (100.0f / 10.0f);
            float ninth      = width / (100.0f / 9.0f);
            float eigth      = width / (100.0f / 8.0f);
            float seventh    = width / (100.0f / 7.0f);
            float sixth      = width / (100.0f / 6.0f);
            float fifth      = width / (100.0f / 5.0f);

            //Which Mode
            if(mMode == MODE_SMALL){
                int counter=0;
                //ROW 1
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)tenth;
                counter++;

                for(int i=0;i<10;i++){
                    allkeys[i+counter].x     = (int)(tenth + i*eigth);
                    allkeys[i+counter].width = (int)eigth;
                }
                counter+=10;

                allkeys[counter].x     = (int)(width - tenth);
                allkeys[counter].width = (int)tenth;
                counter++;

                //ROW 2
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)tenth;
                counter++;

                for(int i=0;i<10;i++){
                    allkeys[i+counter].x     = (int)(tenth + i*eigth);
                    allkeys[i+counter].width = (int)eigth;
                }
                counter+=10;

                allkeys[counter].x     = (int)(width - tenth);
                allkeys[counter].width = (int)tenth;
                counter++;

                //ROW 3
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)fourteenth;
                counter++;

                for(int i=0;i<9;i++){
                    allkeys[i+counter].x     = (int)(fourteenth + i*eigth);
                    allkeys[i+counter].width = (int)eigth;
                }
                counter+=9;

                allkeys[counter].x     = (int)(width - fourteenth);
                allkeys[counter].width = (int)fourteenth;
                counter++;

                //ROW 4
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)fourteenth;
                counter++;

                for(int i=0;i<9;i++){
                    allkeys[i+counter].x     = (int)(fourteenth + i*eigth);
                    allkeys[i+counter].width = (int)eigth;
                }
                counter+=9;

                allkeys[counter].x     = (int)(width - fourteenth);
                allkeys[counter].width = (int)fourteenth;
                counter++;

                //ROW 5
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)fifteenth;
                counter++;
                float pos = fifteenth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)fifteenth;
                counter++;
                pos += fifteenth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                //Space bar
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)(eigth*3);
                counter++;
                pos += eigth*3;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                //Arrow Keys
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                //Setup Key
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)sixth;

            }else{
                int counter=0;

                //ROW 1
                allkeys[counter].x      = 0;
                allkeys[counter].width  = (int)ninth;
                float pos = ninth;
                counter++;

                for(int i=0;i<10;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)seventh;
                    pos += seventh;
                }
                counter+=10;

                for(int i=0;i<2;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)sixth;
                    pos += sixth;
                }
                counter+=2;

                allkeys[counter].x     = (int)(width - ninth);
                allkeys[counter].width = (int)ninth;
                counter++;

                //ROW 2
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)ninth;
                pos = ninth;
                counter++;

                for(int i=0;i<10;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)seventh;
                    pos += seventh;
                }
                counter+=10;

                for(int i=0;i<2;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)sixth;
                    pos += sixth;
                }
                counter+=2;

                allkeys[counter].x     = (int)(width - ninth);
                allkeys[counter].width = (int)ninth;
                counter++;

                //ROW 3
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)twelth;
                pos = twelth;
                counter++;

                for(int i=0;i<9;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)seventh;
                    pos += seventh;
                }
                counter+=9;

                for(int i=0;i<2;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)sixth;
                    pos += sixth;
                }
                counter+=2;

                allkeys[counter].x     = (int)(width - thirt);
                allkeys[counter].width = (int)thirt;
                counter++;

                //ROW 4
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)twelth;
                pos = twelth;
                counter++;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)sixth;
                pos += sixth;
                counter++;

                for(int i=0;i<7;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)seventh;
                    pos += seventh;
                }
                counter+=7;

                for(int i=0;i<2;i++){
                    allkeys[i+counter].x     = (int)(pos);
                    allkeys[i+counter].width = (int)sixth;
                    pos += sixth;
                }
                counter+=2;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                pos += eigth;
                counter++;

                allkeys[counter].x     = (int)(width - thirt);
                allkeys[counter].width = (int)thirt;
                counter++;

                //ROW 5
                allkeys[counter].x     = 0;
                allkeys[counter].width = (int)thirt;
                counter++;
                pos = thirt;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)twelth;
                counter++;
                pos += twelth;

                //Space bar
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)(tenth*4);
                counter++;
                pos += tenth*4;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)sixth;
                counter++;
                pos += sixth;

                //Arrow Keys
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)eigth;
                counter++;
                pos += eigth;

                //Setup Key
                allkeys[counter].x     = (int)pos;
                allkeys[counter].width = (int)fifth;
            }

        }
     */
    public void setKeyPositions() {
        //get The Keyboard..
        Keyboard keyb = getKeyboard();

        if (keyb == null) {
//            Log.v("Spartacus","NO KEYBOARD TO MAP!!");
            return;
        }

        try {
            LatinKeyboard lkeyb = (LatinKeyboard) keyb;
            if (lkeyb.hasKeysSet()) {
//            Log.v("Spartacus","Render Saved..!");
                return;
            }
            lkeyb.KeysSet();
        } catch (Exception e) {
            return;
        }

        //Get the keys..
        List<Key> keys = keyb.getKeys();
        Key[] allkeys = keys.toArray(new Key[keys.size()]);

        float width = getWidth();
//        Log.v("Spartacus","KEYBOARD WIDTH : "+width);

        float thirtytwo = width / (100.0f / 32.0f);
        float fifteenth = width / (100.0f / 15.0f);
        float fourteenth = width / (100.0f / 14.0f);
        float thirt = width / (100.0f / 13.0f);
        float twelth = width / (100.0f / 12.0f);
        float eleventh = width / (100.0f / 11.0f);
        float tenth = width / (100.0f / 10.0f);
        float ninth = width / (100.0f / 9.0f);
        float eigth = width / (100.0f / 8.0f);
        float seventh = width / (100.0f / 7.0f);
        float sixth = width / (100.0f / 6.0f);
        float fifth = width / (100.0f / 5.0f);

        //Which Mode
        if (mMode == MODE_SMALL) {
            int counter = 0;
            //ROW 1
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) tenth;
            counter++;

            for (int i = 0; i < 10; i++) {
                allkeys[i + counter].x = (int) (tenth + i * eigth);
                allkeys[i + counter].width = (int) eigth;
            }
            counter += 10;

            allkeys[counter].x = (int) (width - tenth);
            allkeys[counter].width = (int) tenth;
            counter++;

            //ROW 2
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) tenth;
            counter++;

            for (int i = 0; i < 10; i++) {
                allkeys[i + counter].x = (int) (tenth + i * eigth);
                allkeys[i + counter].width = (int) eigth;
            }
            counter += 10;

            allkeys[counter].x = (int) (width - tenth);
            allkeys[counter].width = (int) tenth;
            counter++;

            //ROW 3
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) fourteenth;
            counter++;

            for (int i = 0; i < 9; i++) {
                allkeys[i + counter].x = (int) (fourteenth + i * eigth);
                allkeys[i + counter].width = (int) eigth;
            }
            counter += 9;

            allkeys[counter].x = (int) (width - fourteenth);
            allkeys[counter].width = (int) fourteenth;
            counter++;

            //ROW 4
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) fourteenth;
            counter++;

            for (int i = 0; i < 9; i++) {
                allkeys[i + counter].x = (int) (fourteenth + i * eigth);
                allkeys[i + counter].width = (int) eigth;
            }
            counter += 9;

            allkeys[counter].x = (int) (width - fourteenth);
            allkeys[counter].width = (int) fourteenth;
            counter++;

            //ROW 5
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) fifteenth;
            counter++;
            float pos = fifteenth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) fifteenth;
            counter++;
            pos += fifteenth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            //Space bar
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) (eigth * 3);
            counter++;
            pos += eigth * 3;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            //Arrow Keys
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            //Setup Key
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) sixth;

        } else {
            int counter = 0;
            float pos = 0;

            //ROW 1
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) sixth;
            counter++;
            pos += sixth;

            for (int i = 0; i < 10; i++) {
                allkeys[counter].x = (int) (pos);
                allkeys[counter].width = (int) seventh;
                pos += seventh;
                counter++;
            }

            for (int i = 0; i < 2; i++) {
                allkeys[counter].x = (int) (pos);
                allkeys[counter].width = (int) sixth;
                pos += sixth;
                counter++;
            }

            allkeys[counter].x = (int) (width - twelth);
            allkeys[counter].width = (int) twelth;
            counter++;

            //ROW 2
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) ninth;
            pos = ninth;
            counter++;

            for (int i = 0; i < 10; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) seventh;
                pos += seventh;
            }
            counter += 10;

            for (int i = 0; i < 2; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) sixth;
                pos += sixth;
            }
            counter += 2;

            allkeys[counter].x = (int) (width - ninth);
            allkeys[counter].width = (int) ninth;
            counter++;

            //ROW 3
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) twelth;
            pos = twelth;
            counter++;

            for (int i = 0; i < 9; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) seventh;
                pos += seventh;
            }
            counter += 9;

            for (int i = 0; i < 2; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) sixth;
                pos += sixth;
            }
            counter += 2;

            allkeys[counter].x = (int) (width - thirt);
            allkeys[counter].width = (int) thirt;
            counter++;

            //ROW 4
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) twelth;
            pos = twelth;
            counter++;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) sixth;
            pos += sixth;
            counter++;

            for (int i = 0; i < 7; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) seventh;
                pos += seventh;
            }
            counter += 7;

            for (int i = 0; i < 2; i++) {
                allkeys[i + counter].x = (int) (pos);
                allkeys[i + counter].width = (int) sixth;
                pos += sixth;
            }
            counter += 2;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            pos += eigth;
            counter++;

            allkeys[counter].x = (int) (width - thirt);
            allkeys[counter].width = (int) thirt;
            counter++;

            //ROW 5
            allkeys[counter].x = 0;
            allkeys[counter].width = (int) eleventh;
            counter++;
            pos = eleventh;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eleventh;
            counter++;
            pos += eleventh;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eleventh;
            counter++;
            pos += eleventh;

            //Space bar
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) (thirtytwo);
            counter++;
            pos += thirtytwo;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) sixth;
            counter++;
            pos += sixth;

            //Arrow Keys
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) eigth;
            counter++;
            pos += eigth;

            //Setup Key
            allkeys[counter].x = (int) pos;
            allkeys[counter].width = (int) fifth;
        }

    }

    class FPoint {
        public float x = 0;
        public float y = 0;

        public FPoint() {
        }
    }
}
