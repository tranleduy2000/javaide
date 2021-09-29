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
public class ScrollViewChildDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ScrollViewChildDetector();
    }

    public void testScrollView() throws Exception {
        assertEquals(
                "res/layout/wrong_dimension.xml:10: Warning: This LinearLayout should use android:layout_width=\"wrap_content\" [ScrollViewSize]\n" +
                "     android:layout_width=\"match_parent\"\n" +
                "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings\n" +
                "",
                lintFiles("res/layout/wrong_dimension.xml"));
    }
}
