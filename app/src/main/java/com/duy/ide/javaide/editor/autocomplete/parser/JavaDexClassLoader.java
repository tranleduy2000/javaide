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

import com.android.annotations.Nullable;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.interfaces.Filter;
import com.sun.tools.javac.tree.JCTree;

import java.io.File;
import java.util.List;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final Filter<IClass> mClassFilter = new Filter<IClass>() {
        @Override
        public boolean accept(IClass aClass) {
            return !(aClass.isAnnotation() || aClass.isInterface() || aClass.isEnum());
        }
    };
    private static final Filter<IClass> mEnumFilter = new Filter<IClass>() {
        @Override
        public boolean accept(IClass aClass) {
            return aClass.isEnum();
        }
    };
    private static final Filter<IClass> mInterfaceFilter = new Filter<IClass>() {
        @Override
        public boolean accept(IClass aClass) {
            return aClass.isInterface();
        }
    };
    private static final Filter<IClass> mAnnotationFilter = new Filter<IClass>() {
        @Override
        public boolean accept(IClass aClass) {
            return aClass.isAnnotation();
        }
    };

    private JavaClassManager mClassReader;

    public JavaDexClassLoader(File classpath, File outDir) {
        mClassReader = JavaClassManager.getInstance(classpath, outDir);
    }

    public JavaClassManager getClassReader() {
        return mClassReader;
    }

    @NonNull
    public List<IClass> findAllWithPrefix(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, null);
    }

    @NonNull
    public List<IClass> findAllWithPrefix(@NonNull String simpleNamePrefix,
                                          @Nullable Filter<IClass> filter) {
        return mClassReader.find(simpleNamePrefix, filter);
    }

    public List<IClass> findClasses(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mClassFilter);
    }

    public List<IClass> findInterfaces(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mInterfaceFilter);
    }

    public List<IClass> findEnums(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mEnumFilter);

    }

    public List<IClass> findAnnotations(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mAnnotationFilter);
    }


    public void loadAllClasses(JavaProject projectFile) {
        mClassReader.loadFromProject(projectFile);
    }

    public void updateClass(JCTree.JCCompilationUnit ast) {
        // TODO: 14-Jun-18
    }
}
