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
public class TooManyViewsDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new TooManyViewsDetector();
    }

    public void testTooMany() throws Exception {
        assertEquals(
            "res/layout/too_many.xml:399: Warning: too_many.xml has more than 80 views, bad for performance [TooManyViews]\n" +
            "                <Button\n" +
            "                ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/too_many.xml"));
    }

    public void testTooDeep() throws Exception {
        assertEquals(
            "res/layout/too_deep.xml:46: Warning: too_deep.xml has more than 10 levels, bad for performance [TooDeepLayout]\n" +
            "                                    <LinearLayout\n" +
            "                                    ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/too_deep.xml"));
    }
}
