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

package com.android.sdklib.internal.repository.archives;

import junit.framework.TestCase;

public class ArchiveTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testShortDescription() throws Exception {
        Archive a = new Archive(
                null, //pkg,
                new ArchFilter(HostOs.WINDOWS, null, null, null), //arch,
                null, //url
                0, //size
                null); //checksum
        assertEquals("Archive for Windows", a.getShortDescription());

        a = new Archive(
                null, //pkg,
                new ArchFilter(HostOs.LINUX, null, null, null), //arch,
                null, //url
                0, //size
                null); //checksum
        assertEquals("Archive for Linux", a.getShortDescription());

        a = new Archive(
                null, //pkg,
                new ArchFilter(HostOs.MACOSX, null, null, null), //arch,
                null, //url
                0, //size
                null); //checksum
        assertEquals("Archive for MacOS X", a.getShortDescription());

        a = new Archive(
                null, //pkg,
                null, //arch,
                null, //url
                0, //size
                null); //checksum
        assertEquals("Archive for any OS", a.getShortDescription());
    }

    public void testLongDescription() throws Exception {
        Archive a = new Archive(
                null, //pkg,
                new ArchFilter(HostOs.WINDOWS, null, null, null), //arch,
                null, //url
                900, //size
                "1234567890ABCDEF"); //checksum
        assertEquals(
                "Archive for Windows\n" +
                "Size: 900 Bytes\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());

        a = new Archive(null, new ArchFilter(HostOs.WINDOWS, null, null, null), null, 1100, "1234567890ABCDEF");
        assertEquals(
                "Archive for Windows\n" +
                "Size: 1 KiB\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());

        a = new Archive(null, new ArchFilter(HostOs.WINDOWS, null, null, null), null, 1900, "1234567890ABCDEF");
        assertEquals(
                "Archive for Windows\n" +
                "Size: 2 KiB\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());

        a = new Archive(null, new ArchFilter(HostOs.WINDOWS, null, null, null), null, (long)2e6, "1234567890ABCDEF");
        assertEquals(
                "Archive for Windows\n" +
                "Size: 1.9 MiB\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());

        a = new Archive(null, new ArchFilter(HostOs.WINDOWS, null, null, null), null, (long)19e6, "1234567890ABCDEF");
        assertEquals(
                "Archive for Windows\n" +
                "Size: 18.1 MiB\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());

        a = new Archive(null, new ArchFilter(HostOs.WINDOWS, null, null, null), null, (long)18e9, "1234567890ABCDEF");
        assertEquals(
                "Archive for Windows\n" +
                "Size: 16.8 GiB\n" +
                "SHA1: 1234567890ABCDEF",
                a.getLongDescription());
    }
}
