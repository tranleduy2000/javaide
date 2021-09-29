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

import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;

@SuppressWarnings("javadoc")
public class WrongCallDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WrongCallDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/LayoutTest.java:23: Error: Suspicious method call; should probably call \"layout\" rather than \"onLayout\" [WrongCall]\n"
                + "  child.onLayout(changed, left, top, right, bottom); // Not OK\n"
                + "        ~~~~~~~~\n"
                + "src/test/pkg/LayoutTest.java:25: Error: Suspicious method call; should probably call \"measure\" rather than \"onMeasure\" [WrongCall]\n"
                + "  super.onMeasure(0, 0); // Not OK\n"
                + "        ~~~~~~~~~\n"
                + "src/test/pkg/LayoutTest.java:26: Error: Suspicious method call; should probably call \"draw\" rather than \"onDraw\" [WrongCall]\n"
                + "  super.onDraw(null); // Not OK\n"
                + "        ~~~~~~\n"
                + "src/test/pkg/LayoutTest.java:33: Error: Suspicious method call; should probably call \"layout\" rather than \"onLayout\" [WrongCall]\n"
                + "  super.onLayout(false, 0, 0, 0, 0); // Not OK\n"
                + "        ~~~~~~~~\n"
                + "src/test/pkg/LayoutTest.java:34: Error: Suspicious method call; should probably call \"measure\" rather than \"onMeasure\" [WrongCall]\n"
                + "  child.onMeasure(widthMeasureSpec, heightMeasureSpec); // Not OK\n"
                + "        ~~~~~~~~~\n"
                + "src/test/pkg/LayoutTest.java:41: Error: Suspicious method call; should probably call \"draw\" rather than \"onDraw\" [WrongCall]\n"
                + "  child.onDraw(canvas); // Not OK\n"
                + "        ~~~~~~\n"
                + "6 errors, 0 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                    "res/layout/onclick.xml=>res/layout/onclick.xml",
                    "bytecode/LayoutTest.java.txt=>src/test/pkg/LayoutTest.java",
                    "bytecode/LayoutTest.class.data=>bin/classes/test/pkg/LayoutTest.class"
            ));
    }

    public void testGetOldValue() {
        assertEquals("onLayout", WrongCallDetector.getOldValue(
                "Suspicious method call; should probably call \"layout\" rather than \"onLayout\"",
                TEXT));
    }

    public void testGetNewValue() {
        assertEquals("layout", WrongCallDetector.getNewValue(
                "Suspicious method call; should probably call \"layout\" rather than \"onLayout\"",
                TEXT));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        assertNotNull(message, WrongCallDetector.getOldValue(message, TEXT));
        assertNotNull(message, WrongCallDetector.getNewValue(message, TEXT));
    }
}
