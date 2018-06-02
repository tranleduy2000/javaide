package com.android.tests.libstest.lib1;

import com.android.tests.libstest.lib2.Lib2;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib1_main);
        
        Lib1.handleTextView(this);
        Lib2.handleTextView(this);
    }
}
