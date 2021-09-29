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
public class ArraySizeDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ArraySizeDetector();
    }
    public void testArraySizes() throws Exception {
        assertEquals(
            "res/values/arrays.xml:3: Warning: Array security_questions has an inconsistent number of items (3 in values-nl-rNL/arrays.xml, 4 in values-cs/arrays.xml) [InconsistentArrays]\n" +
            "    <string-array name=\"security_questions\">\n" +
            "    ^\n" +
            "    res/values-cs/arrays.xml:3: Declaration with array size (4)\n" +
            "    res/values-es/strings.xml:12: Declaration with array size (4)\n" +
            "    res/values-nl-rNL/arrays.xml:3: Declaration with array size (3)\n" +
            "res/values/arrays.xml:10: Warning: Array signal_strength has an inconsistent number of items (5 in values/arrays.xml, 6 in values-land/arrays.xml) [InconsistentArrays]\n" +
            "    <array name=\"signal_strength\">\n" +
            "    ^\n" +
            "    res/values-land/arrays.xml:2: Declaration with array size (6)\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject(
                 "res/values/arrays.xml",
                 "res/values-cs/arrays.xml",
                 "res/values-land/arrays.xml",
                 "res/values-nl-rNL/arrays.xml",
                 "res/values-es/strings.xml"));
    }

    public void testMultipleArrays() throws Exception {
        assertEquals(
            "res/values/stringarrays.xml:3: Warning: Array map_density_desc has an inconsistent number of items (5 in values/stringarrays.xml, 1 in values-it/stringarrays.xml) [InconsistentArrays]\n" +
            "    <string-array name=\"map_density_desc\">\n" +
            "    ^\n" +
            "    res/values-it/stringarrays.xml:6: Declaration with array size (1)\n" +
            "0 errors, 1 warnings\n",

            lintProject(
                 "res/values-it/stringarrays.xml",
                 "res/values/stringarrays.xml"));
    }

    public void testArraySizesSuppressed() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                 "res/values/arrays.xml",
                 "res/values-land/arrays_ignore.xml=>res/values-land/arrays.xml"));
    }

    public void testArraySizesIncremental() throws Exception {
        assertEquals(""
            + "res/values/arrays.xml:3: Warning: Array security_questions has an inconsistent number of items (4 in values/arrays.xml, 3 in values-nl-rNL/arrays.xml) [InconsistentArrays]\n"
            + "    <string-array name=\"security_questions\">\n"
            + "    ^\n"
            + "res/values/arrays.xml:10: Warning: Array signal_strength has an inconsistent number of items (5 in values/arrays.xml, 6 in values-land/arrays.xml) [InconsistentArrays]\n"
            + "    <array name=\"signal_strength\">\n"
            + "    ^\n"
            + "0 errors, 2 warnings\n",

            lintProjectIncrementally("res/values/arrays.xml",
                    "res/values/arrays.xml",
                    "res/values-cs/arrays.xml",
                    "res/values-land/arrays.xml",
                    "res/values-nl-rNL/arrays.xml",
                    "res/values-es/strings.xml"));
    }

}
