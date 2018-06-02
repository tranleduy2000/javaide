package com.android.test.ivyapp;

import android.os.Bundle;
import android.app.Activity;
import com.foo.Foo;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Foo();
    }
}
