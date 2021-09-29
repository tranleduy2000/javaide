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
public class WrongImportDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WrongImportDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/BadImport.java:5: Warning: Don't include android.R here; use a fully qualified name for each usage instead [SuspiciousImport]\n" +
            "import android.R;\n" +
            "~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject(
                // Java files must be renamed in source tree
                "src/test/pkg/BadImport.java.txt=>src/test/pkg/BadImport.java"));
    }
}
