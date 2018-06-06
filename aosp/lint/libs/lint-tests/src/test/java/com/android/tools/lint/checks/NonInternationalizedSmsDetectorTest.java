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
public class NonInternationalizedSmsDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new NonInternationalizedSmsDetector();
    }

    public void test() throws Exception {
        assertEquals(
                "src/test/pkg/NonInternationalizedSmsDetectorTest.java:18: Warning: To make sure the SMS can be sent by all users, please start the SMS number with a + and a country code or restrict the code invocation to people in the country you are targeting. [UnlocalizedSms]\n" +
                "  sms.sendMultipartTextMessage(\"001234567890\", null, null, null, null);\n" +
                "                               ~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings\n" +
                "",

            lintProject("src/test/pkg/NonInternationalizedSmsDetectorTest.java.txt=>src/test/pkg/NonInternationalizedSmsDetectorTest.java"));
    }
}