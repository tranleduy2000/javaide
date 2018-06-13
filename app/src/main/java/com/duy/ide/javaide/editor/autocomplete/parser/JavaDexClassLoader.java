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
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final Filter<Class> mClassFilter = new Filter<Class>() {
        @Override
        public boolean accepts(Class aClass) {
            return !(aClass.isAnnotation() || aClass.isInterface() || aClass.isEnum()
                    && aClass.isAnonymousClass() || aClass.isSynthetic());
        }
    };
    private static final Filter<Class> mEnumFilter = new Filter<Class>() {
        @Override
        public boolean accepts(Class aClass) {
            return aClass.isEnum();
        }
    };
    private static final Filter<Class> mInterfaceFilter = new Filter<Class>() {
        @Override
        public boolean accepts(Class aClass) {
            return aClass.isInterface();
        }
    };
    private static final Filter<Class> mAnnotationFilter = new Filter<Class>() {
        @Override
        public boolean accepts(Class aClass) {
            return aClass.isAnnotation();
        }
    };

    private static final String TAG = "JavaDexClassLoader";
    private JavaClassManager mClassReader;

    public JavaDexClassLoader(File classpath, File outDir) {
        mClassReader = JavaClassManager.getInstance(classpath, outDir);
    }

    public JavaClassManager getClassReader() {
        return mClassReader;
    }

    @NonNull
    public ArrayList<ClassDescription> findAllWithPrefix(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, null);
    }

    @NonNull
    public ArrayList<ClassDescription> findAllWithPrefix(@NonNull String simpleNamePrefix,
                                                         @Nullable Filter<Class> filter) {
        return mClassReader.find(simpleNamePrefix, filter);
    }

    public ArrayList<ClassDescription> findClasses(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mClassFilter);
    }

    public ArrayList<ClassDescription> findInterfaces(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mInterfaceFilter);
    }

    public ArrayList<ClassDescription> findEnums(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mEnumFilter);

    }

    public ArrayList<ClassDescription> findAnnotations(String simpleNamePrefix) {
        return mClassReader.find(simpleNamePrefix, mAnnotationFilter);
    }


    public ClassDescription loadClass(String className) {
        return mClassReader.getParsedClass(className, null);
    }

    public void loadAllClasses(JavaProject projectFile) {
        mClassReader.load(projectFile);
    }
}
