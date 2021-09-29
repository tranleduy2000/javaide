/*
 * Copyright (C) 2014 The Android Open Source Project
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
public class ByteOrderMarkDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ByteOrderMarkDetector();
    }

    public void test() throws Exception {
        // See issue b.android.com/65103
        assertEquals(""
                + "res/values-zh-rCN/bom.xml:3: Error: Found byte-order-mark in the middle of a file [ByteOrderMark]\n"
                + " <string name=\"hanping_chinese\uFEFF_lite\uFEFF_app_name\">(Translated name)</string>\n"
                + "         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject("res/values-zh-rCN/bom.xml"));
    }
}
