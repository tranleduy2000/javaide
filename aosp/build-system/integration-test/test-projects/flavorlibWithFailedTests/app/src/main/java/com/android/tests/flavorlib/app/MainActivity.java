package com.android.tests.flavorlib.app;

import com.android.tests.flavorlib.lib.Lib;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        App.handleTextView(this);
        Lib.handleTextView(this);
    }
}