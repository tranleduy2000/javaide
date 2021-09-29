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

public class AlarmDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new AlarmDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/AlarmTest.java:9: Warning: Value will be forced up to 5000 as of Android 5.1; don't rely on this to be exact [ShortAlarm]\n"
                + "        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 50, 10, null); // ERROR\n"
                + "                                                                 ~~\n"
                + "src/test/pkg/AlarmTest.java:9: Warning: Value will be forced up to 60000 as of Android 5.1; don't rely on this to be exact [ShortAlarm]\n"
                + "        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 50, 10, null); // ERROR\n"
                + "                                                                     ~~\n"
                + "src/test/pkg/AlarmTest.java:11: Warning: Value will be forced up to 60000 as of Android 5.1; don't rely on this to be exact [ShortAlarm]\n"
                + "                OtherClass.MY_INTERVAL, null);                          // ERROR\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AlarmTest.java:16: Warning: Value will be forced up to 60000 as of Android 5.1; don't rely on this to be exact [ShortAlarm]\n"
                + "        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 5000, interval2, null); // ERROR\n"
                + "                                                                       ~~~~~~~~~\n"
                + "0 errors, 4 warnings\n",

                lintProject("src/test/pkg/AlarmTest.java.txt=>src/test/pkg/AlarmTest.java"));
    }
}
