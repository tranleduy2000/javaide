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
public class UselessViewDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new UselessViewDetector();
    }

    public void testUseless() throws Exception {
        assertEquals(
            "res/layout/useless.xml:85: Warning: This FrameLayout view is useless (no children, no background, no id, no style) [UselessLeaf]\n" +
            "    <FrameLayout\n" +
            "    ^\n" +
            "res/layout/useless.xml:13: Warning: This LinearLayout layout or its FrameLayout parent is useless [UselessParent]\n" +
            "        <LinearLayout\n" +
            "        ^\n" +
            "res/layout/useless.xml:47: Warning: This LinearLayout layout or its FrameLayout parent is useless; transfer the background attribute to the other view [UselessParent]\n" +
            "        <LinearLayout\n" +
            "        ^\n" +
            "res/layout/useless.xml:65: Warning: This LinearLayout layout or its FrameLayout parent is useless; transfer the background attribute to the other view [UselessParent]\n" +
            "        <LinearLayout\n" +
            "        ^\n" +
            "0 errors, 4 warnings\n" +
            "",
            lintFiles("res/layout/useless.xml"));
    }

    public void testTabHost() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/useless2.xml"));
    }

    public void testStyleAttribute() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/useless3.xml"));
    }

    public void testUselessLeafRoot() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/breadcrumbs_in_fragment.xml"));
    }

    public void testUseless65519() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=65519
        assertEquals(
                "No warnings.",

                lintFiles("res/layout/useless4.xml"));
    }
}
