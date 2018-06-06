/*
 * Copyright (C) 2015 The Android Open Source Project
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
public class LogDetectorTest  extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new LogDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/LogTest.java:33: Error: Mismatched tags: the d() and isLoggable() calls typically should pass the same tag: TAG1 versus TAG2 [LogTagMismatch]\n" +
            "            Log.d(TAG2, \"message\"); // warn: mismatched tags!\n" +
            "                  ~~~~\n" +
            "    src/test/pkg/LogTest.java:32: Conflicting tag\n" +
            "src/test/pkg/LogTest.java:36: Error: Mismatched tags: the d() and isLoggable() calls typically should pass the same tag: \"my_tag\" versus \"other_tag\" [LogTagMismatch]\n" +
            "            Log.d(\"other_tag\", \"message\"); // warn: mismatched tags!\n" +
            "                  ~~~~~~~~~~~\n" +
            "    src/test/pkg/LogTest.java:35: Conflicting tag\n" +
            "src/test/pkg/LogTest.java:80: Error: Mismatched logging levels: when checking isLoggable level DEBUG, the corresponding log call should be Log.d, not Log.v [LogTagMismatch]\n" +
            "            Log.v(TAG1, \"message\"); // warn: wrong level\n" +
            "                ~\n" +
            "    src/test/pkg/LogTest.java:79: Conflicting tag\n" +
            "src/test/pkg/LogTest.java:83: Error: Mismatched logging levels: when checking isLoggable level DEBUG, the corresponding log call should be Log.d, not Log.v [LogTagMismatch]\n" +
            "            Log.v(TAG1, \"message\"); // warn: wrong level\n" +
            "                ~\n" +
            "    src/test/pkg/LogTest.java:82: Conflicting tag\n" +
            "src/test/pkg/LogTest.java:86: Error: Mismatched logging levels: when checking isLoggable level VERBOSE, the corresponding log call should be Log.v, not Log.d [LogTagMismatch]\n" +
            "            Log.d(TAG1, \"message\"); // warn? verbose is a lower logging level, which includes debug\n" +
            "                ~\n" +
            "    src/test/pkg/LogTest.java:85: Conflicting tag\n" +
            "src/test/pkg/LogTest.java:53: Error: The logging tag can be at most 23 characters, was 43 (really_really_really_really_really_long_tag) [LongLogTag]\n" +
            "            Log.d(\"really_really_really_really_really_long_tag\", \"message\"); // error: too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:59: Error: The logging tag can be at most 23 characters, was 24 (123456789012345678901234) [LongLogTag]\n" +
            "            Log.d(TAG24, \"message\"); // error: too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:60: Error: The logging tag can be at most 23 characters, was 39 (MyReallyReallyReallyReallyReallyLongTag) [LongLogTag]\n" +
            "            Log.d(LONG_TAG, \"message\"); // error: way too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:64: Error: The logging tag can be at most 23 characters, was 39 (MyReallyReallyReallyReallyReallyLongTag) [LongLogTag]\n" +
            "            Log.d(LOCAL_TAG, \"message\"); // error: too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:67: Error: The logging tag can be at most 23 characters, was 28 (1234567890123456789012MyTag1) [LongLogTag]\n" +
            "            Log.d(TAG22 + TAG1, \"message\"); // error: too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:68: Error: The logging tag can be at most 23 characters, was 27 (1234567890123456789012MyTag) [LongLogTag]\n" +
            "            Log.d(TAG22 + \"MyTag\", \"message\"); // error: too long\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:21: Warning: The log call Log.i(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogConditional]\n" +
            "        Log.i(TAG1, \"message\" + m); // error: unconditional w/ computation\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LogTest.java:22: Warning: The log call Log.i(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogConditional]\n" +
            "        Log.i(TAG1, toString()); // error: unconditional w/ computation\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "11 errors, 2 warnings\n",

            lintProject(
                "src/test/pkg/LogTest.java.txt=>src/test/pkg/LogTest.java",
                // stub for type resolution
                "src/test/pkg/Log.java.txt=>src/android/util/Log.java"
            ));
    }
}
