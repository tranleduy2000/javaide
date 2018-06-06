/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class JavaPerformanceDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new JavaPerformanceDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/JavaPerformanceTest.java:28: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        new String(\"foo\");\n" +
            "        ~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:29: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        String s = new String(\"bar\");\n" +
            "                   ~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:103: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        new String(\"flag me\");\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:109: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        new String(\"flag me\");\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:112: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        Bitmap.createBitmap(100, 100, null);\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:113: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        android.graphics.Bitmap.createScaledBitmap(null, 100, 100, false);\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:114: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "        BitmapFactory.decodeFile(null);\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:116: Warning: Avoid object allocations during draw operations: Use Canvas.getClipBounds(Rect) instead of Canvas.getClipBounds() which allocates a temporary Rect [DrawAllocation]\n" +
            "        canvas.getClipBounds(); // allocates on your behalf\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:140: Warning: Avoid object allocations during draw/layout operations (preallocate and reuse instead) [DrawAllocation]\n" +
            "            new String(\"foo\");\n" +
            "            ~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:70: Warning: Use new SparseArray<String>(...) instead for better performance [UseSparseArrays]\n" +
            "        Map<Integer, String> myMap = new HashMap<Integer, String>();\n" +
            "                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:72: Warning: Use new SparseBooleanArray(...) instead for better performance [UseSparseArrays]\n" +
            "        Map<Integer, Boolean> myBoolMap = new HashMap<Integer, Boolean>();\n" +
            "                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:74: Warning: Use new SparseIntArray(...) instead for better performance [UseSparseArrays]\n" +
            "        Map<Integer, Integer> myIntMap = new java.util.HashMap<Integer, Integer>();\n" +
            "                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:190: Warning: Use new SparseIntArray(...) instead for better performance [UseSparseArrays]\n" +
            "        new SparseArray<Integer>(); // Use SparseIntArray instead\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:192: Warning: Use new SparseBooleanArray(...) instead for better performance [UseSparseArrays]\n" +
            "        new SparseArray<Boolean>(); // Use SparseBooleanArray instead\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:201: Warning: Use new SparseArray<String>(...) instead for better performance [UseSparseArrays]\n" +
            "        Map<Byte, String> myByteMap = new HashMap<Byte, String>();\n" +
            "                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:33: Warning: Use Integer.valueOf(5) instead [UseValueOf]\n" +
            "        Integer i = new Integer(5);\n" +
            "                    ~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:145: Warning: Use Integer.valueOf(42) instead [UseValueOf]\n" +
            "        Integer i1 = new Integer(42);\n" +
            "                     ~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:146: Warning: Use Long.valueOf(42L) instead [UseValueOf]\n" +
            "        Long l1 = new Long(42L);\n" +
            "                  ~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:147: Warning: Use Boolean.valueOf(true) instead [UseValueOf]\n" +
            "        Boolean b1 = new Boolean(true);\n" +
            "                     ~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:148: Warning: Use Character.valueOf('c') instead [UseValueOf]\n" +
            "        Character c1 = new Character('c');\n" +
            "                       ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:149: Warning: Use Float.valueOf(1.0f) instead [UseValueOf]\n" +
            "        Float f1 = new Float(1.0f);\n" +
            "                   ~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/JavaPerformanceTest.java:150: Warning: Use Double.valueOf(1.0) instead [UseValueOf]\n" +
            "        Double d1 = new Double(1.0);\n" +
            "                    ~~~~~~~~~~~~~~~\n" +
            "0 errors, 22 warnings\n",

            lintProject("src/test/pkg/JavaPerformanceTest.java.txt=>" +
                    "src/test/pkg/JavaPerformanceTest.java"));
    }

    public void testLongSparseArray() throws Exception {
        assertEquals(""
                + "src/test/pkg/LongSparseArray.java:10: Warning: Use new LongSparseArray(...) instead for better performance [UseSparseArrays]\n"
                + "        Map<Long, String> myStringMap = new HashMap<Long, String>();\n"
                + "                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                    "apicheck/minsdk17.xml=>AndroidManifest.xml",
                    "src/test/pkg/LongSparseArray.java.txt=>src/test/pkg/LongSparseArray.java"));
    }

    public void testLongSparseSupportLibArray() throws Exception {
        assertEquals(""
                + "src/test/pkg/LongSparseArray.java:10: Warning: Use new android.support.v4.util.LongSparseArray(...) instead for better performance [UseSparseArrays]\n"
                + "        Map<Long, String> myStringMap = new HashMap<Long, String>();\n"
                + "                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "src/test/pkg/LongSparseArray.java.txt=>src/test/pkg/LongSparseArray.java",
                        "bytecode/classes.jar=>libs/android-support-v4.jar"));
    }

    public void testNoLongSparseArray() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                    "apicheck/minsdk1.xml=>AndroidManifest.xml",
                    "src/test/pkg/LongSparseArray.java.txt=>src/test/pkg/LongSparseArray.java"));
    }

    public void testSparseLongArray1() throws Exception {
        assertEquals(""
                + "src/test/pkg/SparseLongArray.java:10: Warning: Use new SparseLongArray(...) instead for better performance [UseSparseArrays]\n"
                + "        Map<Integer, Long> myStringMap = new HashMap<Integer, Long>();\n"
                + "                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "apicheck/minsdk19.xml=>AndroidManifest.xml",
                        "src/test/pkg/SparseLongArray.java.txt=>src/test/pkg/SparseLongArray.java"));
    }

    public void testSparseLongArray2() throws Exception {
        // Note -- it's offering a SparseArray, not a SparseLongArray!
        assertEquals(""
                + "src/test/pkg/SparseLongArray.java:10: Warning: Use new SparseArray<Long>(...) instead for better performance [UseSparseArrays]\n"
                + "        Map<Integer, Long> myStringMap = new HashMap<Integer, Long>();\n"
                + "                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "apicheck/minsdk1.xml=>AndroidManifest.xml",
                        "src/test/pkg/SparseLongArray.java.txt=>src/test/pkg/SparseLongArray.java"));
    }
}
