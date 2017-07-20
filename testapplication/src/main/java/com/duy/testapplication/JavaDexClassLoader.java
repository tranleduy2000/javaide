package com.duy.testapplication;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private DexClassLoader dexClassLoader;

    public JavaDexClassLoader(Context context) {
        File classes = new File(Environment.getExternalStorageDirectory(), "android.jar");
        File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
        dexClassLoader = new DexClassLoader(classes.getAbsolutePath(),
                dexOutputDir.getAbsolutePath(), null,
                ClassLoader.getSystemClassLoader());
    }

    @Nullable
    public Class loadClass(String name) {
        try {
            return dexClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
