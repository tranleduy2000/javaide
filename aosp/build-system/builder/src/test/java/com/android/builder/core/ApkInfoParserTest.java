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

package com.android.builder.core;

import com.android.testutils.TestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;

public class ApkInfoParserTest extends TestCase {

    public void testPre21Output() throws Exception {
        File file = new File(TestUtils.getRoot("core"), "aapt20.txt");
        List<String> lines = Files.readLines(file, Charsets.UTF_8);

        ApkInfoParser.ApkInfo apkInfo = ApkInfoParser.getApkInfo(lines);

        assertNotNull(apkInfo);
        assertEquals("com.android.tests.basic.debug", apkInfo.getPackageName());
        assertEquals(Integer.valueOf(12), apkInfo.getVersionCode());
        assertEquals("2.0", apkInfo.getVersionName());
    }

    public void testPost21Output() throws Exception {
        File file = new File(TestUtils.getRoot("core"), "aapt21.txt");
        List<String> lines = Files.readLines(file, Charsets.UTF_8);

        ApkInfoParser.ApkInfo apkInfo = ApkInfoParser.getApkInfo(lines);

        assertNotNull(apkInfo);
        assertEquals("com.android.tests.basic.debug", apkInfo.getPackageName());
        assertEquals(Integer.valueOf(12), apkInfo.getVersionCode());
        assertEquals("2.0", apkInfo.getVersionName());
    }
}
