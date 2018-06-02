package com.android.tests.libstest.app;

import com.android.tests.libstest.lib1.Lib1;
import com.android.tests.libstest.lib2.Lib2;
import com.android.tests.libstest.lib2.Lib2b;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        App.handleTextView(this);
        Lib1.handleTextView(this);
        Lib2.handleTextView(this);
        Lib2b.handleTextView(this);
        LibApp.handleTextView(this);
    }
}