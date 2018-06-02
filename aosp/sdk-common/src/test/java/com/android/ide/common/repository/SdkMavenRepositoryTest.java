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

package com.android.ide.common.repository;

import com.android.annotations.Nullable;
import com.android.sdklib.SdkManager;
import com.android.sdklib.repository.local.LocalSdk;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;

import junit.framework.TestCase;

import java.io.File;

public class SdkMavenRepositoryTest extends TestCase {

    public void testGetLocation() {
        assertNull(SdkMavenRepository.ANDROID.getRepositoryLocation(null, false));

        File sdkHome = getTestSdk();
        if (sdkHome == null) {
            return;
        }

        File android = SdkMavenRepository.ANDROID.getRepositoryLocation(sdkHome, true);
        assertNotNull(android);

        File google = SdkMavenRepository.GOOGLE.getRepositoryLocation(sdkHome, true);
        assertNotNull(google);
    }

    public void testGetBestMatch() {
        assertNull(SdkMavenRepository.ANDROID.getHighestInstalledVersion(
                null, "com.android.support", "support-v4", "19", false));

        File sdkHome = getTestSdk();
        if (sdkHome == null) {
            return;
        }

        GradleCoordinate gc1 = SdkMavenRepository.ANDROID.getHighestInstalledVersion(
                sdkHome, "com.android.support", "support-v4", "19", false);
        assertEquals(GradleCoordinate.parseCoordinateString(
                "com.android.support:support-v4:19.1.0"), gc1);

        GradleCoordinate gc2 = SdkMavenRepository.ANDROID.getHighestInstalledVersion(
                sdkHome, "com.android.support", "support-v4", "20", false);
        assertEquals(GradleCoordinate.parseCoordinateString(
                "com.android.support:support-v4:20.0.0"), gc2);

        /* These tests only applied when 21 was marked as a preview release; it no longer
           is. Re-enable when we get another preview platform.
        GradleCoordinate gc3 = SdkMavenRepository.ANDROID.getHighestInstalledVersion(
                sdkHome, "com.android.support", "support-v4", "22", false);
        assertNull(gc3);

        GradleCoordinate gc4 = SdkMavenRepository.ANDROID.getHighestInstalledVersion(
                sdkHome, "com.android.support", "support-v4", "21", true);
        assertEquals(GradleCoordinate.parseCoordinateString(
                "com.android.support:support-v4:21.0.0-rc1"), gc4);
        */
    }

    public void testIsInstalled() {
        assertFalse(SdkMavenRepository.ANDROID.isInstalled((File)null));
        assertFalse(SdkMavenRepository.ANDROID.isInstalled((LocalSdk)null));

        File sdkHome = getTestSdk();
        if (sdkHome == null) {
            return;
        }

        assertTrue(SdkMavenRepository.ANDROID.isInstalled(sdkHome));
        assertTrue(SdkMavenRepository.GOOGLE.isInstalled(sdkHome));

        ILogger logger = new StdLogger(StdLogger.Level.INFO);
        SdkManager sdkManager = SdkManager.createManager(sdkHome.getPath(), logger);
        assertNotNull(sdkManager);
        assertTrue(SdkMavenRepository.ANDROID.isInstalled(sdkManager.getLocalSdk()));
        assertTrue(SdkMavenRepository.GOOGLE.isInstalled(sdkManager.getLocalSdk()));
    }

    public void testGetDirName() {
        assertEquals("android", SdkMavenRepository.ANDROID.getDirName());
        assertEquals("google", SdkMavenRepository.GOOGLE.getDirName());
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetByGroupId() {
        assertSame(SdkMavenRepository.ANDROID, SdkMavenRepository.getByGroupId(
                GradleCoordinate.parseCoordinateString(
                        "com.android.support:appcompat-v7:13.0.0").getGroupId()));
        assertSame(SdkMavenRepository.ANDROID, SdkMavenRepository.getByGroupId(
                GradleCoordinate.parseCoordinateString(
                        "com.android.support.test:espresso:0.2").getGroupId()));
        assertSame(SdkMavenRepository.GOOGLE, SdkMavenRepository.getByGroupId(
                GradleCoordinate.parseCoordinateString(
                        "com.google.android.gms:play-services:5.2.08").getGroupId()));
        assertSame(SdkMavenRepository.GOOGLE, SdkMavenRepository.getByGroupId(
                GradleCoordinate.parseCoordinateString(
                        "com.google.android.gms:play-services-wearable:5.0.77").getGroupId()));
        assertNull(SdkMavenRepository.getByGroupId(GradleCoordinate.parseCoordinateString(
                "com.google.guava:guava:11.0.2").getGroupId()));
    }

    /**
     * Environment variable or system property containing the full path to an SDK install
     */
    public static final String SDK_PATH_PROPERTY = "ADT_TEST_SDK_PATH";

    @Nullable
    private static File getTestSdk() {
        String sdkHome = getTestSdkPath();
        if (sdkHome != null) {
            File file = new File(sdkHome);
            assertTrue(file.getPath(), file.isDirectory());
            return file;
        }

        return null;
    }

    @Nullable
    private static String getTestSdkPath() {
        String override = System.getProperty(SDK_PATH_PROPERTY);
        if (override != null) {
            assertTrue(override, new File(override).exists());
            return override;
        }
        override = System.getenv(SDK_PATH_PROPERTY);
        if (override != null) {
            return override;
        }

        return null;
    }
}