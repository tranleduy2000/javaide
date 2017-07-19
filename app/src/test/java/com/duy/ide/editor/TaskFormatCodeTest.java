package com.duy.ide.editor;

import junit.framework.TestCase;

import net.barenca.jastyle.ASFormatter;
import net.barenca.jastyle.FormatterHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by Duy on 16-Jul-17.
 */
public class TaskFormatCodeTest extends TestCase {


    public void test3() throws IOException {
        String source = "{\n{\n{\n}\n}\n}\n";
        ASFormatter formatter = new ASFormatter();
        formatter.setBreakOneLineBlocksMode(true);
        formatter.setBreakBlocksMode(true);
        formatter.setBreakClosingHeaderBlocksMode(true);
        formatter.setJavaStyle();
        source = source + "\n";

        Reader in = new BufferedReader(new StringReader(source));

        String format = FormatterHelper.format(in, formatter);
        System.out.println(format);
        in.close();
    }

    public void test1(){

    }
}