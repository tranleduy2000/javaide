/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.keyboard;

import android.content.Context;
import android.util.Log;

import com.duy.ide.R;

/**
 * @author Spartacus Rex
 */
public class KeyboardSwitcher {

    public static final int KEYBOARD_NORM = 0;
    public static final int KEYBOARD_LARGE = 1;
    public static final int KEYBOARD_FUNCTION = 2;
    public static final int KEYBOARD_FUNCTION_LARGE = 3;

    public static final int NOT_SHIFTED = 0;
    public static final int SHIFTED = 1;

    public static int KEYBOARD_SIZES = 4;

    //All the different Keyboards.. Small and Large size
    LatinKeyboard[][][] mKeyboards;

    int mKeyboard;
    int mSize;
    int mShift;
    boolean mFunction;

    //The META Key states
    boolean mShiftLocked;
    boolean mCTRL;
    boolean mALT;

    public KeyboardSwitcher() {

        //Set Default
        mKeyboard = KEYBOARD_NORM;
        mSize = 0;

        mShift = NOT_SHIFTED;
        mShiftLocked = false;
        mCTRL = false;
        mALT = false;

        mKeyboards = null;
    }

    private void log(String zLog) {
        Log.v("SpartacusRex", zLog);
    }

    public boolean isInited() {
        if (mKeyboards == null) {
            return false;
        }
        return true;
    }

