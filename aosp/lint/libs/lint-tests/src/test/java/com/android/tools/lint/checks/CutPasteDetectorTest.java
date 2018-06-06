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
public class CutPasteDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new CutPasteDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/PasteError.java:15: Warning: The id R.id.textView1 has already been looked up in this method; possible cut & paste error? [CutPasteId]\n" +
            "        View view2 = findViewById(R.id.textView1);\n" +
            "                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    src/test/pkg/PasteError.java:14: First usage here\n" +
            "src/test/pkg/PasteError.java:71: Warning: The id R.id.textView1 has already been looked up in this method; possible cut & paste error? [CutPasteId]\n" +
            "            view2 = findViewById(R.id.textView1);\n" +
            "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    src/test/pkg/PasteError.java:68: First usage here\n" +
            "src/test/pkg/PasteError.java:78: Warning: The id R.id.textView1 has already been looked up in this method; possible cut & paste error? [CutPasteId]\n" +
            "            view2 = findViewById(R.id.textView1);\n" +
            "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    src/test/pkg/PasteError.java:76: First usage here\n" +
            "src/test/pkg/PasteError.java:86: Warning: The id R.id.textView1 has already been looked up in this method; possible cut & paste error? [CutPasteId]\n" +
            "            view2 = findViewById(R.id.textView1);\n" +
            "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    src/test/pkg/PasteError.java:83: First usage here\n" +
            "src/test/pkg/PasteError.java:95: Warning: The id R.id.textView1 has already been looked up in this method; possible cut & paste error? [CutPasteId]\n" +
            "                view2 = findViewById(R.id.textView1);\n" +
            "                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    src/test/pkg/PasteError.java:91: First usage here\n" +
            "0 errors, 5 warnings\n",

            lintProject("src/test/pkg/PasteError.java.txt=>" +
                    "src/test/pkg/PasteError.java"));
    }
}
