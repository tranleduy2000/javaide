package com.duy.ide.activities;


import android.content.Context;

import java.io.File;

public class Environment {
    private static final String APP_NAME = "JavaNIDE";

    public static File getBinDir(Context context) {
        File dir = new File(getRootDir(context), "bin");
        return mkdirsIfNotExist(dir);
    }

    public static File getRootDir(Context context) {
        File root = new File(context.getFilesDir(), APP_NAME);
        return mkdirsIfNotExist(root);
    }

    private static File mkdirsIfNotExist(File f) {
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }
}
