package com.google.googlejavaformat.java;

import junit.framework.TestCase;

import static com.google.googlejavaformat.java.JavaFormatterOptions.Style.AOSP;

/**
 * Created by Duy on 22-Jul-17.
 */
public class FormatterTest extends TestCase {
    public void test_formatSource() throws Exception {
        String src = "package com.google.googlejavaformat.java;\n" +
                "\n" +
                "import org.junit.Test;\n" +
                "\n" +
                "import java.io.FileInputStream;\n" +
                "\n" +
                "/**\n" +
                " * Created by Duy on 22-Jul-17.\n" +
                " */\n" +
                "public class FormatterTest {\n" +
                "    @Test\n" +
                "    public void formatSource() throws Exception {\n" +
                "String input = new String();\n" +
                "new Formatter().formatSource();\n" +
                "    }\n" +
                "\n" +
                "    @Test\n" +
                "    public void formatSource1() throws Exception {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Test\n" +
                "    public void formatSource2() throws Exception {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}";
        JavaFormatterOptions.Builder builder = JavaFormatterOptions.builder();
        builder.style(AOSP);
        System.out.println(new Formatter(builder.build()).formatSource(src));
    }

    public void test2() throws FormatterException {
        String src = "package com.duy;\n" +
                "import java.util.ArrayList;\n" +
                "/** * Created by Duy on 17-Jul-17. */\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        ArrayList list = new ArrayList();\n" +
                "        for (int i = 0; i < 1000; i++) {\n" +
                "            list.add(i);\n" +
                "            \n" +
                "             \n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        JavaFormatterOptions.Builder builder = JavaFormatterOptions.builder();
        builder.style(AOSP);
        System.out.println(new Formatter(builder.build()).formatSource(src));
    }


}