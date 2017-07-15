/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.util;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import com.spartacusrex.spartacuside.TermService;

/**
 * The OptionDialogPreference will display a dialog, and will persist the
 * <code>true</code> when pressing the positive button and <code>false</code>
 * otherwise. It will persist to the android:key specified in xml-preference.
 */
public class dialogpref extends DialogPreference {

    Context mContext;

    public dialogpref(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

//        Log.v("DIALOGPREF", "PRESSES "+positiveResult);

        //Used to reset the Key Mappings..
        if(positiveResult){
            TermService.resetAllKeyCodes();
        }
    }

}