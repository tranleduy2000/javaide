/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.parser;

import com.android.annotations.Nullable;
import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Created by Duy on 16-Aug-17.
 */

public class JavaParser {
    private static final String TAG = "JavaParser";
    private Context context;
    private ParserFactory parserFactory;
    private DiagnosticCollector<JavaFileObject> diagnostics;
    private boolean canParse = true;

    public JavaParser() {
        context = new Context();
        diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            canParse = false;
        }
        parserFactory = ParserFactory.instance(context);
    }

    @Nullable
    public JCTree.JCCompilationUnit parse(final String src) {
        if (!canParse) return null;
        long time = System.currentTimeMillis();

        SimpleJavaFileObject source = new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return src;
            }
        };
        Log.instance(context).useSource(source);
        Parser parser = parserFactory.newParser(src,
                /*keepDocComments=*/ true,
                /*keepEndPos=*/ true,
                /*keepLineMap=*/ true);
        JCTree.JCCompilationUnit unit;
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
        return unit;
    }

    @Nullable
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        if (!canParse) return null;
        return diagnostics.getDiagnostics();
    }
}
