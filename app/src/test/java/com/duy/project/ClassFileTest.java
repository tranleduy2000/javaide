package com.duy.project;

import com.duy.android.compiler.file.java.ClassFile;

import junit.framework.TestCase;

/**
 * Created by Duy on 18-Jul-17.
 */
public class ClassFileTest extends TestCase {
    public void test1() {
        String className = this.getClass().getName();
        ClassFile classFile = new ClassFile(className);
        assertEquals(classFile.getSimpleName(), this.getClass().getSimpleName());
    }

    public void test2() {
        String className = this.getClass().getName();
        ClassFile classFile = new ClassFile(className);
        System.out.println(classFile.getPackage());
        assertEquals(classFile.getPackage(), this.getClass().getPackage().toString());
    }
}