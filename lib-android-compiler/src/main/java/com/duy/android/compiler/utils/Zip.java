package com.duy.android.compiler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {

    public static boolean unpackZip(File zipFile, File destFolder) {

        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            String filename;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(destFolder, filename);
                    fmd.mkdirs();
                    continue;
                }

                File outFile = new File(destFolder, filename);
                System.out.println("out = " + outFile);
                FileOutputStream fout = new FileOutputStream(outFile);

                // cteni zipu a zapis
                while ((count = zipInputStream.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zipInputStream.closeEntry();
            }

            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
