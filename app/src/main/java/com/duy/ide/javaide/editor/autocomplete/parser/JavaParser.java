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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.editor.autocomplete.model.ConstructorDescription;
import com.duy.ide.javaide.editor.autocomplete.model.FieldDescription;
import com.duy.ide.javaide.editor.autocomplete.model.MethodDescription;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.Tree;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import static com.google.common.base.Charsets.UTF_8;
import static com.sun.tools.javac.tree.JCTree.JCClassDecl;
import static com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import static com.sun.tools.javac.tree.JCTree.JCModifiers;

/**
 * Created by Duy on 16-Aug-17.
 */

public class JavaParser {
    private static final String TAG = "JavaParser";
    private static final String DOT = ".";
    private static final String CONSTRUCTOR_NAME = "<init>";

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
    public JCCompilationUnit parse(final String src) {
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
        JCCompilationUnit unit;
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
        return unit;
    }

    @Nullable
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        if (!canParse) return null;
        return diagnostics.getDiagnostics();
    }

    public List<IClass> parseClasses(JCCompilationUnit unit) {

        List<IClass> classes = new ArrayList<>();
        List<JCTree> typeDecls = unit.getTypeDecls();
        for (JCTree typeDecl : typeDecls) {
            if (typeDecl instanceof JCClassDecl) {
                classes.add(parseClass(unit, (JCClassDecl) typeDecl));
            }
        }
        return classes;
    }

    private IClass parseClass(JCCompilationUnit unit, JCClassDecl classDecl) {
        final String className = unit.getPackageName() + DOT + classDecl.getSimpleName();
        final int modifiers = JavaUtil.toJavaModifiers(classDecl.getModifiers().getFlags());

        ClassDescription clazz = new ClassDescription(
                className,
                modifiers,
                false,
                classDecl.getKind() == Tree.Kind.ANNOTATION_TYPE,
                classDecl.getKind() == Tree.Kind.ENUM);

        IClass extendsClass = JavaUtil.jcTypeToClass(unit, classDecl.getExtendsClause());
        if (extendsClass != null) {
            clazz.setSuperclass(extendsClass);
        } else {
            clazz.setSuperclass(JavaClassManager.getInstance().getParsedClass(Object.class.getName()));
        }


        List<JCTree> members = classDecl.getMembers();
        for (JCTree member : members) {
            if (member instanceof JCTree.JCMethodDecl) {
                addMethod(unit, clazz, (JCTree.JCMethodDecl) member);
            } else if (member instanceof JCTree.JCVariableDecl) {
                addVariable(unit, clazz, classDecl, (JCTree.JCVariableDecl) member);
            }
        }
        //now add constructor
        //add methods
        return clazz;
    }

    private void addVariable(@NonNull JCCompilationUnit unit,
                             ClassDescription clazz, @NonNull JCClassDecl classDecl,
                             @NonNull JCTree.JCVariableDecl member) {
        String name = member.getName().toString();
        int modifiers = JavaUtil.toJavaModifiers(member.getModifiers().getFlags());
        IClass type = JavaUtil.jcTypeToClass(unit, member.getType());
        JCTree.JCExpression initializer = member.getInitializer();
        FieldDescription fieldDescription = new FieldDescription(
                modifiers,
                type,
                name,
                initializer != null ? initializer.toString() : null);
        clazz.addField(fieldDescription);
    }

    private void addMethod(@NonNull JCCompilationUnit unit,
                           @NonNull ClassDescription clazz,
                           @NonNull JCTree.JCMethodDecl member) {


        final String methodName = member.getName().toString();
        final int modifiers = JavaUtil.toJavaModifiers(member.getModifiers().getFlags());

        final List<IClass> methodParameters = new ArrayList<>();
        List<JCTree.JCVariableDecl> parameters = member.getParameters();
        for (JCTree.JCVariableDecl parameter : parameters) {
            JCTree type = parameter.getType();
            IClass paramType = JavaUtil.jcTypeToClass(unit, type);
            methodParameters.add(paramType);
        }

        IClass returnType = JavaUtil.jcTypeToClass(unit, member.getReturnType());

        if (member.getName().toString().equals(CONSTRUCTOR_NAME)) {
            ConstructorDescription constructor = new ConstructorDescription(
                    clazz.getFullClassName(),
                    methodParameters);
            clazz.addConstructor(constructor);
        } else {
            MethodDescription methodDescription = new MethodDescription(
                    methodName,
                    modifiers,
                    methodParameters,
                    returnType);
            clazz.addMethod(methodDescription);
        }
    }


    private void addConstructor(ClassDescription clazz, JCTree.JCMethodDecl member) {
        ConstructorDescription constructorDescription;
    }

    private JCClassDecl resolveClassSimpleName(List<JCTree> typeDecls) {
        for (JCTree typeDecl : typeDecls) {
            if (typeDecl instanceof JCClassDecl) {
                JCModifiers modifiers = ((JCClassDecl) typeDecl).getModifiers();
                Set<Modifier> flags = modifiers.getFlags();
                for (Modifier flag : flags) {
                    if (flag.equals(Modifier.PUBLIC)) {
                        return (JCClassDecl) typeDecl;
                    }
                }
            }
        }
        return null;
    }
}
