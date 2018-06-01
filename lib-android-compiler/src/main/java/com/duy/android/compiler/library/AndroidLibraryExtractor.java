package com.duy.android.compiler.library;

import android.content.Context;

import com.android.utils.FileUtils;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.utils.Zip;

import java.io.File;

/**
 * Extract aar file
 */
public class AndroidLibraryExtractor {
    private Context context;

    public AndroidLibraryExtractor(Context context) {
        this.context = context;
    }

    /**
     * Extract to local repository
     *
     * @param file        - arr file
     * @param libraryName
     * @return
     */
    public boolean extract(File file, String libraryName) {
        if (!file.isFile() || !file.getName().toLowerCase().endsWith(".aar")) {
            return false;
        }

        try {
            File libraryCachedDir = Environment.getSdCardLibraryCachedDir(context);
            File outDir = new File(libraryCachedDir, libraryName);
            FileUtils.emptyFolder(outDir);

            Zip.unpackZip(file, outDir);
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
