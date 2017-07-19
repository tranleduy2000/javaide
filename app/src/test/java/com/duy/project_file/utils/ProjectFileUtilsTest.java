package com.duy.project_file.utils;

import junit.framework.TestCase;

import java.io.File;

/**
 * Created by duy on 18/07/2017.
 */
public class ProjectFileUtilsTest extends TestCase {
    public void testInSrcDir() throws Exception {

    }

    public void testIsRoot() throws Exception {

    }

    public void testFindPackage() throws Exception {
        String root = "/home/duy/StudioProjects/javaide";
        String current = "/home/duy/StudioProjects/javaide/src/main/java/com/duy/project_files/utils";
        String aPackage = ProjectFileUtil.findPackage(root, new File(current));
        assertEquals(aPackage, "com.duy.project_files.utils");
    }

}