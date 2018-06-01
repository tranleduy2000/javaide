package com.duy.android.compiler.io;

import android.content.Context;

import com.android.utils.FileUtils;
import com.duy.android.compiler.env.Environment;

import java.io.File;

/**
 * Extract aar file
 */
public class AndroidLibraryExtractor {
    private Context context;

    public AndroidLibraryExtractor(Context context) {
        this.context = context;
    }

    public boolean extract(File file) {
        try {
            File libraryCachedDir = Environment.getSdCardLibraryCachedDir(context);
            String outputFolderName = removeExt(file.getName());
            File outDir = new File(libraryCachedDir, outputFolderName);
            FileUtils.emptyFolder(outDir);
            outDir.mkdirs();

            Zip.unpackZip(file, outputFolderName, libraryCachedDir);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String removeExt(String name) {
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf("."));
        } else {
            return name;
        }
    }
}
