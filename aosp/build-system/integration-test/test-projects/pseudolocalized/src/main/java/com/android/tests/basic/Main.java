package com.android.tests.basic;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.Locale;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Switch locale
        Locale locale = new Locale("en", "XA");
        Configuration config = new Configuration();
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
              getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.main);
    }
}
