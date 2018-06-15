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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.duy.android.compiler.builder.internal.jar;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.android.compiler.project.JavaProject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by duy on 19/07/2017.
 */

public class JarArchive {
    private boolean mVerbose;
    @Nullable
    private JarOptions mOptions;

    public JarArchive(boolean verbose, JarOptions options) {
        this.mVerbose = verbose;
        this.mOptions = options;
    }

    public JarArchive(boolean verbose) {
        this.mVerbose = verbose;
    }

    public void createJarArchive(JavaProject project) throws IOException {
        //input file
        File classesFolder = project.getDirBuildClasses();
        File archiveFile = project.getOutJarArchive();

        // Open archive file
        FileOutputStream stream = new FileOutputStream(archiveFile);

        Manifest manifest = buildManifest(getOptions());

        //Create the jar file
        JarOutputStream out = new JarOutputStream(stream, manifest);

        //Add the files..
        if (classesFolder.listFiles() != null) {
            for (File clazz : classesFolder.listFiles()) {
                add(classesFolder.getPath(), clazz, out);
            }
        }

        out.close();
        stream.close();
    }

    @NonNull
    private Manifest buildManifest(@Nullable JarOptions options) {
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();

        if (options != null) {
            attrs.put(Attributes.Name.MANIFEST_VERSION, options.getManifestVersion());
            if (options.getMainClass() != null) {
                attrs.put(Attributes.Name.MAIN_CLASS, options.getMainClass());
            }
        } else {
            attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }
        return manifest;
    }

    private void add(String parentPath, File source, JarOutputStream target) throws IOException {
        String name = source.getPath().substring(parentPath.length() + 1);
        if (mVerbose) {
            System.out.println("Adding file : " + name);
        }
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";

                    //Add the Entry
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }

                for (File nestedFile : source.listFiles()) {
                    add(parentPath, nestedFile, target);
                }
                return;
            }

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));
            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();

        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public JarOptions getOptions() {
        return mOptions;
    }

    public void setOptions(JarOptions mOptions) {
        this.mOptions = mOptions;
    }
}
