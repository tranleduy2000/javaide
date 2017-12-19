/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.multidex;

import com.duy.dx.cf.direct.DirectClassFile;
import com.duy.dx.rop.cst.Constant;
import com.duy.dx.rop.cst.ConstantPool;
import com.duy.dx.rop.cst.CstFieldRef;
import com.duy.dx.rop.cst.CstMethodRef;
import com.duy.dx.rop.cst.CstType;
import com.duy.dx.rop.type.Prototype;
import com.duy.dx.rop.type.StdTypeList;
import com.duy.dx.rop.type.Type;
import com.duy.dx.rop.type.TypeList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tool to find direct class references to other classes.
 */
public class ClassReferenceListBuilder {
    private static final String CLASS_EXTENSION = ".class";

    private final Path path;
    private final Set<String> classNames = new HashSet<String>();

    public ClassReferenceListBuilder(Path path) {
        this.path = path;
    }

    /**
     * Kept for compatibility with the gradle integration, this method just forwards to
     * {@link MainDexListBuilder#main(String[])}.
     * @deprecated use {@link MainDexListBuilder#main(String[])} instead.
     */
    @Deprecated
    public static void main(String[] args) {
        com.duy.multidex.MainDexListBuilder.main(args);
    }

    /**
     * @param jarOfRoots Archive containing the class files resulting of the tracing, typically
     * this is the result of running ProGuard.
     */
    public void addRoots(ZipFile jarOfRoots) throws IOException {

        // keep roots
        for (Enumeration<? extends ZipEntry> entries = jarOfRoots.entries();
                entries.hasMoreElements();) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(CLASS_EXTENSION)) {
                classNames.add(name.substring(0, name.length() - CLASS_EXTENSION.length()));
            }
        }

        // keep direct references of roots (+ direct references hierarchy)
        for (Enumeration<? extends ZipEntry> entries = jarOfRoots.entries();
                entries.hasMoreElements();) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(CLASS_EXTENSION)) {
                DirectClassFile classFile;
                try {
                    classFile = path.getClass(name);
                } catch (FileNotFoundException e) {
                    throw new IOException("Class " + name +
                            " is missing form original class path " + path, e);
                }

                addDependencies(classFile.getConstantPool());
            }
        }
    }

    Set<String> getClassNames() {
        return classNames;
    }

    private void addDependencies(ConstantPool pool) {
        for (Constant constant : pool.getEntries()) {
            if (constant instanceof CstType) {
                checkDescriptor(((CstType) constant).getClassType());
            } else if (constant instanceof CstFieldRef) {
                checkDescriptor(((CstFieldRef) constant).getType());
            } else if (constant instanceof CstMethodRef) {
                Prototype proto = ((CstMethodRef) constant).getPrototype();
                checkDescriptor(proto.getReturnType());
                StdTypeList args = proto.getParameterTypes();
                for (int i = 0; i < args.size(); i++) {
                    checkDescriptor(args.get(i));
                }
            }
        }
    }

    private void checkDescriptor(Type type) {
        String descriptor = type.getDescriptor();
        if (descriptor.endsWith(";")) {
            int lastBrace = descriptor.lastIndexOf('[');
            if (lastBrace < 0) {
                addClassWithHierachy(descriptor.substring(1, descriptor.length()-1));
            } else {
                assert descriptor.length() > lastBrace + 3
                && descriptor.charAt(lastBrace + 1) == 'L';
                addClassWithHierachy(descriptor.substring(lastBrace + 2,
                        descriptor.length() - 1));
            }
        }
    }

    private void addClassWithHierachy(String classBinaryName) {
        if (classNames.contains(classBinaryName)) {
            return;
        }

        try {
            DirectClassFile classFile = path.getClass(classBinaryName + CLASS_EXTENSION);
            classNames.add(classBinaryName);
            CstType superClass = classFile.getSuperclass();
            if (superClass != null) {
                addClassWithHierachy(superClass.getClassType().getClassName());
            }

            TypeList interfaceList = classFile.getInterfaces();
            int interfaceNumber = interfaceList.size();
            for (int i = 0; i < interfaceNumber; i++) {
                addClassWithHierachy(interfaceList.getType(i).getClassName());
            }
        } catch (FileNotFoundException e) {
            // Ignore: The referenced type is not in the path it must be part of the libraries.
        }
    }

}
