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
public class ToastDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ToastDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/ToastTest.java:31: Warning: Toast created but not shown: did you forget to call show() ? [ShowToast]\n" +
            "        Toast.makeText(context, \"foo\", Toast.LENGTH_LONG);\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/ToastTest.java:32: Warning: Expected duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG, a custom duration value is not supported [ShowToast]\n" +
            "        Toast toast = Toast.makeText(context, R.string.app_name, 5000);\n" +
            "                                                                 ~~~~\n" +
            "src/test/pkg/ToastTest.java:32: Warning: Toast created but not shown: did you forget to call show() ? [ShowToast]\n" +
            "        Toast toast = Toast.makeText(context, R.string.app_name, 5000);\n" +
            "                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/ToastTest.java:38: Warning: Toast created but not shown: did you forget to call show() ? [ShowToast]\n" +
            "        Toast.makeText(context, \"foo\", Toast.LENGTH_LONG);\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n" +
            "",

            lintProject("src/test/pkg/ToastTest.java.txt=>src/test/pkg/ToastTest.java"));
    }
}
