package com.duy.ide.editor;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FileTest extends TestCase {
    public void testGetClass() {
        File file = new File("C:/github/javaide/app/src/main/java/com");
        ArrayList<String> strings = listClassName(file);
        System.out.println(strings);
    }

    public static ArrayList<String> listClassName(File src) {
        if (!src.exists()) return new ArrayList<>();

        String[] exts = new String[]{"java"};
        Collection<File> files = FileUtils.listFiles(src, exts, true);

        ArrayList<String> classes = new ArrayList<>();
        String srcPath = src.getPath();
        for (File file : files) {
            String javaPath = file.getPath();
            javaPath = javaPath.substring(srcPath.length(), javaPath.length() - 5); //.java
            javaPath = javaPath.replace(File.separator, ".");
            classes.add(javaPath);
        }
        return classes;
    }
}
