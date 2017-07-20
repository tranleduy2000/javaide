package com.duy.compile.external.dex;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private dalvik.system.DexClassLoader dexClassLoader;

    public JavaDexClassLoader(Context context) {
        String classesPath = context.getFilesDir() + File.separator + "system/classes/android.jar";
        File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
        dexClassLoader = new DexClassLoader(classesPath, dexOutputDir.getAbsolutePath(), null,
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
