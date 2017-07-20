package com.duy.testapplication.dex;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */
public class JavaClassReaderTest extends TestCase {

    public void testreadAllClassesFromJar() throws IOException {
        ArrayList<Class> x = JavaClassReader.readAllClassesFromJar("C:\\github\\javaide2\\system\\system\\classes\\android.jar");
        System.out.println(x);
        System.out.println(x.size());
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\github\\javaide2\\system\\classes.txt"));
        for (Class aClass : x) {
            writer.write(aClass.getName() + "\n");
        }
        writer.close();
    }
}