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

package com.duy.ide.javaide.editor.autocomplete.dex;

import android.support.annotation.NonNull;

import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.android.compiler.project.JavaProject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final String TAG = "JavaDexClassLoader";
    private JavaClassReader mClassReader;

    public JavaDexClassLoader(File classpath, File outDir) {
        mClassReader = new JavaClassReader(classpath.getPath(), outDir.getPath());
    }

    public JavaClassReader getClassReader() {
        return mClassReader;
    }

    @NonNull
    public ArrayList<ClassDescription> findClassWithPrefix(String simpleNamePrefix) {
        return mClassReader.findClass(simpleNamePrefix);
    }


    public void touchClass(String className) {
        ClassDescription classDescriptions = mClassReader.readClassByName(className, null);
    }

    public ClassDescription loadClass(String className) {
        return mClassReader.readClassByName(className, null);
    }

    public void loadAllClasses(JavaProject projectFile) {
        mClassReader.load(projectFile);
    }
}
