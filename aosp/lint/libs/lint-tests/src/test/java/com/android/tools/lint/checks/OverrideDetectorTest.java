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
public class OverrideDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new OverrideDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/pkg2/Class2.java:7: Error: This package private method may be unintentionally overriding method in pkg1.Class1 [DalvikOverride]\n" +
            "    void method() { // Flag this as an accidental override\n" +
            "         ~~~~~~\n" +
            "    src/pkg1/Class1.java:4: This method is treated as overridden\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "src/pkg1/Class1.java.txt=>src/pkg1/Class1.java",
                "src/pkg2/Class2.java.txt=>src/pkg2/Class2.java",
                "bytecode/Class1.class.data=>bin/classes/pkg1/Class1.class",
                "bytecode/Class1$Class4.class.data=>bin/classes/pkg1/Class1$Class4.class",
                "bytecode/Class2.class.data=>bin/classes/pkg2/Class2.class",
                "bytecode/Class2$Class3.class.data=>bin/classes/pkg2/Class2$Class3.class"
                ));
    }
}
