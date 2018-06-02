package com.android.tests.libstest.lib.test;

import com.android.tests.libstest.lib.Lib;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(Lib.getStringFromStyle(this));
    }
}
