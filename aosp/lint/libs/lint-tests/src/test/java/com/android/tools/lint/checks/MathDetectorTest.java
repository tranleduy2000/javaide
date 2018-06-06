/*
 * Copyright (C) 2011 The Android Open Source Project
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
public class MathDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new MathDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/bytecode/MathTest.java:11: Warning: Use java.lang.Math#cos instead of android.util.FloatMath#cos() since it is faster as of API 8 [FloatMath]\n" +
            "        floatResult = FloatMath.cos(x);\n" +
            "                                ~~~\n" +
            "src/test/bytecode/MathTest.java:12: Warning: Use java.lang.Math#sin instead of android.util.FloatMath#sin() since it is faster as of API 8 [FloatMath]\n" +
            "        floatResult = FloatMath.sin((float) y);\n" +
            "                                ~~~\n" +
            "src/test/bytecode/MathTest.java:13: Warning: Use java.lang.Math#ceil instead of android.util.FloatMath#ceil() since it is faster as of API 8 [FloatMath]\n" +
            "        floatResult = android.util.FloatMath.ceil((float) y);\n" +
            "                                             ~~~~\n" +
            "src/test/bytecode/MathTest.java:14: Warning: Use java.lang.Math#floor instead of android.util.FloatMath#floor() since it is faster as of API 8 [FloatMath]\n" +
            "        System.out.println(FloatMath.floor(x));\n" +
            "                                     ~~~~~\n" +
            "src/test/bytecode/MathTest.java:15: Warning: Use java.lang.Math#sqrt instead of android.util.FloatMath#sqrt() since it is faster as of API 8 [FloatMath]\n" +
            "        System.out.println(FloatMath.sqrt(z));\n" +
            "                                     ~~~~\n" +
            "0 errors, 5 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                    "bytecode/MathTest.java.txt=>src/test/bytecode/MathTest.java",
                    "bytecode/MathTest.class.data=>bin/classes/test/bytecode/MathTest.class"
                    ));
    }

    public void testNoWarningsPreFroyo() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "apicheck/minsdk2.xml=>AndroidManifest.xml",
                    "bytecode/MathTest.java.txt=>src/test/bytecode/MathTest.java",
                    "bytecode/MathTest.class.data=>bin/classes/test/bytecode/MathTest.class"
                    ));
    }

}
