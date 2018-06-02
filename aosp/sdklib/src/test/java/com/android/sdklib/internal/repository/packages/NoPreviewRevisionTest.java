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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.NoPreviewRevision;

import java.util.Arrays;

import junit.framework.TestCase;

public class NoPreviewRevisionTest extends TestCase {

    public final void testNoPreviewRevision() {
        NoPreviewRevision p = new NoPreviewRevision(5);
        assertEquals(5, p.getMajor());
        assertEquals(NoPreviewRevision.IMPLICIT_MINOR_REV, p.getMinor());
        assertEquals(NoPreviewRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(NoPreviewRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("5", p.toShortString());
        assertEquals(p, NoPreviewRevision.parseRevision("5"));
        assertEquals("5.0.0", p.toString());
        assertEquals(p, NoPreviewRevision.parseRevision("5.0.0"));
        assertEquals("[5, 0, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5, 0, 0, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new NoPreviewRevision(5, 0, 0);
        assertEquals(5, p.getMajor());
        assertEquals(NoPreviewRevision.IMPLICIT_MINOR_REV, p.getMinor());
        assertEquals(NoPreviewRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(NoPreviewRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("5", p.toShortString());
        assertEquals(p, NoPreviewRevision.parseRevision("5"));
        assertEquals("5.0.0", p.toString());
        assertEquals(p, NoPreviewRevision.parseRevision("5.0.0"));
        assertEquals("[5, 0, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5, 0, 0, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new NoPreviewRevision(6, 7, 0);
        assertEquals(6, p.getMajor());
        assertEquals(7, p.getMinor());
        assertEquals(0, p.getMicro());
        assertEquals(0, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("6.7", p.toShortString());
        assertEquals(p, NoPreviewRevision.parseRevision("6.7"));
        assertEquals("6.7.0", p.toString());
        assertEquals(p, NoPreviewRevision.parseRevision("6.7.0"));
        assertEquals("[6, 7, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[6, 7, 0, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new NoPreviewRevision(10, 11, 12);
        assertEquals(10, p.getMajor());
        assertEquals(11, p.getMinor());
        assertEquals(12, p.getMicro());
        assertEquals(0, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("10.11.12", p.toShortString());
        assertEquals("10.11.12", p.toString());
        assertEquals(p, NoPreviewRevision.parseRevision("10.11.12"));
        assertEquals("[10, 11, 12]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[10, 11, 12, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new NoPreviewRevision(10, 11, 12);
        assertEquals(10, p.getMajor());
        assertEquals(11, p.getMinor());
        assertEquals(12, p.getMicro());
        assertEquals(NoPreviewRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("10.11.12", p.toShortString());
        assertEquals("10.11.12", p.toString());
        assertEquals(p, NoPreviewRevision.parseRevision("10.11.12"));
        assertEquals(p, NoPreviewRevision.parseRevision("   10.11.12"));
        assertEquals(p, NoPreviewRevision.parseRevision("10.11.12   "));
        assertEquals(p, NoPreviewRevision.parseRevision("   10.11.12   "));
        assertEquals("[10, 11, 12]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[10, 11, 12, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));
    }

    public final void testParseError() {
        String errorMsg = null;
        try {
            NoPreviewRevision.parseRevision("not a number");
            fail("NoPreviewRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: not a number", errorMsg);

        errorMsg = null;
        try {
            NoPreviewRevision.parseRevision("5 .6 .7");
            fail("NoPreviewRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: 5 .6 .7", errorMsg);

        errorMsg = null;
        try {
            NoPreviewRevision.parseRevision("5.0.0 preview 1");
            fail("NoPreviewRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: 5.0.0 preview 1", errorMsg);

        errorMsg = null;
        try {
            NoPreviewRevision.parseRevision("  5.1.2 rc 42  ");
            fail("NoPreviewRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision:   5.1.2 rc 42  ", errorMsg);
    }

    public final void testCompareTo() {
        NoPreviewRevision a4 = new NoPreviewRevision(4);
        NoPreviewRevision b4 = new NoPreviewRevision(4, 1, 0);
        NoPreviewRevision c5 = new NoPreviewRevision(5);
        NoPreviewRevision d5 = new NoPreviewRevision(5, 1, 0);

        NoPreviewRevision t4 = new NoPreviewRevision(4, 0, 0);
        NoPreviewRevision u5 = new NoPreviewRevision(5, 0, 0);

        assertEquals(a4, t4);                   // 4.0.0 == 4.0.0
        assertFalse(a4.equals(b4));             // 4.0.0 != 4.1.0
        assertTrue (a4.compareTo(b4) < 0);      // 4.0.0  < 4.1.0
        assertTrue (a4.compareTo(c5) < 0);      // 4.0.0  < 5.0.0
        assertTrue (a4.compareTo(d5) < 0);      // 4.0.0  < 5.1.0

        assertTrue (b4.compareTo(a4) > 0);      // 4.1.0  > 4.0.0
        assertTrue (b4.compareTo(c5) < 0);      // 4.1.0  < 5.0.0
        assertTrue (b4.compareTo(d5) < 0);      // 4.1.0  < 5.1.0

        assertEquals(c5, u5);                   // 5.0.0 == 5.0.0
        assertFalse(c5.equals(d5));             // 5.0.0 != 5.1.0
        assertTrue (c5.compareTo(a4) > 0);      // 5.0.0  > 4.0.0
        assertTrue (c5.compareTo(b4) > 0);      // 5.0.0  > 4.1.0
        assertTrue (c5.compareTo(d5) < 0);      // 5.0.0  < 5.1.0

        assertTrue (d5.compareTo(a4) > 0);      // 5.1.0  > 4.0.0
        assertTrue (d5.compareTo(b4) > 0);      // 5.1.0  > 4.1.0
        assertTrue (d5.compareTo(c5) > 0);      // 5.1.0  > 5.0.0
    }

}
