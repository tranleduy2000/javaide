/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Spartacus Rex
 */
public class FileManager {


    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void extractAsset(Context zContext, String zAssetFile, File zOuput) throws IOException {
        InputStream in = zContext.getAssets().open(zAssetFile);
        OutputStream os = new FileOutputStream(zOuput);
        copyFile(in, os);
        in.close();
        os.close();
    }

    public static void deleteFolder(File zFile) {
        if (zFile.isDirectory()) {
            //Its a directory
            File[] files = zFile.listFiles();
            for (File ff : files) {
                deleteFolder(ff);
            }
        }
        //Now delete
        zFile.delete();
    }
}
