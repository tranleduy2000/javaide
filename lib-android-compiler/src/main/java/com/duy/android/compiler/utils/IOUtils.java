package com.duy.android.compiler.utils;

import java.io.File;
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
}