    public void init(Context zContext) {
        //Create all the Keyboards
        //TYPE / SHIFTED / SIZE
        mKeyboards = new LatinKeyboard[4][2][KEYBOARD_SIZES];

        //WHY IS THIS DONE LIKE THIS ? Because you can only specify the height in the XML..

        //Smaller 30dip size
        mKeyboards[KEYBOARD_NORM][NOT_SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty30);
        mKeyboards[KEYBOARD_NORM][SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_shift30);
        mKeyboards[KEYBOARD_NORM][SHIFTED][0].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION][NOT_SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_function30);
        mKeyboards[KEYBOARD_FUNCTION][SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_shift_function30);
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_large30);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift30);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][0].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_large_function30);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][0] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift_function30);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][0].setShifted(true);

        //Normal 40dip size
        mKeyboards[KEYBOARD_NORM][NOT_SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty);
        mKeyboards[KEYBOARD_NORM][SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_shift);
        mKeyboards[KEYBOARD_NORM][SHIFTED][1].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION][NOT_SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_function);
        mKeyboards[KEYBOARD_FUNCTION][SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_shift_function);
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_large);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][1].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_large_function);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][1] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift_function);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][1].setShifted(true);

        //Larger 50dip size
        mKeyboards[KEYBOARD_NORM][NOT_SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty50);
        mKeyboards[KEYBOARD_NORM][SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_shift50);
        mKeyboards[KEYBOARD_NORM][SHIFTED][2].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION][NOT_SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_function50);
        mKeyboards[KEYBOARD_FUNCTION][SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_shift_function50);
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_large50);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift50);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][2].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_large_function50);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][2] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift_function50);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][2].setShifted(true);

        //Largest 65dip size
        mKeyboards[KEYBOARD_NORM][NOT_SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty60);
        mKeyboards[KEYBOARD_NORM][SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_shift60);
        mKeyboards[KEYBOARD_NORM][SHIFTED][3].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION][NOT_SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_function60);
        mKeyboards[KEYBOARD_FUNCTION][SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_shift_function60);
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_large60);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift60);
        mKeyboards[KEYBOARD_LARGE][SHIFTED][3].setShifted(true);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_large_function60);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][3] = new LatinKeyboard(zContext, R.xml.qwerty_large_shift_function60);
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][3].setShifted(true);

        //Set the default keyboard
        setKeyboard(0, NOT_SHIFTED, 0);
    }

    public void setKeyboardSize(int zSize) {
        if (zSize < KEYBOARD_SIZES) {
            setKeyboard(mKeyboard, mShift, zSize);
        }
    }

    public int getKeyboardType() {
        return mKeyboard;
    }

    public void setKeyboardType(int zType) {
        setKeyboard(zType, mShift, mSize);
    }

    public void setKeyboard(int zType, int zShift, int zSize) {
        mKeyboard = zType;
        mSize = zSize;
        mShift = zShift;

        if (!isShifted()) {
            //Turn off
            mShiftLocked = false;
        }
    }

    public LatinKeyboard getCurrentKeyboard() {
        //The Keyboard
        LatinKeyboard currentKeyboard = mKeyboards[mKeyboard][mShift][mSize];

        //Is Function Pressed
        if (mFunction && mKeyboard == KEYBOARD_NORM) {
            currentKeyboard = mKeyboards[KEYBOARD_FUNCTION][mShift][mSize];
            currentKeyboard.setFNKeyState(true);

        } else if (mFunction && mKeyboard == KEYBOARD_LARGE) {
            currentKeyboard = mKeyboards[KEYBOARD_FUNCTION_LARGE][mShift][mSize];
            currentKeyboard.setFNKeyState(true);

        } else {
            currentKeyboard.setFNKeyState(false);
        }

        //Make sure meta keys set
        currentKeyboard.setCTRLKeyState(mCTRL);
        currentKeyboard.setALTKeyState(mALT);

        //Shift Keys
        setShiftKeys();

        return currentKeyboard;
    }

    //ShiftKey
    public void shiftKey() {
        if (!isShifted()) {
            //Never Shifts
            mShiftLocked = false;

            //Change to Shift Keyboard
            setKeyboard(mKeyboard, SHIFTED, mSize);

        } else {
            if (mShiftLocked) {
                //Change to Shift Keyboard
                setKeyboard(mKeyboard, NOT_SHIFTED, mSize);

            } else {
                mShiftLocked = true;
            }
        }

        setShiftKeys();
    }

    //ShiftKey
    public void FNKey() {
        if (!mFunction) {
            mFunction = true;
        } else {
            //Back to Normal
            mFunction = false;
        }
    }

    public boolean isFunction() {
        return mFunction;
    }

    private void setShiftKeys() {
        //Shift Keys
        mKeyboards[KEYBOARD_NORM][NOT_SHIFTED][mSize].getShiftKeyLeft().on = false;
        mKeyboards[KEYBOARD_NORM][SHIFTED][mSize].getShiftKeyLeft().on = mShiftLocked;
        mKeyboards[KEYBOARD_FUNCTION][NOT_SHIFTED][mSize].getShiftKeyLeft().on = false;
        mKeyboards[KEYBOARD_FUNCTION][SHIFTED][mSize].getShiftKeyLeft().on = mShiftLocked;
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][mSize].getShiftKeyLeft().on = false;
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][mSize].getShiftKeyLeft().on = mShiftLocked;
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][mSize].getShiftKeyLeft().on = false;
        mKeyboards[KEYBOARD_LARGE][SHIFTED][mSize].getShiftKeyLeft().on = mShiftLocked;

        //Only on the large keyboard
        mKeyboards[KEYBOARD_LARGE][NOT_SHIFTED][mSize].getShiftKeyRight().on = false;
        mKeyboards[KEYBOARD_LARGE][SHIFTED][mSize].getShiftKeyRight().on = mShiftLocked;
        mKeyboards[KEYBOARD_FUNCTION_LARGE][NOT_SHIFTED][mSize].getShiftKeyRight().on = false;
        mKeyboards[KEYBOARD_FUNCTION_LARGE][SHIFTED][mSize].getShiftKeyRight().on = mShiftLocked;
    }

    //Do we switch
    public boolean validKeyPress() {
        if (isShifted()) {
            if (!mShiftLocked) {
                //Change to normal Keyboard
                setKeyboard(mKeyboard, NOT_SHIFTED, mSize);
                return true;
            }
        }

        return false;
    }

    //META Keys
    public boolean isCTRL() {
        return mCTRL;
    }

    public void setCTRL(boolean zCTRL) {
        mCTRL = zCTRL;
        getCurrentKeyboard().setCTRLKeyState(zCTRL);
    }

    public boolean isALT() {
        return mALT;
    }

    public void setALT(boolean zALT) {
        mALT = zALT;
        getCurrentKeyboard().setALTKeyState(zALT);
    }

    public boolean isShifted() {
        return (mShift == SHIFTED);
    }
}
