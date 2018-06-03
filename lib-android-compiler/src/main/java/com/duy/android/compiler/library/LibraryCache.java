package com.duy.android.compiler.library;

import android.content.Context;

import com.android.utils.FileUtils;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.utils.Zip;

import java.io.File;
import java.io.FileFilter;

import static com.android.SdkConstants.FD_JARS;

/**
 * Extract aar file
 */
public class LibraryCache {
    private Context context;

    public LibraryCache(Context context) {
        this.context = context;
    }

    /**
     * Extract to local repository
     */
    public boolean extractAar(File bundle, File folderOut) {
        if (!bundle.isFile() || !bundle.getName().toLowerCase().endsWith(".aar")) {
            return false;
        }

        try {
            FileUtils.deleteFolder(folderOut);
            folderOut.mkdirs();

            Zip.unpackZip(bundle, folderOut);
            File[] jarsFile = folderOut.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".jar");
                }
            });

            File jarFolder = new File(folderOut, FD_JARS);
            jarFolder.mkdirs();

            for (File file : jarsFile) {
                file.renameTo(new File(jarFolder, file.getName()));
            }

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
