package com.duy.editor.editor;

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
        String source = "package com.example;\n" +
                "\n" +
                "import javList;\n" +
                "\n" +
                "public class Main {public static void main(String[] args) {NumberOne numberOne = new NumberOne();numberOne.print();NumberTwo numberTwo = new NumberTwo();ArrayList<String> arrayList = new ArrayList<String>();arrayList.add(numberOne.toString());arrayList.add(numberTwo.toString());System.out.println(arrayList); for (int i = 0; i < 10; i++){}}\n" +
                "}\n";
        ASFormatter formatter = new ASFormatter();

        // bug on lib's implementation. reported here: http://barenka.blogspot.com/2009/10/source-code-formatter-library-for-java.html
        source = source.replace("{", "{\n");

        Reader in = new BufferedReader(new StringReader(source));
        formatter.setJavaStyle();

        String format = FormatterHelper.format(in, formatter);
        System.out.println(format);
        in.close();
    }

    public void test1(){

    }
}