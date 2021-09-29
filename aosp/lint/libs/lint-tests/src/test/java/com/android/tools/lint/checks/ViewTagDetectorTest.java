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
public class ViewTagDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ViewTagDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/ViewTagTest.java:21: Warning: Avoid setting views as values for setTag: Can lead to memory leaks in versions older than Android 4.0 [ViewTag]\n" +
            "        view.setTag(android.R.id.button1, group); // ERROR\n" +
            "             ~~~~~~\n" +
            "src/test/pkg/ViewTagTest.java:22: Warning: Avoid setting views as values for setTag: Can lead to memory leaks in versions older than Android 4.0 [ViewTag]\n" +
            "        view.setTag(android.R.id.icon, view.findViewById(android.R.id.icon)); // ERROR\n" +
            "             ~~~~~~\n" +
            "src/test/pkg/ViewTagTest.java:23: Warning: Avoid setting cursors as values for setTag: Can lead to memory leaks in versions older than Android 4.0 [ViewTag]\n" +
            "        view.setTag(android.R.id.icon1, cursor1); // ERROR\n" +
            "             ~~~~~~\n" +
            "src/test/pkg/ViewTagTest.java:24: Warning: Avoid setting cursors as values for setTag: Can lead to memory leaks in versions older than Android 4.0 [ViewTag]\n" +
            "        view.setTag(android.R.id.icon2, cursor2); // ERROR\n" +
            "             ~~~~~~\n" +
            "src/test/pkg/ViewTagTest.java:25: Warning: Avoid setting view holders as values for setTag: Can lead to memory leaks in versions older than Android 4.0 [ViewTag]\n" +
            "        view.setTag(android.R.id.copy, new MyViewHolder()); // ERROR\n" +
            "             ~~~~~~\n" +
            "0 errors, 5 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "bytecode/ViewTagTest.java.txt=>src/test/pkg/ViewTagTest.java",
                    "bytecode/ViewTagTest.class.data=>bin/classes/test/pkg/ViewTagTest.class"
                    ));
    }

    public void testICS() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "bytecode/ViewTagTest.java.txt=>src/test/pkg/ViewTagTest.java",
                    "bytecode/ViewTagTest.class.data=>bin/classes/test/pkg/ViewTagTest.class"
                    ));
    }
}
