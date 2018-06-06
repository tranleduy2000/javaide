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
public class Utf8DetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new Utf8Detector();
    }

    public void testIsoLatin() throws Exception {
        assertEquals(
            "res/layout/encoding.xml:1: Error: iso-latin-1: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n" +
            "<?xml version=\"1.0\" encoding=\"iso-latin-1\"?>\n" +
            "                              ~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n",
            lintProject("res/layout/encoding.xml"));
    }

    public void testWithWindowsCarriageReturn() throws Exception {
        assertEquals(
            "res/layout/encoding2.xml:1: Error: iso-latin-1: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n" +
            "<?xml version=\"1.0\" encoding=\"iso-latin-1\"?>\n" +
            "                              ~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n",
            // encoding2.xml = encoding.xml but with \n => \r
            lintProject("res/layout/encoding2.xml"));
    }

    public void testNegative() throws Exception {
        // Make sure we don't get warnings for a correct file
        assertEquals(
            "No warnings.",
            lintProject("res/layout/layout1.xml"));
    }

    public void testNoProlog() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject("res/layout/activity_item_two_pane.xml"));
    }

    public void testImplicitUtf16() throws Exception {
        // Implicit encoding: Not currently checked
        assertEquals("No warnings.",
                lintProject("encoding/UTF-16-bom-implicit.xml=>res/layout/layout.xml"));
    }

    public void testUtf16WithByteOrderMark() throws Exception {
        assertEquals(""
            + "res/layout/layout.xml:1: Error: UTF-16: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
            + "                              ~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject("encoding/UTF-16-bom.xml=>res/layout/layout.xml"));
    }

    public void testUtf16WithoutByteOrderMark() throws Exception {
        assertEquals(""
            + "res/layout/layout.xml:1: Error: UTF-16: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
            + "                              ~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject("encoding/UTF-16-nobom.xml=>res/layout/layout.xml"));
    }

    public void testUtf32WithByteOrderMark() throws Exception {
        assertEquals(""
            + "res/layout/layout.xml:1: Error: UTF_32: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
            + "<?xml version=\"1.0\" encoding=\"UTF_32\"?>\n"
            + "                              ~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject("encoding/UTF_32-bom.xml=>res/layout/layout.xml"));
    }

    public void testUtf32WithoutByteOrderMark() throws Exception {
        assertEquals(""
            + "res/layout/layout.xml:1: Error: UTF_32: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
            + "<?xml version=\"1.0\" encoding=\"UTF_32\"?>\n"
            + "                              ~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject("encoding/UTF_32-nobom.xml=>res/layout/layout.xml"));
    }

    public void testUtf32LeWithoutByteOrderMark() throws Exception {
        assertEquals(""
            + "res/layout/layout.xml:1: Error: UTF_32LE: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
            + "<?xml version=\"1.0\" encoding=\"UTF_32LE\"?>\n"
            + "                              ~~~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject("encoding/UTF_32LE-nobom.xml=>res/layout/layout.xml"));
    }

    public void testMacRoman() throws Exception {
        assertEquals(""
                        + "res/layout/layout.xml:1: Error: MacRoman: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
                        + "<?xml version=\"1.0\" encoding=\"MacRoman\"?>\n"
                        + "                              ~~~~~~~~\n"
                        + "1 errors, 0 warnings\n",
                lintProject("encoding/MacRoman.xml=>res/layout/layout.xml"));
    }

    public void testWindows1252() throws Exception {
        assertEquals(""
                        + "res/layout/layout.xml:1: Error: windows-1252: Not using UTF-8 as the file encoding. This can lead to subtle bugs with non-ascii characters [EnforceUTF8]\n"
                        + "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n"
                        + "                              ~~~~~~~~~~~~\n"
                        + "1 errors, 0 warnings\n",
                lintProject("encoding/Windows-1252.xml=>res/layout/layout.xml"));
    }
}
