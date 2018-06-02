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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.PreciseRevision;

import org.junit.Assert;
import junit.framework.TestCase;

import java.util.Arrays;

public class PreciseRevisionTest extends TestCase {

    public final void testPreciseRevision() {

        Assert.assertEquals("5", PreciseRevision.parseRevision("5").toString());
        assertEquals("5.0", PreciseRevision.parseRevision("5.0").toString());
        assertEquals("5.0.0", PreciseRevision.parseRevision("5.0.0").toString());
        assertEquals("5.1.4", PreciseRevision.parseRevision("5.1.4").toString());

        PreciseRevision p = new PreciseRevision(5);
        assertEquals(5, p.getMajor());
        assertEquals(PreciseRevision.IMPLICIT_MINOR_REV, p.getMinor());
        assertEquals(PreciseRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(PreciseRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse(p.isPreview());
        assertEquals("5", p.toShortString());
        assertEquals(p, PreciseRevision.parseRevision("5"));
        assertEquals("5", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("5"));
        assertEquals("[5]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(5, 0);
        assertEquals(5, p.getMajor());
        assertEquals(0, p.getMinor());
        assertEquals(PreciseRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(PreciseRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse(p.isPreview());
        assertEquals("5.0", p.toShortString());
        assertEquals(new PreciseRevision(5), PreciseRevision.parseRevision("5"));
        assertEquals("5.0", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("5.0"));
        assertEquals("[5, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(5, 0, 0);
        assertEquals(5, p.getMajor());
        assertEquals(0, p.getMinor());
        assertEquals(0, p.getMicro());
        assertEquals(PreciseRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse(p.isPreview());
        assertEquals("5.0.0", p.toShortString());
        assertEquals(new PreciseRevision(5), PreciseRevision.parseRevision("5"));
        assertEquals("5.0.0", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("5.0.0"));
        assertEquals("[5, 0, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5, 0, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(5, 0, 0, 6);
        assertEquals(5, p.getMajor());
        assertEquals(PreciseRevision.IMPLICIT_MINOR_REV, p.getMinor());
        assertEquals(PreciseRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(6, p.getPreview());
        assertTrue(p.isPreview());
        assertEquals("5.0.0 rc6", p.toShortString());
        assertEquals("5.0.0 rc6", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("5.0.0 rc6"));
        assertEquals("5.0.0-rc6", PreciseRevision.parseRevision("5.0.0-rc6").toString());
        assertEquals("[5, 0, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[5, 0, 0, 6]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(6, 7, 0);
        assertEquals(6, p.getMajor());
        assertEquals(7, p.getMinor());
        assertEquals(0, p.getMicro());
        assertEquals(0, p.getPreview());
        assertFalse(p.isPreview());
        assertEquals("6.7.0", p.toShortString());
        assertFalse(p.equals(PreciseRevision.parseRevision("6.7")));
        assertEquals(new PreciseRevision(6, 7), PreciseRevision.parseRevision("6.7"));
        assertEquals("6.7.0", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("6.7.0"));
        assertEquals("[6, 7, 0]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[6, 7, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(10, 11, 12, PreciseRevision.NOT_A_PREVIEW);
        assertEquals(10, p.getMajor());
        assertEquals(11, p.getMinor());
        assertEquals(12, p.getMicro());
        assertEquals(0, p.getPreview());
        assertFalse(p.isPreview());
        assertEquals("10.11.12", p.toShortString());
        assertEquals("10.11.12", p.toString());
        assertEquals("[10, 11, 12]",    Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[10, 11, 12, 0]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));

        p = new PreciseRevision(10, 11, 12, 13);
        assertEquals(10, p.getMajor());
        assertEquals(11, p.getMinor());
        assertEquals(12, p.getMicro());
        assertEquals(13, p.getPreview());
        assertTrue  (p.isPreview());
        assertEquals("10.11.12 rc13", p.toShortString());
        assertEquals("10.11.12 rc13", p.toString());
        assertEquals(p, PreciseRevision.parseRevision("10.11.12 rc13"));
        assertEquals(p, PreciseRevision.parseRevision("   10.11.12 rc13"));
        assertEquals(p, PreciseRevision.parseRevision("10.11.12 rc13   "));
        assertEquals(p, PreciseRevision.parseRevision("   10.11.12   rc13   "));
        assertEquals("[10, 11, 12]",     Arrays.toString(p.toIntArray(false /*includePreview*/)));
        assertEquals("[10, 11, 12, 13]", Arrays.toString(p.toIntArray(true  /*includePreview*/)));
    }

    public final void testParseError() {
        String errorMsg = null;
        try {
            PreciseRevision.parseRevision("not a number");
            fail("PreciseRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: not a number", errorMsg);

        errorMsg = null;
        try {
            PreciseRevision.parseRevision("5 .6 .7");
            fail("PreciseRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: 5 .6 .7", errorMsg);

        errorMsg = null;
        try {
            PreciseRevision.parseRevision("5.0.0 preview 1");
            fail("PreciseRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: 5.0.0 preview 1", errorMsg);

        errorMsg = null;
        try {
            PreciseRevision.parseRevision("  5.1.2 rc 42  ");
            fail("PreciseRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision:   5.1.2 rc 42  ", errorMsg);
    }

    public final void testCompareTo() {
        PreciseRevision s4 = new PreciseRevision(4);
        PreciseRevision i4 = new PreciseRevision(4);
        PreciseRevision g5 = new PreciseRevision(5, 1, 0, 6);
        PreciseRevision y5 = new PreciseRevision(5);
        PreciseRevision c5 = new PreciseRevision(5, 1, 0, 6);
        PreciseRevision o5 = new PreciseRevision(5, 0, 0, 7);
        PreciseRevision p5 = new PreciseRevision(5, 1, 0, 0);

        assertEquals(s4, i4);                   // 4.0.0-0 == 4.0.0-0
        assertEquals(g5, c5);                   // 5.1.0-6 == 5.1.0-6

        assertFalse(y5.equals(p5));             // 5.0.0-0 != 5.1.0-0
        assertFalse(g5.equals(p5));             // 5.1.0-6 != 5.1.0-0
        assertTrue (s4.compareTo(i4) == 0);     // 4.0.0-0 == 4.0.0-0
        assertTrue (s4.compareTo(y5)  < 0);     // 4.0.0-0  < 5.0.0-0
        assertTrue (y5.compareTo(y5) == 0);     // 5.0.0-0 == 5.0.0-0
        assertTrue (y5.compareTo(p5)  < 0);     // 5.0.0-0  < 5.1.0-0
        assertTrue (o5.compareTo(y5)  < 0);     // 5.0.0-7  < 5.0.0-0
        assertTrue (p5.compareTo(p5) == 0);     // 5.1.0-0 == 5.1.0-0
        assertTrue (c5.compareTo(p5)  < 0);     // 5.1.0-6  < 5.1.0-0
        assertTrue (p5.compareTo(c5)  > 0);     // 5.1.0-0  > 5.1.0-6
        assertTrue (p5.compareTo(o5)  > 0);     // 5.1.0-0  > 5.0.0-7
        assertTrue (c5.compareTo(o5)  > 0);     // 5.1.0-6  > 5.0.0-7
        assertTrue (o5.compareTo(o5) == 0);     // 5.0.0-7  > 5.0.0-7
    }
}