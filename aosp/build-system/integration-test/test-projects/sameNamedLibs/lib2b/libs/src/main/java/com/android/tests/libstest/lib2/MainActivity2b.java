package com.android.tests.libstest.lib2;

import com.android.tests.libstest.lib2b.R;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity2b extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib2b_main);
        
        Lib2b.handleTextView(this);
    }
}
