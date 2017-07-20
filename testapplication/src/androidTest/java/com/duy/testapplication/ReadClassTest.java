package com.duy.testapplication;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.duy.testapplication.dex.JavaClassReader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

@RunWith(AndroidJUnit4.class)
public class ReadClassTest {
    private static final String TAG = "ReadClassTest";

    @Test
    public void testReadClass() {
        Context context = InstrumentationRegistry.getTargetContext();
        File classFile = new File(Environment.getExternalStorageDirectory(), "android.jar");
        ArrayList<Class> classes = JavaClassReader.readAllClassesFromJar(classFile.getPath(),
                context.getDir("dex", Context.MODE_PRIVATE).getPath());
        for (Class aClass : classes) {
            Log.d(TAG, "testReadClass: " + aClass.getName());
        }
    }
}
