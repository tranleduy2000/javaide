/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.util;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;

/**
 *
 * @author Spartacus Rex
 */
public class hardkeymappings {

    public int TOTAL_HARDMAP_NUMBER = 21;

    public static final int HARDKEY_CTRL_LEFT  = 0;
    public static final int HARDKEY_CTRL_RIGHT = 1;
    public static final int HARDKEY_ALT_LEFT   = 2;
    public static final int HARDKEY_ALT_RIGHT  = 3;
    public static final int HARDKEY_ESCAPE     = 4;
    public static final int HARDKEY_FUNCTION   = 5;

    public static final int HARDKEY_TAB         = 6;
    public static final int HARDKEY_LSHIFT      = 7;
    public static final int HARDKEY_RSHIFT      = 8;
    public static final int HARDKEY_SPACE       = 9;
    public static final int HARDKEY_ENTER       = 10;
    public static final int HARDKEY_DELETE      = 11;
    public static final int HARDKEY_BACKSPACE   = 12;

    public static final int HARDKEY_UP      = 13;
    public static final int HARDKEY_DOWN    = 14;
    public static final int HARDKEY_LEFT    = 15;
    public static final int HARDKEY_RIGHT   = 16;

    public static final int HARDKEY_PGUP    = 17;
    public static final int HARDKEY_PGDOWN  = 18;
    public static final int HARDKEY_HOME    = 19;
    public static final int HARDKEY_END     = 20;
    
//    public static final int HARDKEY_EXCLAMATION     = 21;
//    public static final int HARDKEY_AMPERSAND       = 22;
//    public static final int HARDKEY_HASH            = 23;
//    public static final int HARDKEY_DOLLAR          = 24;
//    public static final int HARDKEY_PERCENT         = 25;

    int[] mKeyMappings;
    boolean mEnabled = false;

    SharedPreferences mPrefs;

    private int getStringPref(SharedPreferences zPrefs, String zKey, String zDefault){
        int ival = -1;
        try {
            String value = zPrefs.getString(zKey, zDefault);
            ival = Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
        return ival;
    }

    public hardkeymappings(){
        mKeyMappings = new int[TOTAL_HARDMAP_NUMBER];
        for(int i=0;i<TOTAL_HARDMAP_NUMBER;i++){
            mKeyMappings[i] = -1;
        }
    }

    public boolean isEnabled(){
        return mEnabled;
    }

    public void resetAllMappings(){
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putString("hardmap_ctrlLeft", "-1");
        editor.putString("hardmap_ctrlRight", "-1");
        editor.putString("hardmap_altLeft", "-1");
        editor.putString("hardmap_altRight", "-1");
        editor.putString("hardmap_escape", "-1");
        editor.putString("hardmap_function", "-1");

        editor.putString("hardmap_tab", "-1");
        editor.putString("hardmap_leftshift", "-1");
        editor.putString("hardmap_rightshift", "-1");
        editor.putString("hardmap_space", "-1");
        editor.putString("hardmap_enter", "-1");
        editor.putString("hardmap_delete", "-1");
        editor.putString("hardmap_backspace", "-1");

        editor.putString("hardmap_up", "-1");
        editor.putString("hardmap_down", "-1");
        editor.putString("hardmap_left", "-1");
        editor.putString("hardmap_right", "-1");

        editor.putString("hardmap_pageup", "-1");
        editor.putString("hardmap_pagedown", "-1");
        editor.putString("hardmap_home", "-1");
        editor.putString("hardmap_end", "-1");

        editor.commit();
        
        //Now reset them
        for(int i=0;i<TOTAL_HARDMAP_NUMBER;i++){
            mKeyMappings[i] = -1;
        }
    }

    public void setKeyMappings(SharedPreferences zPrefs){
        mPrefs = zPrefs;

        mEnabled = ( getStringPref(zPrefs, "hardmap_enable", "0") == 1 );
        
        mKeyMappings[HARDKEY_CTRL_LEFT]     = getStringPref(zPrefs,"hardmap_ctrlLeft", "-1");
        mKeyMappings[HARDKEY_CTRL_RIGHT]    = getStringPref(zPrefs,"hardmap_ctrlRight", "-1");
        mKeyMappings[HARDKEY_ALT_LEFT]      = getStringPref(zPrefs,"hardmap_altLeft", "-1");
        mKeyMappings[HARDKEY_ALT_RIGHT]     = getStringPref(zPrefs,"hardmap_altRight", "-1");
        mKeyMappings[HARDKEY_ESCAPE]        = getStringPref(zPrefs,"hardmap_escape", "-1");
        mKeyMappings[HARDKEY_FUNCTION]      = getStringPref(zPrefs,"hardmap_function", "-1");

        mKeyMappings[HARDKEY_TAB]       = getStringPref(zPrefs,"hardmap_tab", "-1");
        mKeyMappings[HARDKEY_LSHIFT]    = getStringPref(zPrefs,"hardmap_leftshift", "-1");
        mKeyMappings[HARDKEY_RSHIFT]    = getStringPref(zPrefs,"hardmap_rightshift", "-1");
        mKeyMappings[HARDKEY_SPACE]     = getStringPref(zPrefs,"hardmap_space", "-1");
        mKeyMappings[HARDKEY_ENTER]     = getStringPref(zPrefs,"hardmap_enter", "-1");
        mKeyMappings[HARDKEY_DELETE]    = getStringPref(zPrefs,"hardmap_delete", "-1");
        mKeyMappings[HARDKEY_BACKSPACE] = getStringPref(zPrefs,"hardmap_backspace", "-1");

        mKeyMappings[HARDKEY_UP]    = getStringPref(zPrefs,"hardmap_up", "-1");
        mKeyMappings[HARDKEY_DOWN]  = getStringPref(zPrefs,"hardmap_down", "-1");
        mKeyMappings[HARDKEY_LEFT]  = getStringPref(zPrefs,"hardmap_left", "-1");
        mKeyMappings[HARDKEY_RIGHT] = getStringPref(zPrefs,"hardmap_right", "-1");

        mKeyMappings[HARDKEY_PGUP]   = getStringPref(zPrefs,"hardmap_pageup", "-1");
        mKeyMappings[HARDKEY_PGDOWN] = getStringPref(zPrefs,"hardmap_pagedown", "-1");
        mKeyMappings[HARDKEY_HOME]   = getStringPref(zPrefs,"hardmap_home", "-1");
        mKeyMappings[HARDKEY_END]    = getStringPref(zPrefs,"hardmap_end", "-1");
    }

    public int checkKeyCode(int zKeyCode){
        //Cycle through and check
        for(int i=0;i<TOTAL_HARDMAP_NUMBER;i++){
            if(mKeyMappings[i] == zKeyCode){
                return i;
            }
        }

        return -1;
    }

}
