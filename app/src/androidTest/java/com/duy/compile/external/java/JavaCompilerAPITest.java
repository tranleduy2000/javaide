package com.duy.compile.external.java;


import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.duy.project_file.ProjectFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by duy on 19/07/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class JavaCompilerAPITest {

    @Before
    public void setup() {

    }

    @Test
    public void testCompile() throws IOException {
        String rootDir = "/home/duy/StudioProjects/javaide/sample";
        ProjectFile projectFile = new ProjectFile("com.duy.Main", "com.duy", "sample");
        projectFile.setRootDir(rootDir);
        JavaCompilerAPI.process(projectFile);
    }
}