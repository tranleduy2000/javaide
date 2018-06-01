/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.duy.android.compiler.java;

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

    public JarArchive(boolean verbose) {
        this.mVerbose = verbose;
    }

    public void createJarArchive(JavaProject projectFolder) throws IOException {
        //input file
        File dirBuildClasses = projectFolder.getDirBuildClasses();
        File archiveFile = projectFolder.getOutJarArchive();

        // Open archive file
        FileOutputStream stream = new FileOutputStream(archiveFile);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        //Create the jar file
        JarOutputStream out = new JarOutputStream(stream, manifest);

        //Add the files..
        if (dirBuildClasses.listFiles() != null) {
            for (File file : dirBuildClasses.listFiles()) {
                add(dirBuildClasses.getPath(), file, out);
            }
        }

        out.close();
        stream.close();
    }

    private void add(String parentPath, File source, JarOutputStream target) throws IOException {
        String name = source.getPath().substring(parentPath.length() + 1);
        if (mVerbose) {
            System.out.println("Adding file : " + name);
        }
        BufferedInputStream in = null;
        try {
            System.out.println("name = " + name);
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

}
