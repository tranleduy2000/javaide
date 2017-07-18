package com.duy;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by duy on 18/07/2017.
 */

public class JavaFormat {
    public static void main(String[] args) throws IOException {
        File f = new File("/home/duy/StudioProjects/javaide/sample/src/main/java/com/duy/JavaFormat.java");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
        StringBuilder sourceString = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sourceString.append(line);
        }
        try {
            String formattedSource = new Formatter().formatSource(sourceString.toString());
            System.out.println(formattedSource);
        } catch (FormatterException e) {
            e.printStackTrace();
        }
    }
}
