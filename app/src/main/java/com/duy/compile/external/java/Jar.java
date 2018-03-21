/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.duy.compile.external.java;

import com.duy.project.file.java.JavaProjectFolder;

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

public class Jar {

    private static int BUFFER_SIZE = 10240;
    private static byte mBuffer[] = new byte[BUFFER_SIZE];
    private static boolean mVerbose = false;

    public static void main(String[] zArgs) {
        if (zArgs == null || zArgs.length < 2) {
            System.out.println("Usage : jar [-v] JARFILE FOLDER");
        } else {
            if (zArgs.length == 2) {
                //Its just the File and Folder
                String archive = zArgs[0];
                String folder = zArgs[1];

                createJarArchive(new File(archive), new File(folder));
            } else {
                //Its just the File and Folder
                mVerbose = true;
                String archive = zArgs[1];
                String folder = zArgs[2];

                System.out.println("JAR folder : " + folder + " > " + archive);
                createJarArchive(new File(archive), new File(folder));
            }
        }
    }

    public static void createJarArchive(JavaProjectFolder projectFolder) throws IOException {
        //input file
        File dirBuildClasses = projectFolder.getDirBuildClasses();
        File archiveFile = projectFolder.getOutJarArchive();


        // Open archive file
        FileOutputStream stream = new FileOutputStream(archiveFile);
        
        Manifest manifest = new Manifest();
        File manifestFile = new File(projectFolder, "MANIFEST.MF");
        if(manifestFile.exists()){
          FileInputStream manifestStream = new FileInputStream(manifestFile);
          manifest.read(manifestStream);
        } else manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        //Create the jar file
        JarOutputStream out = new JarOutputStream(stream, manifest);

        //Add the files..
        if (dirBuildClasses.listFiles() != null) {
            for (File file : dirBuildClasses.listFiles()) {
                addzz(dirBuildClasses.getPath(), file, out);
            }
        }

        out.close();
        stream.close();
        System.out.println("Adding completed OK");
    }

    private static void addzz(String parentPath, File source, JarOutputStream target) throws IOException {
        String name = source.getPath().substring(parentPath.length() + 1);
        System.out.println("Adding file : " + name);
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
                    addzz(parentPath, nestedFile, target);
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

    public static void createJarArchive(File archiveFile, File zTobeJared) {
        try {
            // Open archive file
            FileOutputStream stream = new FileOutputStream(archiveFile);
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            //Create the jar file
            JarOutputStream out = new JarOutputStream(stream, manifest);

            //Add the files..
            //addFile(zTobeJared, out);
            add(zTobeJared, out);

            out.close();
            stream.close();
            System.out.println("Adding completed OK");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error: " + ex.getMessage());
        }
    }

    protected static void addFile(File zFile, JarOutputStream zOut) throws IOException {
        //Check..
        if (!zFile.exists() || !zFile.canRead()) {
            return;
        }

        if (mVerbose) {
            System.out.println("Adding file : " + zFile.getPath());
        }

        //Add it..
        if (zFile.isDirectory()) {
            //Cycle through
            File[] files = zFile.listFiles();
            for (File ff : files) {
                addFile(ff, zOut);
            }

        } else {
            // Add archive entry
            JarEntry jarAdd = new JarEntry(zFile.getName());
            jarAdd.setTime(zFile.lastModified());
            zOut.putNextEntry(jarAdd);

            // Write file to archive
            FileInputStream in = new FileInputStream(zFile);
            while (true) {
                int nRead = in.read(mBuffer, 0, mBuffer.length);
                if (nRead <= 0)
                    break;

                zOut.write(mBuffer, 0, nRead);
            }
            in.close();
        }
    }

    private static void add(File source, JarOutputStream target) throws IOException {
        if (mVerbose) {
            System.out.println("Adding file : " + source.getPath());
        }

        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath();
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";

                    //Add the Entry
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }

                for (File nestedFile : source.listFiles())
                    add(nestedFile, target);

                return;
            }

            JarEntry entry = new JarEntry(source.getPath());
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
            if (in != null)
                in.close();
        }
    }

}
