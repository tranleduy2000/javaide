package com.pluscubed.logcat;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }
}
