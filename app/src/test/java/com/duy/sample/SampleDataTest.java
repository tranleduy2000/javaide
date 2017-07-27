package com.duy.sample;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SampleDataTest extends TestCase {
    public void testCommonlyUsedJavaClasses() throws IOException, InterruptedException {
        File parent = new File("C:\\github\\javaide2\\app\\src\\main\\assets\\sample\\CommonlyUsedJavaClasses");
        Collection<File> files = FileUtils.listFiles(parent, new String[]{"java"}, true);
        for (File file : files) {
            System.out.println(file);
            Process exec = Runtime.getRuntime().exec("javac " + file.getPath());
            exec.waitFor();
            InputStream inputStream = exec.getErrorStream();
            printStream(inputStream);
        }
        files = FileUtils.listFiles(parent, new String[]{"class"}, true);
        for (File f : files) {
            f.delete();
        }
    }

    public void testJavaLanguageFundamentals() throws IOException, InterruptedException {
        File parent = new File("C:\\github\\javaide2\\app\\src\\main\\assets\\sample\\JavaLanguageFundamentals");
        Collection<File> files = FileUtils.listFiles(parent, new String[]{"java"}, true);
        for (File file : files) {
            System.out.println(file);

            Process exec = Runtime.getRuntime().exec("javac " + file.getPath());
            exec.waitFor();
            InputStream inputStream = exec.getErrorStream();
            printStream(inputStream);
        }
        files = FileUtils.listFiles(parent, new String[]{"class"}, true);
        for (File f : files) {
            f.delete();
        }
    }

    public void testWrapperClassesAndJavaLangPackage() throws IOException, InterruptedException {
        File parent = new File("C:\\github\\javaide2\\app\\src\\main\\assets\\sample\\WrapperClassesAndJavaLangPackage");
        Collection<File> files = FileUtils.listFiles(parent, new String[]{"java"}, true);
        for (File file : files) {
            System.out.println(file);

            Process exec = Runtime.getRuntime().exec("javac " + file.getPath());
            exec.waitFor();
            InputStream inputStream = exec.getErrorStream();
            printStream(inputStream);
        }
        files = FileUtils.listFiles(parent, new String[]{"class"}, true);
        for (File f : files) {
            f.delete();
        }
    }

    private void printStream(InputStream inputStream) throws IOException {
        System.out.println("SampleDataTest.printStream");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        bufferedReader.close();
    }
}
