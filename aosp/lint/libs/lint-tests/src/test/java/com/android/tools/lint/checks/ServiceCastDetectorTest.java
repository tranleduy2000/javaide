/*
 * Copyright (C) 2013 The Android Open Source Project
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
public class ServiceCastDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ServiceCastDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/SystemServiceTest.java:13: Error: Suspicious cast to DisplayManager for a DEVICE_POLICY_SERVICE: expected DevicePolicyManager [ServiceCast]\n"
                + "        DisplayManager displayServiceWrong = (DisplayManager) getSystemService(\n"
                + "                                             ^\n"
                + "src/test/pkg/SystemServiceTest.java:16: Error: Suspicious cast to WallpaperManager for a WALLPAPER_SERVICE: expected WallpaperService [ServiceCast]\n"
                + "        WallpaperManager wallPaperWrong = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);\n"
                + "                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/SystemServiceTest.java:22: Error: Suspicious cast to DisplayManager for a DEVICE_POLICY_SERVICE: expected DevicePolicyManager [ServiceCast]\n"
                + "        DisplayManager displayServiceWrong = (DisplayManager) context\n"
                + "                                             ^\n"
                + "3 errors, 0 warnings\n",

            lintProject("src/test/pkg/SystemServiceTest.java.txt=>" +
                    "src/test/pkg/SystemServiceTest.java"));
    }
}
