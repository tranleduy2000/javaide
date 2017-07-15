/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.setup;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Spartacus Rex
 */
public class assetextract {

    AssetManager mAssets;

    public assetextract(AssetManager zAssets) {
        mAssets = zAssets;
    }

    private static void log(String zLog) {
        Log.v("SpartacusRex", "BinaryManager : " + zLog);
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

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void extractAsset(String zAssetFile, File zOuput) throws IOException {
        InputStream in = mAssets.open(zAssetFile);
        OutputStream os = new FileOutputStream(zOuput);
        copyFile(in, os);
        in.close();
        os.close();
    }

    public boolean isAssetDirectory(String zAssetPath) throws IOException {
        String[] files = mAssets.list(zAssetPath);
        if (files == null || files.length == 0) {
            return false;
        }
        return true;
    }

    private String getPathName(String zPath) {
        int index = zPath.lastIndexOf("/");
        if (index == -1) {
            return zPath;
        }
        return zPath.substring(index);
    }

    public void recurseExtractAssets(String zAssetPath, File zBaseDir) {
        log("recurseExtractAssets " + zAssetPath + " " + zBaseDir);

        //Check that this folder exists
        File folder = new File(zBaseDir.getPath());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        //Get the Asset name..
        String asset = getPathName(zAssetPath);

        //Create a file handle
        File newfile = new File(zBaseDir, asset);

        //Now - what to do..
        try {
            //Is it a Dir
            if (isAssetDirectory(zAssetPath)) {
                //Create a Directory..   
                newfile.mkdirs();

                //List the Asset files
                String[] files = mAssets.list(zAssetPath);
                for (String file : files) {
                    String fpath = zAssetPath + "/" + file;

                    //Recurse down through the tree..
                    recurseExtractAssets(fpath, newfile);
                }

            } else {
                String name = newfile.getName();
                if (name.endsWith(".mp3")) {
                    //Big file - copy and remove .mp3 from filename
                    newfile = new File(zBaseDir, asset.substring(0, asset.length() - 4));
                }

                //Now extract the file..
                extractAsset(zAssetPath, newfile);
            }

        } catch (IOException ex) {
            Logger.getLogger(assetextract.class.getName()).log(Level.SEVERE, null, zAssetPath + " " + ex);
        }
    }

}
