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
public class ExtraTextDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ExtraTextDetector();
    }

    public void testBroken() throws Exception {
        assertEquals(
            "res/layout/broken.xml:6: Warning: Unexpected text found in layout file: \"ImageButton android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_heigh...\" [ExtraText]\n" +
            "    <Button android:text=\"Button\" android:id=\"@+id/button2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"></Button>\n" +
            "    ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject("res/layout/broken.xml"));
    }
}
