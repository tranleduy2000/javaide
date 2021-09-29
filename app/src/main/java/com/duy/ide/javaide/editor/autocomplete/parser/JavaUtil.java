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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sun.tools.javac.tree.JCTree;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaUtil {
    public static String getSimpleName(String className) {
        if (className.contains(".")) {
            return className.substring(className.lastIndexOf(".") + 1);
        } else {
            return className;
        }
    }

    @NonNull
    public static String getPackageName(String classname) {
        if (classname.contains(".")) {
            return classname.substring(0, classname.lastIndexOf("."));
        } else {
            return "";
        }
    }

    public static String getInverseName(String className) {
        String[] split = className.split(".");
        String result = "";
        for (String s : split) {
            result = s + result;
        }
        return result;
    }


    public static boolean isValidClassName(@Nullable String name) {
        return name != null && name.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    @Nullable
    public static String getClassName(File javaSrc, String filePath) {
        if (filePath.startsWith(javaSrc.getPath())) {
            //hello/src/main/java
            //hello ->
            String filename = filePath.substring(filePath.indexOf(javaSrc.getPath()) + javaSrc.getPath().length() + 1);
            filename = filename.replace(File.separator, ".");
            if (filename.endsWith(".java")) {
                filename = filename.substring(0, filename.lastIndexOf(".java"));
            }
            return filename;
        } else {
            return null;
        }
    }

    public static int toJavaModifiers(Set<Modifier> modifierSet) {
        int modifiers = 0;
        for (Modifier modifier : modifierSet) {
            switch (modifier) {
                case FINAL:
                    modifiers |= java.lang.reflect.Modifier.FINAL;
                    break;
                case PROTECTED:
                    modifiers |= java.lang.reflect.Modifier.PROTECTED;

                    break;
                case PRIVATE:
                    modifiers |= java.lang.reflect.Modifier.PRIVATE;

                    break;
                case ABSTRACT:
                    modifiers |= java.lang.reflect.Modifier.ABSTRACT;

                    break;
                case NATIVE:
                    modifiers |= java.lang.reflect.Modifier.NATIVE;

                case PUBLIC:
                    modifiers |= java.lang.reflect.Modifier.PUBLIC;

                case STATIC:
                    modifiers |= java.lang.reflect.Modifier.STATIC;

                case TRANSIENT:
                    modifiers |= java.lang.reflect.Modifier.TRANSIENT;

                    break;
                case VOLATILE:
                    modifiers |= java.lang.reflect.Modifier.VOLATILE;

                    break;
                case SYNCHRONIZED:
                    modifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;

                    break;
                case STRICTFP:
                    modifiers |= java.lang.reflect.Modifier.STRICT;

                    break;
            }
        }
        return modifiers;
    }

    @NonNull
    public static String findImportedClassName(@NonNull JCTree.JCCompilationUnit mUnit,
                                               @NonNull String className) {
        List<JCTree.JCImport> imports = mUnit.getImports();
        for (JCTree.JCImport jcImport : imports) {
            String fullName = jcImport.getQualifiedIdentifier().toString();
            if (fullName.equals(className) || fullName.endsWith("." + className)) {
                return fullName;
            }
        }
        return className;
    }

    @Nullable
    public static IClass jcTypeToClass(JCTree.JCCompilationUnit unit, JCTree type) {
        if (type == null) {
            return null;
        }
        String className;
        if (type instanceof JCTree.JCTypeApply) {   //generic
            return jcTypeToClass(unit, ((JCTree.JCTypeApply) type).getType());
        } else {
            className = type.toString();
        }
        //try to find full class name
        className = findImportedClassName(unit, className);
        return JavaClassManager.getInstance().getParsedClass(className);
    }
    public static ArrayList<String> listClassName(File src) {
        if (!src.exists()) return new ArrayList<>();

        String[] exts = new String[]{"java"};
        Collection<File> files = FileUtils.listFiles(src, exts, true);

        ArrayList<String> classes = new ArrayList<>();
        String srcPath = src.getPath();
        for (File file : files) {
            String javaPath = file.getPath();
            javaPath = javaPath.substring(srcPath.length() + 1, javaPath.length() - 5); //.java
            javaPath = javaPath.replace(File.separator, ".");
            classes.add(javaPath);
        }
        return classes;
    }


}
