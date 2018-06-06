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
package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class ParcelDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ParcelDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/bytecode/MyParcelable1.java:6: Error: This class implements Parcelable but does not provide a CREATOR field [ParcelCreator]\n"
                + "public class MyParcelable1 implements Parcelable {\n"
                + "             ~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/MyParcelable1.java.txt=>src/test/bytecode/MyParcelable1.java",
                "bytecode/MyParcelable2.java.txt=>src/test/bytecode/MyParcelable2.java",
                "bytecode/MyParcelable3.java.txt=>src/test/bytecode/MyParcelable3.java",
                "bytecode/MyParcelable4.java.txt=>src/test/bytecode/MyParcelable4.java",
                "bytecode/MyParcelable5.java.txt=>src/test/bytecode/MyParcelable5.java",
                "bytecode/MyParcelable1.class.data=>bin/classes/test/bytecode/MyParcelable1.class",
                "bytecode/MyParcelable2.class.data=>bin/classes/test/bytecode/MyParcelable2.class",
                "bytecode/MyParcelable2$1.class.data=>bin/classes/test/bytecode/MyParcelable2$1.class",
                "bytecode/MyParcelable3.class.data=>bin/classes/test/bytecode/MyParcelable3.class",
                "bytecode/MyParcelable4.class.data=>bin/classes/test/bytecode/MyParcelable4.class",
                "bytecode/MyParcelable5.class.data=>bin/classes/test/bytecode/MyParcelable5.class"
                ));
    }
}
