package com.duy.testapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.reflect.Constructor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private JavaDexClassLoader mClassLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            return;
        }
        mClassLoader = new JavaDexClassLoader(this);
        testLoadClass();

    }

    private void testLoadClass() {
        Class aClass = mClassLoader.loadClass("java.util.ArrayList");
        Log.d(TAG, "testLoadClass: " + aClass);
        if (aClass != null) {
            for (Constructor constructor : aClass.getConstructors()) {
                Log.d(TAG, "testLoadClass constructor = " + constructor);
            }
        }
    }
}
