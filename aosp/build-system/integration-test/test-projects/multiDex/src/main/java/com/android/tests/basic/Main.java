package com.android.tests.basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // use reflection since the jar project is in the packaging scope but not
        // the compile scope.
        try {
            for (int i = 1 ; i <= 70 ; i++) {
                Class<?> clazz = getClassLoader().loadClass(String.format("com.android.tests.basic.manymethods.Big%03d", i));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        TextView tv = (TextView) findViewById(R.id.text);
        tv.setText("Found all classes");
    }
}
