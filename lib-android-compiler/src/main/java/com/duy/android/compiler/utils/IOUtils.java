package com.duy.android.compiler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static void copyNotIfExistAndClose(InputStream input, File outputFile) throws IOException {
        if (outputFile.exists()) {
            return;
        }
        outputFile.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(outputFile);
        org.apache.commons.io.IOUtils.copy(input, output);
        output.close();
        try {
            input.close();
        } catch (Exception ignored) {
        }
    }

    public static String toStringAndClose(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        String s = org.apache.commons.io.IOUtils.toString(input);
        input.close();
        return s;
    }

    public static void writeAndClose(String content, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        org.apache.commons.io.IOUtils.write(content, output);
        output.close();
    }
}
