/*
 * Copyright (c) 2001, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package sun.misc;

import java.util.ArrayList;

/**
 * This is an abstract base class which is called by java.lang.ClassLoader
 * when ClassFormatError is thrown inside defineClass().
 *
 * The purpose of this class is to allow applications (e.g. Java Plug-in)
 * to have a chance to transform the byte code from one form to another
 * if necessary.
 *
 * One application of this class is used by Java Plug-in to transform
 * malformed JDK 1.1 class file into a well-formed Java 2 class file
 * on-the-fly, so JDK 1.1 applets with malformed class file in the
 * Internet may run in Java 2 after transformation.
 *
 * @author      Stanley Man-Kit Ho
 */

public abstract class ClassFileTransformer
{
    // Singleton of ClassFileTransformer
    //
    private static ArrayList<ClassFileTransformer> transformerList
        = new ArrayList<ClassFileTransformer>();
    private static ClassFileTransformer[] transformers
        = new ClassFileTransformer[0];

    /**
     * Add the class file transformer object.
     *
     * @param t Class file transformer instance
     */
    public static void add(ClassFileTransformer t)
    {
        synchronized(transformerList)
        {
            transformerList.add(t);
            transformers = transformerList.toArray(new ClassFileTransformer[0]);
        }
    }

    /**
     * Get the array of ClassFileTransformer object.
     *
     * @return ClassFileTransformer object array
     */
    public static ClassFileTransformer[] getTransformers()
    {
        // transformers is not intended to be changed frequently,
        // so it is okay to not put synchronized block here
        // to speed up performance.
        //
        return transformers;
    }


    /**
     * Transform a byte array from one to the other.
     *
     * @param b Byte array
     * @param off Offset
     * @param len Length of byte array
     * @return Transformed byte array
     */
    public abstract byte[] transform(byte[] b, int off, int len)
                           throws ClassFormatError;
}
