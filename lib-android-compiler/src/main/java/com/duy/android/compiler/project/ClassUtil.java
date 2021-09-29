package com.duy.android.compiler.project;

import com.duy.common.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;

/**
 * Created by duy on 18/07/2017.
 */

public class ClassUtil {

    private static final Pattern MAIN_FUNCTION;

    static {
        MAIN_FUNCTION = Pattern.compile("(public\\s+static\\s+void\\s+main\\s?)" + //public static void main
                        "(\\(\\s?String\\s?\\[\\s?\\]\\s?\\w+\\s?\\)|" + //String[] args
                        "(\\(\\s?String\\s+\\w+\\[\\s?\\]\\s?\\)))", //String args[]
                Pattern.DOTALL);
    }

    /**
     * fast resolve main method, no need to parse java file
     */
    public static boolean hasMainFunction(File file) {
        try {
            String s = IOUtils.toString(new FileInputStream(file));
            return hasMainFunction(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasMainFunction(String content) {
        return MAIN_FUNCTION.matcher(content).find();
    }
}
