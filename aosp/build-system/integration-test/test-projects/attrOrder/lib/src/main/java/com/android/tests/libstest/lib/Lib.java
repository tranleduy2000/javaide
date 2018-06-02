package com.android.tests.libstest.lib;

import android.content.Context;
import android.content.res.TypedArray;

public class Lib {

    public static String getStringFromStyle(Context context){
        TypedArray array = context.obtainStyledAttributes(R.style.Example, R.styleable.StyleableExample);
        String result =  array.getString(R.styleable.StyleableExample_d_common_attr);
        array.recycle();
        return result;
    }
}
