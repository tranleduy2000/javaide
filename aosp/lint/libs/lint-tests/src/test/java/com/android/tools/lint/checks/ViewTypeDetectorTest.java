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
public class ViewTypeDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ViewTypeDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/WrongCastActivity.java:13: Error: Unexpected cast to ToggleButton: layout tag was Button [WrongViewCast]\n" +
            "        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.button);\n" +
            "                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                    "res/layout/casts.xml",
                    "src/test/pkg/WrongCastActivity.java.txt=>src/test/pkg/WrongCastActivity.java"
                ));
    }

    public void test2() throws Exception {
        assertEquals(
            "src/test/pkg/WrongCastActivity.java:13: Error: Unexpected cast to ToggleButton: layout tag was Button|RadioButton [WrongViewCast]\n" +
            "        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.button);\n" +
            "                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                    "res/layout/casts.xml",
                    "res/layout/casts3.xml",
                    "src/test/pkg/WrongCastActivity.java.txt=>src/test/pkg/WrongCastActivity.java"
                ));
    }

    public void test3() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/layout/casts.xml",
                    "res/layout/casts4.xml",
                    "src/test/pkg/WrongCastActivity.java.txt=>src/test/pkg/WrongCastActivity.java"
                ));
    }

    public void test27441() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "res/layout/casts2.xml",
                "src/test/pkg/WrongCastActivity2.java.txt=>src/test/pkg/WrongCastActivity2.java"
            ));
    }

    public void testCheckable() throws Exception {
        assertEquals(
                "No warnings.",

            lintProject(
                "res/layout/casts2.xml",
                "src/test/pkg/WrongCastActivity3.java.txt=>src/test/pkg/WrongCastActivity3.java"
            ));
    }

    public void testIncremental() throws Exception {
        assertEquals(
                "src/test/pkg/WrongCastActivity.java:13: Error: Unexpected cast to ToggleButton: layout tag was Button [WrongViewCast]\n" +
                        "        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.button);\n" +
                        "                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                        "1 errors, 0 warnings\n",

                lintProjectIncrementally(
                        "src/test/pkg/WrongCastActivity.java",
                        "res/layout/casts.xml",
                        "src/test/pkg/WrongCastActivity.java.txt=>src/test/pkg/WrongCastActivity.java"
                ));
    }

}
