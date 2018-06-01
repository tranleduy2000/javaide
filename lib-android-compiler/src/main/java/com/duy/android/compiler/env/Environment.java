package com.duy.android.compiler.env;


import android.content.Context;

import java.io.File;
import java.io.IOException;

public class Environment {
    private static final String APP_NAME = "JavaNIDE";
    private static final int ANDROID_API = 27;

    public static void install(Context context) throws IOException {
        Assets.copyAssets(context.getAssets(), "sdk", getRootDir(context));
        Assets.copyAssets(context.getAssets(), "bin", getRootDir(context));
        File[] binFiles = getBinDir(context).listFiles();
        for (File binFile : binFiles) {
            binFile.setExecutable(true, true);
        }
    }

    public static File getBinDir(Context context) {
        File dir = new File(getRootDir(context), "bin");
        return mkdirsIfNotExist(dir);
    }

    public static File getRootDir(Context context) {
        return context.getFilesDir();
    }

    private static File mkdirsIfNotExist(File f) {
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static File getSdkDir(Context context) {
        File sdkDir = new File(getRootDir(context), "sdk");
        return mkdirsIfNotExist(sdkDir);
    }

    public static File getPlatformDir(Context context) {
        File dir = new File(getSdkDir(context), "platforms");
        return mkdirsIfNotExist(dir);
    }

    public static File getPlatformApiDir(Context context, int api) {
        File dir = new File(getPlatformDir(context), "android-" + api);
        return mkdirsIfNotExist(dir);
    }

    public static File getClasspathFile(Context context) {
        return new File(getPlatformDir(context), "android-" + ANDROID_API + "/android.jar");
    }

    public static File getSdCardLibraryCachedDir(Context context) {
        File dir = new File(getSdkAppDir(), ".cached");
        return mkdirsIfNotExist(dir);
    }

    public static File getSdkAppDir() {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), APP_NAME);
        return mkdirsIfNotExist(dir);
    }

    public static File getLocalRepositoryDir(Context context) {
        File dir = new File(getSdkDir(context), ".repo");
        return mkdirsIfNotExist(dir);
    }
}
