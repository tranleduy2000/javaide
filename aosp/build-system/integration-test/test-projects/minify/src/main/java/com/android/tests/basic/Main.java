package com.android.tests.basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.Exception;
import java.lang.RuntimeException;

public class Main extends Activity {

    private int foo = 1234;

    private final StringProvider mStringProvider = new StringProvider();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView tv = (TextView) findViewById(R.id.dateText);
        tv.setText(getStringProvider().getString(foo) + "," + useIndirectReference());
    }

    public StringProvider getStringProvider() {
        return mStringProvider;
    }

    public String useIndirectReference() {
        try {
            Class cls = Class.forName(getClass().getPackage().getName() + ".IndirectlyReferencedClass");
            Object o = cls.newInstance();
            return o.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
