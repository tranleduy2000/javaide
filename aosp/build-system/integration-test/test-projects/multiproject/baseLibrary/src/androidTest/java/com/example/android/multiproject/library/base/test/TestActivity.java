package com.example.android.multiproject.library.base.test;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testlayout);
    }
}
