package com.duy;

import android.support.multidex.MultiDexApplication;

import com.duy.editor.BuildConfig;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Duy on 17-Jul-17.
 */

public class JavaApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FirebaseCrash.setCrashCollectionEnabled(false);
        }
    }
}
