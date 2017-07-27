package com.google.googlejavaformat.java;

import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by Duy on 22-Jul-17.
 */

public class ParserTest extends TestCase {
    public void test1() {
        final String src = "package com.duy;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "/** * Created by Duy on 17-Jul-17. */\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        ArrayList list = new ArrayList();\n" +
                "        for (int i = 0; i < 1000; i++) {\n" +
                "            list.add(i);\n" +
                "        }\n" +
                "    }\n" +
                "\n";
        Context context = new Context();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JCTree.JCCompilationUnit unit;
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            throw new IOError(e);
        }
        SimpleJavaFileObject source =
                new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return src;
                    }
                };
        Log.instance(context).useSource(source);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Parser parser =
                parserFactory.newParser(
                        src,
            /*keepDocComments=*/ true,
            /*keepEndPos=*/ true,
            /*keepLineMap=*/ true);
        unit = parser.parseCompilationUnit();
        System.out.println(unit.getImports());

    }
}



