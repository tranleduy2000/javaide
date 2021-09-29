/*
 * Copyright (C) 2014 The Android Open Source Project
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

public class OverrideConcreteDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new OverrideConcreteDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/OverrideConcreteTest.java:23: Error: Must override android.service.notification.NotificationListenerService.onNotificationPosted(android.service.notification.StatusBarNotification): Method was abstract until 21, and your minSdkVersion is 18 [OverrideAbstract]\n"
                + "    private static class MyNotificationListenerService2 extends NotificationListenerService {\n"
                + "                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/OverrideConcreteTest.java:30: Error: Must override android.service.notification.NotificationListenerService.onNotificationRemoved(android.service.notification.StatusBarNotification): Method was abstract until 21, and your minSdkVersion is 18 [OverrideAbstract]\n"
                + "    private static class MyNotificationListenerService3 extends NotificationListenerService {\n"
                + "                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/OverrideConcreteTest.java:37: Error: Must override android.service.notification.NotificationListenerService.onNotificationPosted(android.service.notification.StatusBarNotification): Method was abstract until 21, and your minSdkVersion is 18 [OverrideAbstract]\n"
                + "    private static class MyNotificationListenerService4 extends NotificationListenerService {\n"
                + "                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/OverrideConcreteTest.java:57: Error: Must override android.service.notification.NotificationListenerService.onNotificationRemoved(android.service.notification.StatusBarNotification): Method was abstract until 21, and your minSdkVersion is 18 [OverrideAbstract]\n"
                + "    private static class MyNotificationListenerService7 extends MyNotificationListenerService3 {\n"
                + "                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",

                lintProject(
                        "src/test/pkg/OverrideConcreteTest.java.txt=>src/test/pkg/OverrideConcreteTest.java"));
    }
}
