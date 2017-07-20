package com.duy.testapplication;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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
    @Test
    public void testReadClass() {
        Context context = InstrumentationRegistry.getTargetContext();
        File classFile = new File(context.getFilesDir(), "system/classes/android.jar");
        ArrayList<Class> classes = JavaClassReader.readAllClassesFromJar(classFile.getPath());
        for (Class aClass : classes) {
            System.out.println(aClass.getName());
        }
    }
}
