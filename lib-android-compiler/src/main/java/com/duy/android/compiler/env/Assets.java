package com.duy.android.compiler.env;


import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {
    private static final String TAG = "Assets";

    public static void copyAssets(AssetManager assets, String assetPath, File destFolder)
            throws IOException {
        String[] childs = assets.list(assetPath);
        if (childs.length == 0) {
            copyFile(assets, assetPath, destFolder);
        } else {
            copyFolder(assets, assetPath, destFolder);
        }
    }

    private static void copyFolder(AssetManager assets, String assetPath, File destFolder)
            throws IOException {
        Log.d(TAG, "copyFolder() called with: assets = [" + assets + "], assetPath = [" + assetPath + "], destFolder = [" + destFolder + "]");
        String[] names = assets.list(assetPath);
        for (String name : names) {
            copyAssets(assets, assetPath + "/" + name, destFolder);
        }
    }

    private static void copyFile(AssetManager assets, String assetPath, File destFolder)
            throws IOException {
        Log.d(TAG, "copyFile() called with: assets = [" + assets + "], assetPath = [" + assetPath + "], destFolder = [" + destFolder + "]");
        File outFile = new File(destFolder, assetPath);
        outFile.getParentFile().mkdirs();
        InputStream input = assets.open(assetPath);
        OutputStream output = new FileOutputStream(outFile);
        IOUtils.copy(input, output);
        input.close();
        output.close();
    }
}
