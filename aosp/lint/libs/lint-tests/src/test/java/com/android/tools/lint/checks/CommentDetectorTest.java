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
public class CommentDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new CommentDetector();
    }

    public void test() throws Exception {
        assertEquals(
        "src/test/pkg/Hidden.java:11: Warning: STOPSHIP comment found; points to code which must be fixed prior to release [StopShip]\n" +
        "    // STOPSHIP\n" +
        "       ~~~~~~~~\n" +
        "src/test/pkg/Hidden.java:12: Warning: STOPSHIP comment found; points to code which must be fixed prior to release [StopShip]\n" +
        "    /* We must STOPSHIP! */\n" +
        "               ~~~~~~~~\n" +
        "src/test/pkg/Hidden.java:5: Warning: Code might be hidden here; found unicode escape sequence which is interpreted as comment end, compiled code follows [EasterEgg]\n" +
        "    /* \\u002a\\u002f static { System.out.println(\"I'm executed on class load\"); } \\u002f\\u002a */\n" +
        "       ~~~~~~~~~~~~\n" +
        "src/test/pkg/Hidden.java:6: Warning: Code might be hidden here; found unicode escape sequence which is interpreted as comment end, compiled code follows [EasterEgg]\n" +
        "    /* \\u002A\\U002F static { System.out.println(\"I'm executed on class load\"); } \\u002f\\u002a */\n" +
        "       ~~~~~~~~~~~~\n" +
        "0 errors, 4 warnings\n",

        lintProject("src/test/pkg/Hidden.java.txt=>src/test/pkg/Hidden.java"));
    }

    public void test2() throws Exception {
        assertEquals(
        "No warnings.",

        lintProject("src/test/pkg/SdCardTest.java.txt=>src/test/pkg/SdCardTest.java"));
    }
}
