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
public class MergeRootFrameLayoutDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new MergeRootFrameLayoutDetector();
    }


    public void testMergeRefFromJava() throws Exception {
        assertEquals(
            "res/layout/simple.xml:3: Warning: This <FrameLayout> can be replaced with a <merge> tag [MergeRootFrame]\n" +
            "<FrameLayout\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "res/layout/simple.xml",
                    "src/test/pkg/ImportFrameActivity.java.txt=>src/test/pkg/ImportFrameActivity.java"
                    ));
    }

    public void testMergeRefFromInclude() throws Exception {
        assertEquals(
            "res/layout/simple.xml:3: Warning: This <FrameLayout> can be replaced with a <merge> tag [MergeRootFrame]\n" +
            "<FrameLayout\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "res/layout/simple.xml",
                    "res/layout/simpleinclude.xml"
                    ));
    }

    public void testMergeRefFromIncludeSuppressed() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "res/layout/simple_ignore.xml=>res/layout/simple.xml",
                        "res/layout/simpleinclude.xml"
                        ));
    }

    public void testNotIncluded() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject("res/layout/simple.xml"));
    }
}
